# 📋 Project Cleanup & Rationalization Plan

> **Date:** 2026-04-22  
> **Project:** mkt-reverse  
> **Status:** Pre-execution checklist

---

## 🎯 Executive Summary

Sequential cleanup tasks before any test coverage work:

1. ✅ Delete redundant docs (keep README, ARCHITECTURE, CHANGELOG)
2. ⏳ Lock down MVC+JPA stack and document it
3. ⏳ Consolidate 4 docker-composes into 1 with profiles
4. ⏳ Close/resolve 5 open PRs
5. ⏳ Remove noise files, add to .gitignore

---

## Step 1 — Delete Redundant Docs

### Files to DELETE (root level):
```
FINAL_DELIVERY.md           — dup of README+ARCHITECTURE
PROJECT_FINAL_DELIVERY.md   — dup of FINAL_DELIVERY
IMPLEMENTATION_STATUS.md     — obsolete progress tracker
PULL_REQUEST.md            — internal workflow doc
ROADMAP.md                 — dup of product/vision.md
QUICKSTART.md               — dup of DOCKER_README.md
DEVELOPMENT_GUIDE.md       — dup of SETUP.md
EXECUTIVE_SUMMARY.md        — dup of README
PROJECT_STATUS.md          — dup of docs/PROJECT_STATUS.md
DOCKER_README.md            — dup of docker/README.md
ARCHITECTURE.md             — KEEP (reference doc)
QUICKSTART.md              — dup
```

### Files to DELETE (docs/):
```
docs/PROJECT_STATUS.md          — dup of root-level PROJECT_STATUS.md
docs/SETUP.md                — dup of root-level SETUP.md
docs/requirements.md         — dup of product/vision.md
docs/mvp-analysis.md         — historical, superseded by product/vision.md
docs/workflows/mvp-*.md      — obsolete workflow docs
docs/quality/consistency-report.md — internal dev note
docs/analysis/complexity-analysis.md  — internal dev note
docs/analysis/edge-cases-analysis.md  — internal dev note
docs/analysis/risk-analysis.md      — internal dev note
docs/analysis/test-coverage-analysis.md — internal dev note
docs/planning/*.md               — all superseded by package-by-feature-plan.md
docs/product/*.md (keep vision.md only)
docs/architecture/clean-arch-ddd.md       — internal reference note
docs/architecture/clean-architecture-guidelines.md — internal reference note
docs/architecture/enterprise-patterns.md     — internal reference note
docs/architecture/multi-tenancy-markets.md   — internal reference note
docs/architecture/system-context.md        — internal reference note
```

### Files to KEEP:
```
README.md               — project entry point ✅
ARCHITECTURE.md        — technical reference ✅
CHANGELOG.md           — CREATE THIS (keep tracking changes)
docs/product/vision.md — product spec anchor ✅
docs/planning/package-by-feature-plan.md — active ref ✅
```

### CHANGELOG.md template:
```markdown
# Changelog

## [Unreleased]

### Added
- package-by-feature structure under features/auth/

### Changed
- (list breaking changes here)

### Deprecated
- (list deprecated features)

### Removed
- Redundant documentation files

### Fixed
- (list bug fixes)

### Security
- (security changes)
```

---

## Step 2 — Lock Stack: MVC + JPA

### Stack Decision: MVC+JPA (NOT WebFlux+R2DBC)

| Aspect | Decision | Rationale |
|--------|-----------|-----------|
| **HTTP** | Spring MVC (`spring-boot-starter-web`) | Stable, well-documented, rich ecosystem |
| **Database** | Spring Data JPA + Hibernate | Mature, excellent tooling, team familiarity |
| **Auth** | JWT (access + refresh tokens) | Stateless, simple, sufficient for MVP |
| **Validation** | Bean Validation (JSR-380) | Standard, declarative |
| **Security** | Spring Security + RBAC | Proven, @PreAuthorize already in use |

### Rationale:
- JPA's `@Version` optimistic locking already handles concurrency
- MVC's synchronous model simplifies business logic reasoning
- No urgent need for reactive: request volumes are low in MVP
- Switching to WebFlux+R2DBC would require ~3 weeks of rewrite for no near-term benefit

