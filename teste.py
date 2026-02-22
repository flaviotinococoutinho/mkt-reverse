#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Ebook Concatenator — gera um EPUB (e opcionalmente um Markdown) a partir de um
projeto Java/Spring/Gradle/Maven com capítulos por diretório/pacote ou por tipo
de arquivo, índice de classes/arquivos e sintaxe destacada.

Sem dependências obrigatórias (apenas Python 3.9+). Se Pygments estiver
instalado, usa para highlight; caso contrário, aplica um realce simples.

USO BÁSICO
----------
python ebook_concat_builder.py \
   /caminho/do/repositorio \
   --title "Nome do Projeto" \
   --author "Seu Nome" \
   --strategy by-dir \
   --output build/ebook.epub \
   --markdown build/concat.md

ESTRATÉGIAS DE CAPÍTULO
-----------------------
- by-dir  : capítulo = diretório/pacote; subcapítulo = arquivo/classe
- by-type : capítulo = tipo (Java, XML, YAML, etc.); subcapítulo = arquivo

LEITURA DE CONFIG
-----------------
Se existir .concatconfig.yml na raiz do repo, são lidas chaves opcionais:
- include_ext: [".java", ".xml", ...]
- exclude_globs: ["**/target/**", "**/build/**", ...]
- max_mb: inteiro (tamanho máximo por arquivo)

METADADOS GIT
-------------
- Tenta capturar branch, hash do commit e dirty flag; se git não estiver
  disponível, continua normalmente.

SAÍDAS
------
- EPUB estilizado (com TOC/nav.xhtml; páginas por capítulo e por arquivo)
- Markdown (concat.md) com seções e blocos de código com linguagens apropriadas

LIMITAÇÕES
----------
- O highlight "fallback" é propositalmente simples (comentários/strings etc.).
- EPUB não executa JS; o estilo é CSS puro.

