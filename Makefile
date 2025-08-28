# BingoChain Project Makefile

.PHONY: help build up down logs clean test deploy blockchain backend frontend database

# Default target
help:
	@echo "BingoChain - Makefile Commands"
	@echo "=============================="
	@echo ""
	@echo "🐳 Docker Commands:"
	@echo "  build       - Build all Docker images"
	@echo "  up          - Start all services"
	@echo "  down        - Stop all services"
	@echo "  logs        - Show logs from all services"
	@echo "  clean       - Remove all containers and volumes"
	@echo ""
	@echo "🧪 Development Commands:"
	@echo "  test        - Run all tests"
	@echo "  backend     - Run backend in development mode"
	@echo "  frontend    - Run frontend in development mode"
	@echo "  blockchain  - Start local blockchain (Ganache)"
	@echo ""
	@echo "📊 Database Commands:"
	@echo "  db-backup   - Create database backup"
	@echo "  db-restore  - Restore database from backup"
	@echo "  db-reset    - Reset database to initial state"
	@echo ""
	@echo "🚀 Deployment Commands:"
	@echo "  deploy      - Deploy contracts to local network"
	@echo "  deploy-test - Deploy contracts to testnet"
	@echo ""
	@echo "🔧 Utility Commands:"
	@echo "  setup       - Initial project setup"
	@echo "  check       - Check system requirements"
	@echo "  status      - Show services status"

# Docker commands
build:
	@echo "🔨 Building Docker images..."
	docker-compose build

up:
	@echo "🚀 Starting BingoChain services..."
	docker-compose up -d
	@echo "✅ Services started successfully!"
	@echo "Frontend: http://localhost:3000"
	@echo "Backend: http://localhost:3500"
	@echo "PgAdmin: http://localhost:5050"

down:
	@echo "🛑 Stopping BingoChain services..."
	docker-compose down

logs:
	@echo "📋 Showing service logs..."
	docker-compose logs -f

clean:
	@echo "🧹 Cleaning up containers and volumes..."
	docker-compose down -v --remove-orphans
	docker system prune -f

# Development commands
test:
	@echo "🧪 Running all tests..."
	@echo "Testing backend..."
	cd backend && mvn test
	@echo "Testing contracts..."
	cd blockchain && npm test

backend:
	@echo "🖥️ Starting backend in development mode..."
	cd backend && mvn spring-boot:run

frontend:
	@echo "🌐 Starting frontend in development mode..."
	cd frontend && python app.py

blockchain:
	@echo "⛓️ Starting local blockchain..."
	cd blockchain && npm run ganache

# Database commands
db-backup:
	@echo "💾 Creating database backup..."
	./database/scripts/backup.sh

db-restore:
	@echo "🔄 Restoring database..."
	@read -p "Enter backup file path: " backup_file; \
	./database/scripts/restore.sh "$$backup_file"

db-reset:
	@echo "🔄 Resetting database..."
	docker-compose down postgres
	docker volume rm bingochain_postgres_data || true
	docker-compose up -d postgres
	@echo "Database reset completed!"

# Deployment commands
deploy:
	@echo "🚀 Deploying contracts to local network..."
	cd blockchain && npm run migrate

deploy-test:
	@echo "🚀 Deploying contracts to testnet..."
	cd blockchain && npm run deploy:testnet

# Utility commands
setup:
	@echo "🔧 Setting up BingoChain project..."
	@echo "Installing blockchain dependencies..."
	cd blockchain && npm install
	@echo "Creating required directories..."
	mkdir -p database/backups
	mkdir -p backend/logs
	@echo "Setting up Git hooks..."
	git config core.hooksPath .githooks || true
	@echo "✅ Setup completed!"

check:
	@echo "✅ Checking system requirements..."
	@echo "Docker version:"
	@docker --version || echo "❌ Docker not found"
	@echo "Docker Compose version:"
	@docker-compose --version || echo "❌ Docker Compose not found"
	@echo "Node.js version:"
	@node --version || echo "❌ Node.js not found"
	@echo "Java version:"
	@java --version || echo "❌ Java not found"
	@echo "Python version:"
	@python3 --version || echo "❌ Python not found"

status:
	@echo "📊 Services status:"
	@docker-compose ps

# Development shortcuts
dev: up
	@echo "🚀 Development environment started!"
	@echo "Opening logs..."
	make logs

stop: down
	@echo "🛑 Development environment stopped!"

restart: down up
	@echo "🔄 Services restarted!"

# Quick deployment for development
quick-deploy: up deploy
	@echo "🚀 Quick deployment completed!"
	@echo "Contracts deployed to local network"
	@echo "All services are running"

# Production deployment
prod-deploy:
	@echo "🚀 Production deployment..."
	@echo "Building optimized images..."
	docker-compose -f docker-compose.prod.yml build
	@echo "Deploying to production..."
	docker-compose -f docker-compose.prod.yml up -d

# Monitoring
monitor:
	@echo "📊 Opening monitoring dashboard..."
	@echo "Backend health: http://localhost:3500/api/v1/actuator/health"
	@echo "PgAdmin: http://localhost:5050"
	@open http://localhost:3000 2>/dev/null || xdg-open http://localhost:3000 2>/dev/null || echo "Open http://localhost:3000 in your browser"

# Maintenance
backup-all:
	@echo "💾 Creating full system backup..."
	mkdir -p backups/$(shell date +%Y%m%d_%H%M%S)
	make db-backup
	@echo "Backup completed!"

# Security
security-check:
	@echo "🔒 Running security checks..."
	@echo "Checking for vulnerable dependencies..."
	cd backend && mvn dependency-check:check || true
	cd frontend && pip audit || true
	cd blockchain && npm audit || true