### File: STACK.md
```markdown
# Stack Definition

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 LTS |
| Framework | Spring Boot | 3.2.x |
| HTTP | Spring MVC | (built-in) |
| Database | PostgreSQL | 16 |
| ORM | Spring Data JPA / Hibernate | 6.x |
| Security | Spring Security + JWT | (built-in) |
| Validation | Bean Validation (JSR-380) | (built-in) |
| Build | Maven | 3.9+ |
| Frontend | React 18 + TypeScript | 5.x |
| Bundler | Vite | 5.x |
| Container | Docker + Docker Compose | (latest) |

## What We Are NOT Using (and Why)

| Rejected | Reason |
|---------|--------|
| WebFlux + R2DBC | Reactive needed only at high concurrency; adds complexity without MVP benefit |
| GraphQL | REST sufficient for current API surface; can add later if needed |
| Kafka | RabbitMQ is sufficient for event streaming at MVP scale |
| Elasticsearch | PostgreSQL full-text search is sufficient; upgrade path exists |
| MongoDB | Relational model fits the domain; no document-store need identified |
```

---

## Step 3 — Consolidate Docker Composes

### 4 files → 1 file with profiles

Target: `docker-compose.yml` with profiles:
```yaml
services:
  postgres-main:     # profile: [local, dev, mvp]
  postgres-user:     # profile: [local, dev]
  rabbitmq:          # profile: [local, dev, mvp]
  redis:             # profile: [local, dev]
  api-gateway:       # profile: [local, dev, mvp]
  web-app:           # profile: [local, dev]
  mailhog:           # profile: [local, dev]
  adminer:           # profile: [local]
  jaeger:            # profile: [local, dev]

# Profiles:
# local  — full dev environment (all services)
# dev    — API + database (CI/CD)
# mvp    — minimal production-like
```

### Files to DELETE after consolidation:
```
docker-compose.dev.yml    — merge into profiles
docker-compose.local.yml — merge into profiles
docker-compose.mvp.yml   — merge into profiles
docker-compose.yml       — rename after merge
```

---

## Step 4 — Resolve Open PRs

### Open PRs (identified by branch):
```
branch: dev       — "docs: Update complexity analysis" (legitimate doc update)
branch: main      — (no open PRs on main)
other branches:   — need to list: gh pr list --state open
```

### Strategy:
- Close PRs that are purely doc-only updates with no code changes
- Merge PRs that have both docs AND code changes
- Keep 1 PR maximum for doc-only changes

### Action:
```bash
gh pr list --state open --json number,title,headRefName
# Review each; close non-critical doc PRs with note:
# "Closing - content superseded by recent commits"
```

---

## Step 5 — Remove Noise Files

### Files to DELETE:
```
test.py                    — Python script, not referenced
.Jules/                   — Jules AI config
.agents/                  — agent config
.openclaw/                — OpenClaw config
```

### Add to .gitignore:
```
# Agent / AI tool configs
.Jules/
.agents/
.openclaw/
*.pyc
__pycache__/
*.log
```

---

## Execution Order

```bash
# 1. Delete docs (no git involved — just rm)
rm -f FINAL_DELIVERY.md PROJECT_FINAL_DELIVERY.md \
  IMPLEMENTATION_STATUS.md PULL_REQUEST.md ROADMAP.md \
  QUICKSTART.md EXECUTIVE_SUMMARY.md PROJECT_STATUS.md \
  DOCKER_README.md DEVELOPMENT_GUIDE.md ARCHITECTURE.md

# 2. Create STACK.md and CHANGELOG.md
# (write files)

# 3. Consolidate docker-compose files
# (write unified docker-compose.yml)

# 4. Close PRs
# gh pr close <number>

# 5. Remove noise + update .gitignore
rm -f test.py
rm -rf .Jules .agents .openclaw
echo ".Jules/" >> .gitignore
echo ".agents/" >> .gitignore
echo ".openclaw/" >> .gitignore

# 6. Commit everything as one atomic cleanup commit
git add -A
git commit -m "chore: Project rationalization cleanup"
```

---

## Definition of Done

- [ ] Only 3 docs at root: README.md, ARCHITECTURE.md, CHANGELOG.md
- [ ] STACK.md exists at root and is accurate
- [ ] One docker-compose.yml with profiles
- [ ] All open PRs resolved (closed or merged)
- [ ] No .py, .Jules, .agents, .openclaw in repo
- [ ] .gitignore updated
- [ ] One commit titled "chore: Project rationalization cleanup"

---

*Execution estimated: ~30 minutes (all reversible via git)*