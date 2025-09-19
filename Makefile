# =============================================================================
# MARKETPLACE REVERSO - MAKEFILE
# =============================================================================

.PHONY: help build test clean docker-up docker-down docker-logs setup-dev

# Default target
.DEFAULT_GOAL := help

# Colors for output
RED=\033[0;31m
GREEN=\033[0;32m
YELLOW=\033[1;33m
BLUE=\033[0;34m
NC=\033[0m # No Color

# Project variables
PROJECT_NAME=marketplace-reverso
DOCKER_COMPOSE_FILE=docker-compose.yml
MAVEN_OPTS=-Dmaven.test.skip=false

help: ## Show this help message
	@echo "$(BLUE)Marketplace Reverso - Available Commands$(NC)"
	@echo "========================================"
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make $(YELLOW)<target>$(NC)\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  $(YELLOW)%-15s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(BLUE)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Development

setup-dev: ## Setup development environment
	@echo "$(BLUE)Setting up development environment...$(NC)"
	@if [ ! -f .env ]; then cp .env.example .env; echo "$(GREEN)Created .env file from template$(NC)"; fi
	@echo "$(GREEN)Development environment ready!$(NC)"
	@echo "$(YELLOW)Please review and update .env file with your settings$(NC)"

build: ## Build all modules
	@echo "$(BLUE)Building all modules...$(NC)"
	@mvn clean compile $(MAVEN_OPTS)
	@echo "$(GREEN)Build completed successfully!$(NC)"

test: ## Run all tests
	@echo "$(BLUE)Running tests...$(NC)"
	@mvn test
	@echo "$(GREEN)Tests completed!$(NC)"

test-integration: ## Run integration tests
	@echo "$(BLUE)Running integration tests...$(NC)"
	@mvn verify
	@echo "$(GREEN)Integration tests completed!$(NC)"

test-coverage: ## Generate test coverage report
	@echo "$(BLUE)Generating test coverage report...$(NC)"
	@mvn clean test jacoco:report
	@echo "$(GREEN)Coverage report generated at target/site/jacoco/index.html$(NC)"

package: ## Package all modules
	@echo "$(BLUE)Packaging all modules...$(NC)"
	@mvn clean package -DskipTests
	@echo "$(GREEN)Packaging completed!$(NC)"

install: ## Install all modules to local repository
	@echo "$(BLUE)Installing all modules...$(NC)"
	@mvn clean install
	@echo "$(GREEN)Installation completed!$(NC)"

##@ Docker Operations

docker-build: ## Build Docker images
	@echo "$(BLUE)Building Docker images...$(NC)"
	@docker-compose build
	@echo "$(GREEN)Docker images built successfully!$(NC)"

docker-up: ## Start all services with Docker Compose
	@echo "$(BLUE)Starting all services...$(NC)"
	@docker-compose up -d
	@echo "$(GREEN)All services started!$(NC)"
	@echo "$(YELLOW)Use 'make docker-status' to check service health$(NC)"

docker-up-infra: ## Start only infrastructure services
	@echo "$(BLUE)Starting infrastructure services...$(NC)"
	@docker-compose up -d postgres-main postgres-events redis kafka elasticsearch prometheus grafana jaeger minio mailhog
	@echo "$(GREEN)Infrastructure services started!$(NC)"

docker-up-apps: ## Start only application services
	@echo "$(BLUE)Starting application services...$(NC)"
	@docker-compose up -d api-gateway user-management sourcing-management
	@echo "$(GREEN)Application services started!$(NC)"

docker-down: ## Stop all services
	@echo "$(BLUE)Stopping all services...$(NC)"
	@docker-compose down
	@echo "$(GREEN)All services stopped!$(NC)"

docker-down-volumes: ## Stop all services and remove volumes
	@echo "$(RED)Stopping all services and removing volumes...$(NC)"
	@docker-compose down -v
	@echo "$(GREEN)All services stopped and volumes removed!$(NC)"

docker-logs: ## Show logs from all services
	@docker-compose logs -f

docker-logs-app: ## Show logs from application services only
	@docker-compose logs -f api-gateway user-management sourcing-management

docker-status: ## Show status of all services
	@echo "$(BLUE)Service Status:$(NC)"
	@docker-compose ps

docker-restart: ## Restart all services
	@echo "$(BLUE)Restarting all services...$(NC)"
	@docker-compose restart
	@echo "$(GREEN)All services restarted!$(NC)"

##@ Database Operations

db-migrate: ## Run database migrations
	@echo "$(BLUE)Running database migrations...$(NC)"
	@mvn flyway:migrate -pl modules/user-management
	@mvn flyway:migrate -pl modules/sourcing-management
	@mvn flyway:migrate -pl modules/supplier-management
	@echo "$(GREEN)Database migrations completed!$(NC)"

db-clean: ## Clean database (drop all objects)
	@echo "$(RED)Cleaning database...$(NC)"
	@mvn flyway:clean -pl modules/user-management
	@mvn flyway:clean -pl modules/sourcing-management
	@mvn flyway:clean -pl modules/supplier-management
	@echo "$(GREEN)Database cleaned!$(NC)"

db-info: ## Show database migration info
	@echo "$(BLUE)Database migration info:$(NC)"
	@mvn flyway:info -pl modules/user-management
	@mvn flyway:info -pl modules/sourcing-management
	@mvn flyway:info -pl modules/supplier-management

db-reset: db-clean db-migrate ## Reset database (clean + migrate)
	@echo "$(GREEN)Database reset completed!$(NC)"

##@ Quality & Analysis

lint: ## Run code quality checks
	@echo "$(BLUE)Running code quality checks...$(NC)"
	@mvn checkstyle:check
	@echo "$(GREEN)Code quality checks completed!$(NC)"

sonar: ## Run SonarQube analysis
	@echo "$(BLUE)Running SonarQube analysis...$(NC)"
	@mvn sonar:sonar
	@echo "$(GREEN)SonarQube analysis completed!$(NC)"

security-check: ## Run security vulnerability check
	@echo "$(BLUE)Running security vulnerability check...$(NC)"
	@mvn org.owasp:dependency-check-maven:check
	@echo "$(GREEN)Security check completed!$(NC)"

##@ Monitoring & Health

health-check: ## Check health of all services
	@echo "$(BLUE)Checking service health...$(NC)"
	@echo "$(YELLOW)API Gateway:$(NC)"
	@curl -s http://localhost:8081/actuator/health | jq . || echo "$(RED)API Gateway not responding$(NC)"
	@echo "$(YELLOW)User Management:$(NC)"
	@curl -s http://localhost:8082/actuator/health | jq . || echo "$(RED)User Management not responding$(NC)"
	@echo "$(YELLOW)Sourcing Management:$(NC)"
	@curl -s http://localhost:8083/actuator/health | jq . || echo "$(RED)Sourcing Management not responding$(NC)"

open-grafana: ## Open Grafana dashboard
	@echo "$(BLUE)Opening Grafana dashboard...$(NC)"
	@open http://localhost:3000 || xdg-open http://localhost:3000

open-kibana: ## Open Kibana dashboard
	@echo "$(BLUE)Opening Kibana dashboard...$(NC)"
	@open http://localhost:5601 || xdg-open http://localhost:5601

open-kafka-ui: ## Open Kafka UI
	@echo "$(BLUE)Opening Kafka UI...$(NC)"
	@open http://localhost:8080 || xdg-open http://localhost:8080

open-swagger: ## Open Swagger UI
	@echo "$(BLUE)Opening Swagger UI...$(NC)"
	@open http://localhost:8081/swagger-ui.html || xdg-open http://localhost:8081/swagger-ui.html

##@ Cleanup

clean: ## Clean build artifacts
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	@mvn clean
	@echo "$(GREEN)Build artifacts cleaned!$(NC)"

clean-docker: ## Clean Docker resources
	@echo "$(BLUE)Cleaning Docker resources...$(NC)"
	@docker system prune -f
	@docker volume prune -f
	@echo "$(GREEN)Docker resources cleaned!$(NC)"

clean-all: clean clean-docker ## Clean everything
	@echo "$(GREEN)Everything cleaned!$(NC)"

##@ Development Workflows

dev-start: setup-dev docker-up-infra ## Start development environment
	@echo "$(GREEN)Development environment started!$(NC)"
	@echo "$(YELLOW)Infrastructure services are running$(NC)"
	@echo "$(YELLOW)You can now start individual application services for development$(NC)"

dev-stop: docker-down ## Stop development environment
	@echo "$(GREEN)Development environment stopped!$(NC)"

dev-reset: docker-down-volumes docker-up-infra db-migrate ## Reset development environment
	@echo "$(GREEN)Development environment reset!$(NC)"

full-build: clean install test package ## Full build pipeline
	@echo "$(GREEN)Full build pipeline completed!$(NC)"

quick-start: docker-up health-check ## Quick start for demo
	@echo "$(GREEN)Quick start completed!$(NC)"
	@echo "$(YELLOW)Services available at:$(NC)"
	@echo "  - API Gateway: http://localhost:8081"
	@echo "  - Grafana: http://localhost:3000 (admin/admin123)"
	@echo "  - Kibana: http://localhost:5601"
	@echo "  - Kafka UI: http://localhost:8080"
	@echo "  - MinIO Console: http://localhost:9001 (minioadmin/minioadmin123)"

##@ Module-specific Operations

user-service: ## Start only user management service
	@echo "$(BLUE)Starting User Management service...$(NC)"
	@mvn spring-boot:run -pl modules/user-management -Dspring-boot.run.profiles=dev

sourcing-service: ## Start only sourcing management service
	@echo "$(BLUE)Starting Sourcing Management service...$(NC)"
	@mvn spring-boot:run -pl modules/sourcing-management -Dspring-boot.run.profiles=dev

supplier-service: ## Start only supplier management service
	@echo "$(BLUE)Starting Supplier Management service...$(NC)"
	@mvn spring-boot:run -pl modules/supplier-management -Dspring-boot.run.profiles=dev

##@ Utilities

generate-docs: ## Generate project documentation
	@echo "$(BLUE)Generating project documentation...$(NC)"
	@mvn site
	@echo "$(GREEN)Documentation generated at target/site/index.html$(NC)"

create-migration: ## Create new database migration (usage: make create-migration MODULE=user-management NAME=add_user_table)
	@if [ -z "$(MODULE)" ] || [ -z "$(NAME)" ]; then \
		echo "$(RED)Usage: make create-migration MODULE=user-management NAME=add_user_table$(NC)"; \
		exit 1; \
	fi
	@echo "$(BLUE)Creating migration for $(MODULE)...$(NC)"
	@mkdir -p modules/$(MODULE)/src/main/resources/db/migration
	@touch modules/$(MODULE)/src/main/resources/db/migration/V$(shell date +%Y%m%d%H%M%S)__$(NAME).sql
	@echo "$(GREEN)Migration created: modules/$(MODULE)/src/main/resources/db/migration/V$(shell date +%Y%m%d%H%M%S)__$(NAME).sql$(NC)"

show-urls: ## Show all service URLs
	@echo "$(BLUE)Service URLs:$(NC)"
	@echo "$(YELLOW)Application Services:$(NC)"
	@echo "  - API Gateway: http://localhost:8081"
	@echo "  - User Management: http://localhost:8082"
	@echo "  - Sourcing Management: http://localhost:8083"
	@echo ""
	@echo "$(YELLOW)Infrastructure Services:$(NC)"
	@echo "  - PostgreSQL Main: localhost:5432"
	@echo "  - PostgreSQL Events: localhost:5433"
	@echo "  - Redis: localhost:6379"
	@echo "  - Kafka: localhost:9092"
	@echo "  - Elasticsearch: http://localhost:9200"
	@echo ""
	@echo "$(YELLOW)Monitoring & Management:$(NC)"
	@echo "  - Grafana: http://localhost:3000 (admin/admin123)"
	@echo "  - Prometheus: http://localhost:9090"
	@echo "  - Jaeger: http://localhost:16686"
	@echo "  - Kibana: http://localhost:5601"
	@echo "  - Kafka UI: http://localhost:8080"
	@echo "  - MinIO Console: http://localhost:9001 (minioadmin/minioadmin123)"
	@echo "  - MailHog: http://localhost:8025"

