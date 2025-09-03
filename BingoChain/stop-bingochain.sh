#!/bin/bash

# 🎰 BingoChain - Script de Parada
# Este script detiene todos los servicios de BingoChain

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir mensajes con colores
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

echo "🛑 BingoChain - Deteniendo Sistema"
echo "=================================="

# Función para matar procesos por puerto
kill_by_port() {
    local port=$1
    local service_name=$2
    
    local pids=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pids" ]; then
        print_status "Deteniendo $service_name (puerto $port)..."
        echo $pids | xargs kill -TERM 2>/dev/null
        sleep 2
        
        # Si aún están corriendo, forzar terminación
        local remaining_pids=$(lsof -ti:$port 2>/dev/null)
        if [ -n "$remaining_pids" ]; then
            print_warning "Forzando terminación de $service_name..."
            echo $remaining_pids | xargs kill -KILL 2>/dev/null
        fi
        
        print_success "$service_name detenido"
    else
        print_warning "$service_name no estaba corriendo"
    fi
}

# Detener servicios por puerto
kill_by_port 8080 "Servidor HTTP"
kill_by_port 3500 "Backend Spring Boot"
kill_by_port 8545 "Ganache"

# Detener contenedores Docker
print_status "Deteniendo contenedores Docker..."
docker-compose down

# Limpiar procesos de Node.js relacionados con BingoChain
print_status "Limpiando procesos de Node.js..."
pkill -f "ganache-cli" 2>/dev/null || true
pkill -f "truffle" 2>/dev/null || true

# Limpiar procesos de Python relacionados con BingoChain
print_status "Limpiando procesos de Python..."
pkill -f "python.*http.server.*8080" 2>/dev/null || true

# Limpiar procesos de Java relacionados con BingoChain
print_status "Limpiando procesos de Java..."
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "BingoChainApplication" 2>/dev/null || true

print_success "Todos los servicios de BingoChain han sido detenidos"
echo ""
print_status "Para volver a iniciar el sistema, ejecuta:"
echo "  ./start-bingochain.sh"
