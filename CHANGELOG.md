# Changelog

All notable changes are documented here. Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- `features/auth/` — package-by-feature structure for authentication module

### Removed
- Redundant documentation files (FINAL_DELIVERY, PROJECT_FINAL_DELIVERY, IMPLEMENTATION_STATUS, etc.)
- Obsolete workflow and planning docs
- Agent config directories (.Jules, .agents, .openclaw, test.py)

### Changed
- Stack locked: **MVC + JPA** (NOT WebFlux+R2DBC) — see STACK.md
- Docker Compose consolidated: 4 files → 1 with profiles

### Fixed
- (none yet)

---

## [1.0.0] — 2026-04-15

### Added
- JWT authentication with refresh tokens
- Sourcing event management (CRUD + publication)
- Supplier proposal submission and acceptance flow
- PostgreSQL full-text search for opportunities
- Alert system for opportunity matching
- Docker Compose development environment
- Package-by-feature structure under `features/auth/`
- Validation chain (Chain of Responsibility pattern)
- Optimistic locking via `@Version`

### Changed
- Single PostgreSQL database with multi-tenant support
- Spring Security with RBAC (@PreAuthorize)
- Input sanitization for XSS prevention
- Spring Boot 3.2.x, Java 21

---

## [0.1.0] — 2026-04-10

### Added
- Initial project structure
- Multi-module Maven setup
- React + TypeScript frontend scaffold
- API Gateway with REST controllers

[Unreleased]: https://github.com/flaviotinococoutinho/mkt-reverse/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/flaviotinococoutinho/mkt-reverse/releases/tag/v1.0.0
[0.1.0]: https://github.com/flaviotinococoutinho/mkt-reverse/releases/tag/v0.1.0