"""

from __future__ import annotations

import argparse
import datetime as _dt
import fnmatch
import html
import io
import os
import re
import subprocess
import sys
import textwrap
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Tuple
import zipfile

# ---------------------------------------------------------------
# Configuração padrão
# ---------------------------------------------------------------
DEFAULT_INCLUDE_EXT = {
    ".java", ".kt", ".xml", ".yaml", ".yml", ".properties", ".sql",
    ".gradle", ".groovy", ".md", ".json", ".sh", ".bash", ".bat",
    ".dockerfile", ".txt",
}

DEFAULT_EXCLUDE_GLOBS = [
    "**/.git/**", "**/.idea/**", "**/.vscode/**", "**/node_modules/**",
    "**/target/**", "**/build/**", "**/.gradle/**", "**/.mvn/**",
    "**/.venv/**", "**/__pycache__/**",
]

DEFAULT_MAX_MB = 2  # evita entrar arquivos enormes

EPUB_UUID_SCHEME = "urn:uuid:"

# ---------------------------------------------------------------
# Utilitários
# ---------------------------------------------------------------

def try_import_yaml():
    try:
        import yaml  # type: ignore
        return yaml
    except Exception:
        return None


def load_concat_config(root: Path) -> Tuple[set, List[str], int]:
    include_ext = set(DEFAULT_INCLUDE_EXT)
    exclude_globs = list(DEFAULT_EXCLUDE_GLOBS)
    max_mb = DEFAULT_MAX_MB

    cfg = root / ".concatconfig.yml"
    y = try_import_yaml()
    if cfg.exists() and y:
        try:
            data = y.safe_load(cfg.read_text(encoding="utf-8")) or {}
            if isinstance(data.get("include_ext"), list):
                include_ext = set(map(str, data["include_ext"]))
            if isinstance(data.get("exclude_globs"), list):
                exclude_globs = list(map(str, data["exclude_globs"]))
            if isinstance(data.get("max_mb"), int):
                max_mb = int(data["max_mb"])
        except Exception:
            pass
    return include_ext, exclude_globs, max_mb


def is_binary_sample(sample: bytes) -> bool:
    # Heurística simples
    if b"\x00" in sample:
        return True
    # Muitos bytes fora de ASCII podem sugerir binário; relaxado
    high = sum(b > 127 for b in sample)
    return high > max(16, len(sample) // 3)


def detect_language(path: Path) -> str:
    name = path.name.lower()
    ext = path.suffix.lower()
    if name == "dockerfile" or ext == ".dockerfile":
        return "dockerfile"
    return {
        ".java": "java",
        ".kt": "kotlin",
        ".xml": "xml",
        ".yaml": "yaml",
        ".yml": "yaml",
        ".properties": "properties",
        ".sql": "sql",
        ".gradle": "groovy",
        ".groovy": "groovy",
        ".md": "markdown",
        ".json": "json",
        ".sh": "bash",
        ".bash": "bash",
        ".bat": "bat",
        ".txt": "text",
    }.get(ext, "text")


def slugify(text: str) -> str:
    text = re.sub(r"[^\w\-\.]+", "-", text, flags=re.U)
    text = re.sub(r"-+", "-", text)
    return text.strip("-")[:128]


def read_git_meta(root: Path) -> Dict[str, str]:
    meta = {"branch": "", "commit": "", "dirty": ""}
    def run(args: List[str]) -> str:
        try:
            out = subprocess.check_output(args, cwd=str(root), stderr=subprocess.DEVNULL)
            return out.decode().strip()
        except Exception:
            return ""
    meta["branch"] = run(["git", "rev-parse", "--abbrev-ref", "HEAD"]) or "unknown"
    meta["commit"] = run(["git", "rev-parse", "--short", "HEAD"]) or "unknown"
    status = run(["git", "status", "--porcelain"]) or ""
    meta["dirty"] = "*" if status else ""
    return meta

# ---------------------------------------------------------------
# Extração de classes (Java/Kotlin) para Índice
# ---------------------------------------------------------------
JAVA_CLASS_RE = re.compile(
    r"\b(public|protected|private)?\s*(abstract\s+|final\s+)?(class|interface|enum|record)\s+([A-Za-z_][A-Za-z0-9_]*)\b"
)
JAVA_PACKAGE_RE = re.compile(r"^\s*package\s+([a-z0-9_.]+)\s*;", re.I | re.M)

@dataclass
class ClassEntry:
    name: str
    package: str
    path: Path
    anchor: str

# ---------------------------------------------------------------
# Highlight — tenta Pygments; fallback simples se indisponível
# ---------------------------------------------------------------
try:
    from pygments import highlight  # type: ignore
    from pygments.lexers import get_lexer_by_name  # type: ignore
    from pygments.formatters import HtmlFormatter  # type: ignore
    _HAS_PYGMENTS = True
except Exception:
    _HAS_PYGMENTS = False


class SimpleHighlighter:
    """Fallback minimalista para algumas linguagens.
    Não cobre todas as nuances; serve para dar um leve ganho visual.
    """
    @staticmethod
    def _wrap(code: str) -> str:
        # Adiciona números de linha via spans
        out = ["<pre class=\"codeblock\"><code>"]
        for i, line in enumerate(code.splitlines(), start=1):
            out.append(f"<span class=\"ln\" data-l=\"{i}\"></span>{html.escape(line)}\n")
        out.append("</code></pre>")
        return "".join(out)

    @classmethod
    def java(cls, text: str) -> str:
        # Comentários e strings
        text = re.sub(r"/\*.*?\*/", lambda m: f"<span class=\"cm\">{html.escape(m.group(0))}</span>", text, flags=re.S)
        text = re.sub(r"//.*?$", lambda m: f"<span class=\"cm\">{html.escape(m.group(0))}</span>", text, flags=re.M)
        text = re.sub(r"'(?:\\.|[^\\'])'|\"(?:\\.|[^\\\"])*\"",
                      lambda m: f"<span class=\"st\">{html.escape(m.group(0))}</span>", text)
        # Anotações
        text = re.sub(r"@[A-Za-z_][A-Za-z0-9_]*", lambda m: f"<span class=\"an\">{html.escape(m.group(0))}</span>", text)
        return cls._wrap(text)

    @classmethod
    def xml(cls, text: str) -> str:
        text = html.escape(text)
        # tags
        text = re.sub(r"(&lt;/?)([A-Za-z_:][A-Za-z0-9_\-\.:]*)([^&]*?)(/?&gt;)",
                      r"\1<span class=\"tg\">\2</span><span class=\"at\">\3</span>\4", text)
        return cls._wrap(text)

    @classmethod
    def yaml(cls, text: str) -> str:
        # comentários
        text = re.sub(r"#.*$", lambda m: f"<span class=\"cm\">{html.escape(m.group(0))}</span>", text, flags=re.M)
        # chaves simples
        text = re.sub(r"^([ \-]*)([^:#\n]+):(.*)$",
                      lambda m: f"{m.group(1)}<span class=\"ky\">{html.escape(m.group(2))}</span>:{html.escape(m.group(3))}",
                      text, flags=re.M)
        return cls._wrap(text)

    @classmethod
    def properties(cls, text: str) -> str:
        text = re.sub(r"^\s*#.*$", lambda m: f"<span class=\"cm\">{html.escape(m.group(0))}</span>", text, flags=re.M)
        text = re.sub(r"^([^=#\n]+)(=)(.*)$",
                      lambda m: f"<span class=\"ky\">{html.escape(m.group(1))}</span>{m.group(2)}{html.escape(m.group(3))}",
                      text, flags=re.M)
        return cls._wrap(text)

    @classmethod
    def bash(cls, text: str) -> str:
        text = re.sub(r"#.*$", lambda m: f"<span class=\"cm\">{html.escape(m.group(0))}</span>", text, flags=re.M)
        text = re.sub(r"'(?:\\.|[^\\'])*'|\"(?:\\.|[^\\\"])*\"",
                      lambda m: f"<span class=\"st\">{html.escape(m.group(0))}</span>", text)
        return cls._wrap(text)

    @classmethod
    def text(cls, text: str) -> str:
        return cls._wrap(text)


# ---------------------------------------------------------------
# EPUB builder (mínimo viável)
# ---------------------------------------------------------------
@dataclass
class EpubItem:
    id: str
    href: str
    media_type: str
    content: bytes


@dataclass
class EpubBook:
    title: str
    author: str
    lang: str = "pt-BR"
    uid: str = field(default_factory=lambda: os.urandom(16).hex())
    items: List[EpubItem] = field(default_factory=list)
    spine: List[str] = field(default_factory=list)
    nav_points: List[Tuple[str, str, List[Tuple[str, str]]]] = field(default_factory=list)
    # nav_points: [(chap_id, chap_title, [(sub_id, sub_title), ...])]

    def add_item(self, item: EpubItem):
        self.items.append(item)

    def add_to_spine(self, item_id: str):
        self.spine.append(item_id)

    def add_nav_point(self, chap_id: str, chap_title: str, subs: List[Tuple[str, str]]):
        self.nav_points.append((chap_id, chap_title, subs))

    def build(self, out_path: Path):
        out_path.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(out_path, "w") as z:
            # mimetype deve ser o primeiro e sem compressão
            z.writestr("mimetype", "application/epub+zip", compress_type=zipfile.ZIP_STORED)
            # container
            z.writestr("META-INF/container.xml", textwrap.dedent(f"""
                <?xml version="1.0" encoding="UTF-8"?>
                <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                  <rootfiles>
                    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
                  </rootfiles>
                </container>
            """).strip())
            # itens
            for item in self.items:
                z.writestr(f"OEBPS/{item.href}", item.content)
            # content.opf
            manifest_items = []
            for it in self.items:
                manifest_items.append(f'<item id="{it.id}" href="{it.href}" media-type="{it.media_type}"/>')
            spine_items = []
            for sid in self.spine:
                spine_items.append(f'<itemref idref="{sid}" />')
            opf = textwrap.dedent(f"""
                <?xml version="1.0" encoding="UTF-8"?>
                <package xmlns="http://www.idpf.org/2007/opf" unique-identifier="bookid" version="3.0" xml:lang="{self.lang}">
                  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <dc:identifier id="bookid">{EPUB_UUID_SCHEME}{self.uid}</dc:identifier>
                    <dc:title>{html.escape(self.title)}</dc:title>
                    <dc:creator>{html.escape(self.author)}</dc:creator>
                    <dc:language>{self.lang}</dc:language>
                    <meta property="dcterms:modified">{_dt.datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')}</meta>
                  </metadata>
                  <manifest>
                    {''.join(manifest_items)}
                  </manifest>
                  <spine>
                    {''.join(spine_items)}
                  </spine>
                </package>
            """).strip()
            z.writestr("OEBPS/content.opf", opf)

# ---------------------------------------------------------------
# Varredura do projeto / árvore / modelagem de capítulos
# ---------------------------------------------------------------
@dataclass
class FileEntry:
    path: Path
    rel: Path
    lang: str
    title: str
    size: int
    anchor: str
    classes: List[ClassEntry] = field(default_factory=list)


def iter_files(root: Path, include_ext: set, exclude_globs: List[str], max_mb: int) -> Iterable[FileEntry]:
    max_bytes = max_mb * 1024 * 1024
    all_paths = sorted(p for p in root.rglob("*") if p.is_file())

    # Pré-computa exclusões
    def excluded(p: Path) -> bool:
        for pat in exclude_globs:
            if fnmatch.fnmatch(str(p.as_posix()), pat):
                return True
        return False

    for p in all_paths:
        if excluded(p):
            continue
        ext = p.suffix.lower()
        name_lower = p.name.lower()
        # Dockerfile sem extensão
        if name_lower == "dockerfile":
            lang = "dockerfile"
        elif ext in include_ext:
            lang = detect_language(p)
        else:
            continue
        try:
            size = p.stat().st_size
            if size > max_bytes:
                continue
            with open(p, "rb") as fh:
                head = fh.read(4096)
                if is_binary_sample(head):
                    continue
        except Exception:
            continue

        rel = p.relative_to(root)
        anchor = slugify(str(rel))
        fe = FileEntry(path=p, rel=rel, lang=lang, title=str(rel), size=size, anchor=anchor)

        if lang in {"java", "kotlin"}:
            try:
                txt = p.read_text(encoding="utf-8", errors="replace")
                pkg = JAVA_PACKAGE_RE.search(txt)
                pkg_name = pkg.group(1) if pkg else ""
                for m in JAVA_CLASS_RE.finditer(txt):
                    cls = ClassEntry(name=m.group(4), package=pkg_name, path=rel, anchor=f"{anchor}--{m.group(4)}")
                    fe.classes.append(cls)
            except Exception:
                pass

        yield fe


@dataclass
class Chapter:
    id: str
    title: str
    files: List[FileEntry]


def group_by_strategy(files: List[FileEntry], strategy: str, root: Path) -> List[Chapter]:
    chapters: List[Chapter] = []
    if strategy == "by-type":
        by_type: Dict[str, List[FileEntry]] = {}
        for f in files:
            key = f.lang
            by_type.setdefault(key, []).append(f)
        for lang, lst in sorted(by_type.items()):
            chap_id = slugify(f"chap-{lang}")
            chapters.append(Chapter(id=chap_id, title=f"{lang.upper()}", files=sorted(lst, key=lambda x: str(x.rel))))
        return chapters

    # default: by-dir (pacotes)
    by_dir: Dict[str, List[FileEntry]] = {}
    for f in files:
        # define "raiz lógica": src/main/java/... => usa o pacote após esse prefixo
        rel_str = f.rel.as_posix()
        m = re.search(r"src/(main|test)/java/(.*)", rel_str)
        if m:
            key = m.group(2).split("/")[0] if "/" in m.group(2) else m.group(2)
        else:
            # caso geral: primeiro diretório
            parts = f.rel.parts
            key = parts[0] if parts else "root"
        by_dir.setdefault(key or "root", []).append(f)
    for d, lst in sorted(by_dir.items()):
        chap_id = slugify(f"chap-{d}")
        chapters.append(Chapter(id=chap_id, title=d, files=sorted(lst, key=lambda x: str(x.rel))))
    return chapters

# ---------------------------------------------------------------
# Renderização (HTML para EPUB) e Markdown paralelo
# ---------------------------------------------------------------
CSS_BASE = (
    ".page{font-family:system-ui,-apple-system,Segoe UI,Roboto,'Helvetica Neue',Arial,sans-serif;line-height:1.5;}") + \
    (" h1,h2,h3{font-weight:700;}") + \
    (" pre.codeblock{background:#0b1020;color:#e8eaf6;padding:0.8rem;border-radius:10px;overflow-x:auto;}") + \
    (" .ln{counter-increment: line; display:inline-block; width:3.5em; text-align:right; padding-right:.75em; margin-right:.75em; opacity:.35;}") + \
    (" .ln:before{content: counter(line);}") + \
    (" .cm{color:#8bc34a;} .st{color:#ffeb3b;} .an{color:#ff9800;} .ky{color:#64b5f6;} .tg{color:#ffcc80;} .at{color:#b0bec5;}") + \
    (" a{color:#2962ff;text-decoration:none;} a:hover{text-decoration:underline;}") + \
    (" table.meta{border-collapse:collapse;margin:.5rem 0;} table.meta td{border:1px solid #ccc;padding:.25rem .5rem;}")

HTML_SHELL = """<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="{lang}">
<head>
  <meta charset="utf-8" />
  <title>{title}</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <style>{css}</style>
</head>
<body class="page">
{body}
</body>
</html>
"""


def page(title: str, body: str, lang: str = "pt-BR") -> bytes:
    return HTML_SHELL.format(title=html.escape(title), css=CSS_BASE, body=body, lang=lang).encode("utf-8")


def highlight_html(text: str, lang: str) -> str:
    if _HAS_PYGMENTS:
        try:
            lexer = get_lexer_by_name(lang, stripall=False)
        except Exception:
            lexer = get_lexer_by_name("text", stripall=False)
        fmt = HtmlFormatter(nowrap=True, cssclass="codeblock")
        code = highlight(text, lexer, fmt)
        # adiciona spans de número de linha
        out = ["<pre class=\"codeblock\"><code>"]
        for i, line in enumerate(code.splitlines(), start=1):
            out.append(f"<span class=\"ln\" data-l=\"{i}\"></span>{line}\n")
        out.append("</code></pre>")
        return "".join(out)

    # Fallback
    method = getattr(SimpleHighlighter, lang, SimpleHighlighter.text)
    return method(text)


def render_file_section(fe: FileEntry, root: Path) -> Tuple[str, List[str]]:
    """Retorna (html_section, anchors) para o arquivo."""
    text = fe.path.read_text(encoding="utf-8", errors="replace")
    code_html = highlight_html(text, fe.lang)

    meta_rows = []
    meta_rows.append(f"<tr><td>Path</td><td>{html.escape(str(fe.rel))}</td></tr>")
    meta_rows.append(f"<tr><td>Tamanho</td><td>{fe.size} bytes</td></tr>")
    meta_rows.append(f"<tr><td>Linguagem</td><td>{fe.lang}</td></tr>")

    anchors = [fe.anchor]
    if fe.classes:
        for ce in fe.classes:
            anchors.append(ce.anchor)

    header = f"<h2 id=\"{fe.anchor}\">{html.escape(fe.title)}</h2>"
    if fe.classes:
        header += "<ul>" + "".join(
            f"<li><a href=\"#{ce.anchor}\">{html.escape(ce.package + '.' if ce.package else '')}{html.escape(ce.name)}</a></li>" for ce in fe.classes
        ) + "</ul>"

    html_block = (
        f"{header}\n"
        f"<table class=\"meta\">{''.join(meta_rows)}</table>\n"
        f"{code_html}\n"
    )
    return html_block, anchors


def render_chapter(ch: Chapter, root: Path, book: EpubBook) -> Tuple[EpubItem, List[EpubItem], List[Tuple[str, str]]]:
    subs: List[Tuple[str, str]] = []
    sections: List[str] = []
    anchors: List[str] = []
    for fe in ch.files:
        sec, a = render_file_section(fe, root)
        sections.append(sec)
        anchors.extend(a)
        subs.append((fe.anchor, fe.title))

    body = f"<h1 id=\"{ch.id}\">{html.escape(ch.title)}</h1>\n" + "\n".join(sections)
    content = page(title=ch.title, body=body)

    chap_item = EpubItem(id=ch.id, href=f"chapters/{ch.id}.xhtml", media_type="application/xhtml+xml", content=content)

    # Também cria pequenas âncoras vazias (auxiliar — opcional)
    anchor_items: List[EpubItem] = []
    for a in anchors:
        # placeholders (não obrigatório, leitores usam ID na própria página)
        pass

    return chap_item, anchor_items, subs


def render_frontmatter(title: str, author: str, git_meta: Dict[str, str], started_at: str) -> EpubItem:
    body = textwrap.dedent(f"""
        <h1>{html.escape(title)}</h1>
        <p><strong>Autor:</strong> {html.escape(author)}</p>
        <p><strong>Gerado em:</strong> {html.escape(started_at)}</p>
        <h2>Metadados</h2>
        <table class="meta">
          <tr><td>Branch</td><td>{html.escape(git_meta.get('branch',''))}</td></tr>
          <tr><td>Commit</td><td>{html.escape(git_meta.get('commit',''))}{html.escape(git_meta.get('dirty',''))}</td></tr>
        </table>
        <p>Este ebook foi gerado automaticamente a partir do repositório fonte, com capítulos por pacote/diretório ou por tipo de arquivo e subcapítulos por arquivo/classe.</p>
    """).strip()
    return EpubItem(id="frontmatter", href="front.xhtml", media_type="application/xhtml+xml", content=page(title=title, body=body))


def render_nav(book: EpubBook, title: str) -> EpubItem:
    # EPUB3 nav.xhtml
    lis = []
    for chap_id, chap_title, subs in book.nav_points:
        sublis = "".join(f"<li><a href='chapters/{chap_id}.xhtml#{slugify(sid)}'>{html.escape(st)}</a></li>" for sid, st in subs)
        lis.append(f"<li><a href='chapters/{chap_id}.xhtml'>{html.escape(chap_title)}</a><ol>{sublis}</ol></li>")
    body = f"<h1>{html.escape(title)}</h1><nav epub:type='toc'><ol>{''.join(lis)}</ol></nav>"
    return EpubItem(id="nav", href="nav.xhtml", media_type="application/xhtml+xml", content=page(title="Sumário", body=body))


def render_style() -> EpubItem:
    return EpubItem(id="style", href="style.css", media_type="text/css", content=CSS_BASE.encode("utf-8"))

# ---------------------------------------------------------------
# Markdown paralelo (concat.md)
# ---------------------------------------------------------------

def to_markdown(chapters: List[Chapter], root: Path, title: str, author: str, git_meta: Dict[str, str]) -> str:
    lines: List[str] = []
    lines.append(f"# {title}")
    lines.append("")
    lines.append(f"Autor: {author}")
    lines.append(f"Gerado em: {_dt.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    lines.append("")
    lines.append("## Metadados Git")
    lines.append("")
    lines.append(f"- Branch: {git_meta.get('branch','')}")
    lines.append(f"- Commit: {git_meta.get('commit','')}{git_meta.get('dirty','')}")
    lines.append("")
    lines.append("## Sumário")
    for ch in chapters:
        lines.append(f"- [{ch.title}](#{slugify(ch.id)})")
        for fe in ch.files:
            lines.append(f"  - [{fe.title}](#{slugify(fe.anchor)})")
    lines.append("")

    for ch in chapters:
        lines.append(f"\n# {ch.title}\n")
        for fe in ch.files:
            lines.append(f"\n## {fe.title}\n")
            try:
                text = fe.path.read_text(encoding="utf-8", errors="replace")
            except Exception:
                text = "<erro ao ler arquivo>"
            lang = fe.lang if fe.lang != "text" else ""
            fence_lang = lang if lang else ""
            lines.append(f"```{fence_lang}")
            lines.append(text)
            lines.append("```")
    return "\n".join(lines)

# ---------------------------------------------------------------
# CLI principal
# ---------------------------------------------------------------

def main(argv: Optional[List[str]] = None) -> int:
    p = argparse.ArgumentParser(description="Gera EPUB e Markdown concatenados de um projeto.")
    p.add_argument("repo", nargs="?", default=".", type=str, help="Caminho do repositório (padrão: diretório atual)")
    p.add_argument("--title", type=str, default="Projeto Concatenado", help="Título do ebook")
    p.add_argument("--author", type=str, default="Concatenador", help="Autor")
    p.add_argument("--strategy", choices=["by-dir", "by-type"], default="by-dir", help="Estratégia de capítulos")
    p.add_argument("--output", type=str, default="build/ebook.epub", help="Arquivo EPUB de saída")
    p.add_argument("--markdown", type=str, default="build/concat.md", help="Arquivo Markdown de saída")
    p.add_argument("--max-mb", type=int, default=None, help="Tamanho máximo de arquivo (MB)")
    p.add_argument("--include-tests", action="store_true", help="Inclui src/test/**")
    p.add_argument("--exclude", action="append", default=[], help="Globs de exclusão adicionais")
    p.add_argument("--only-ext", action="append", default=[], help="Lista adicional de extensões a incluir (prefixo .)")
    args = p.parse_args(argv)

    root = Path(args.repo).resolve()
    if not root.exists():
        print(f"[ERRO] Caminho não encontrado: {root}", file=sys.stderr)
        return 2

    include_ext, exclude_globs, max_mb_cfg = load_concat_config(root)
    if args.only_ext:
        include_ext |= set(args.only_ext)
    if not args.include_tests:
        exclude_globs.append("**/src/test/**")
    exclude_globs.extend(args.exclude)

    max_mb = args.max_mb if args.max_mb is not None else max_mb_cfg

    files = list(iter_files(root, include_ext, exclude_globs, max_mb))
    if not files:
        print("[AVISO] Nenhum arquivo elegível encontrado.")

    chapters = group_by_strategy(files, args.strategy, root)

    git_meta = read_git_meta(root)
    started_at = _dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    # EPUB
    book = EpubBook(title=args.title, author=args.author)
    book.add_item(render_style())
    fm = render_frontmatter(args.title, args.author, git_meta, started_at)
    book.add_item(fm)
    book.add_to_spine(fm.id)

    for ch in chapters:
        chap_item, anchor_items, subs = render_chapter(ch, root, book)
        book.add_item(chap_item)
        book.add_to_spine(chap_item.id)
        for it in anchor_items:
            book.add_item(it)
        book.add_nav_point(chap_item.id, ch.title, subs)

    nav = render_nav(book, "Sumário")
    book.add_item(nav)

    out_epub = Path(args.output)
    book.build(out_epub)

    # Markdown
    out_md = Path(args.markdown)
    out_md.parent.mkdir(parents=True, exist_ok=True)
    out_md.write_text(to_markdown(chapters, root, args.title, args.author, git_meta), encoding="utf-8")

    print(f"[OK] EPUB: {out_epub}")
    print(f"[OK] Markdown: {out_md}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
ßß
