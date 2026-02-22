# Marketplace Reverso - Project Memory & Guidelines

## 1. Project Context
- **Name:** mkt-reverse (Marketplace Reverso)
- **Architecture:** Monorepo with Java Backend (Maven) and React Frontend (Vite).
- **Goal:** Provide a comprehensive platform with reverse marketplace dynamics.

## 2. Frontend Stack & Conventions
- **Path:** `frontend/mkt-reverse-web/`
- **Frameworks:** React 19, Vite, React Router v7.
- **State/Data Fetching:** `@tanstack/react-query`.
- **Typing & Validation:** TypeScript (strict mode), `zod`.
- **Testing:** `vitest` + React Testing Library (`jsdom`).
- **Styling Conventions:** Prefer modern module-based or utility-first patterns (as implemented via `clsx`).
- **Rules:**
  - Always use TypeScript for new files.
  - Implement data fetching strictly via TanStack Query.
  - Adhere to functional components and React Hooks.
  - Write tests for core logical components using Vitest.

## 3. Backend Stack & Conventions
- **Build System:** Maven (`pom.xml`, `mvnw`).
- **Structure:** Modular structure (`application`, `modules`).
- **Patterns:** Enterprise patterns as documented in `Padrões_Enterprise_Avançados_para_Marketplace_Reve.md`.
- **Rules:**
  - Strict adherence to defined Enterprise Architectures.
  - Maintain isolated modules.

## 4. Workflows & Rules of Engagement
- **Documentation First:** Consult `README.md`, `CHECKPOINT.md`, and `PULL_REQUEST.md` for broader scope context before making sweeping changes.
- **Code Quality:** Ensure all code passes `npm run build` and `npm run test` before declaring a feature complete.
- **Independence:** Never modify files outside of the specifically requested task boundary unless strictly required for type safety or test coverage.

## 5. Agent Instructions
- Always read this file as part of your system instructions when operating within this workspace.
- Start backend actions from the repository root `/Users/flaviocoutinho/development/mkt-reverse`.
- Start frontend actions from the `/Users/flaviocoutinho/development/mkt-reverse/frontend/mkt-reverse-web` directory.