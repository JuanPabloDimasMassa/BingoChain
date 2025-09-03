#!/bin/bash

# 🎰 BingoChain - Script de Inicio Automatizado
# Este script levanta todos los servicios necesarios para BingoChain

set -e  # Salir si cualquier comando falla

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

# Función para verificar si un puerto está en uso
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Puerto en uso
    else
        return 1  # Puerto libre
    fi
}

# Función para esperar a que un servicio esté listo
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    print_status "Esperando a que $service_name esté listo..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" >/dev/null 2>&1; then
            print_success "$service_name está listo!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name no respondió después de $max_attempts intentos"
    return 1
}

# Función para limpiar procesos al salir
cleanup() {
    print_status "Limpiando procesos..."
    # Matar procesos en background
    jobs -p | xargs -r kill
    exit 0
}

# Configurar trap para limpieza
trap cleanup SIGINT SIGTERM

echo "🎰 BingoChain - Iniciando Sistema Completo"
echo "=========================================="

# Verificar que estamos en el directorio correcto
if [ ! -f "docker-compose.yml" ]; then
    print_error "Este script debe ejecutarse desde el directorio raíz de BingoChain"
    exit 1
fi

# Verificar dependencias
print_status "Verificando dependencias..."

# Verificar Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker no está instalado"
    exit 1
fi

# Verificar Docker Compose
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose no está instalado"
    exit 1
fi

# Verificar Java
if ! command -v java &> /dev/null; then
    print_error "Java no está instalado"
    exit 1
fi

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven no está instalado"
    exit 1
fi

# Verificar Node.js
if ! command -v node &> /dev/null; then
    print_error "Node.js no está instalado"
    exit 1
fi

# Verificar Python
if ! command -v python3 &> /dev/null; then
    print_error "Python3 no está instalado"
    exit 1
fi

print_success "Todas las dependencias están instaladas"

# 1. Levantar Base de Datos y Redis
print_status "Levantando PostgreSQL y Redis..."
docker-compose up -d postgres redis

# Esperar a que los servicios estén listos
print_status "Esperando a que los servicios estén listos..."
sleep 5

# Verificar PostgreSQL
if docker ps | grep -q "bingochain-postgres.*healthy"; then
    print_success "PostgreSQL está listo!"
else
    print_warning "PostgreSQL puede no estar completamente listo, pero continuando..."
fi

# Verificar Redis
if docker ps | grep -q "bingochain-redis"; then
    print_success "Redis está listo!"
else
    print_warning "Redis puede no estar completamente listo, pero continuando..."
fi

# 2. Configurar Blockchain
print_status "Configurando blockchain..."

# Instalar dependencias de Node.js si es necesario
if [ ! -d "blockchain/node_modules" ]; then
    print_status "Instalando dependencias de blockchain..."
    cd blockchain
    npm install
    cd ..
fi

# Verificar si Ganache ya está corriendo
if check_port 8545; then
    print_warning "Ganache ya está corriendo en el puerto 8545"
else
    print_status "Iniciando Ganache..."
    cd blockchain
    npx ganache-cli -p 8545 -m "candy maple cake sugar pudding cream honey rich smooth crumble sweet treat" &
    GANACHE_PID=$!
    cd ..
    
    # Esperar a que Ganache esté listo
    wait_for_service "http://localhost:8545" "Ganache" || exit 1
fi

# Compilar y desplegar contratos
print_status "Compilando contratos inteligentes..."
cd blockchain
npx truffle compile

print_status "Desplegando contratos..."
npx truffle migrate --network development

# Obtener la dirección del contrato desplegado
CONTRACT_ADDRESS=$(cat deployments/development.json | grep -o '"contractAddress":"[^"]*"' | cut -d'"' -f4)
print_success "Contrato desplegado en: $CONTRACT_ADDRESS"

cd ..

# Actualizar configuración del backend con la dirección del contrato
print_status "Actualizando configuración del backend..."
sed -i "s/address: \"\"/address: \"$CONTRACT_ADDRESS\"/" backend/src/main/resources/application.yml

# 3. Levantar Backend Java
print_status "Iniciando backend Java Spring Boot..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

# Esperar a que el backend esté listo
wait_for_service "http://localhost:3500/api/v1/actuator/health" "Backend API" || exit 1

# 4. Configurar Frontend (Servidor HTTP Estático)
print_status "Configurando frontend estático..."
print_success "Frontend configurado - usando servidor HTTP estático"

# 5. Levantar Servidor HTTP para archivos estáticos
print_status "Iniciando servidor HTTP para archivos estáticos..."
python3 -m http.server 8080 &
HTTP_PID=$!

# Esperar a que el servidor HTTP esté listo
wait_for_service "http://localhost:8080" "Servidor HTTP" || exit 1

# 6. Mostrar información de acceso
echo ""
echo "🎉 ¡BingoChain está completamente operativo!"
echo "============================================="
echo ""
print_success "Servicios disponibles:"
echo "  🗄️  PostgreSQL:     localhost:5434"
echo "  🔴 Redis:          localhost:6379"
echo "  ⛓️  Ganache:        localhost:8545"
echo "  🔧 Backend API:    http://localhost:3500/api/v1"
echo "  📁 Frontend Web:   http://localhost:8080"
echo ""
print_success "URLs principales:"
echo "  🏠 Página de Inicio:    http://localhost:8080/"
echo "  🎮 Demo Principal:      http://localhost:8080/demo.html"
echo "  🎫 Mis Boletos:         http://localhost:8080/mis-boletos.html"
echo "  ⚙️  Configurar Sepolia:  http://localhost:8080/configurar-sepolia.html"
echo ""
print_success "Información del contrato:"
echo "  📍 Dirección: $CONTRACT_ADDRESS"
echo "  🌐 Red: Localhost (Chain ID: 1337)"
echo ""
print_warning "Para detener todos los servicios, presiona Ctrl+C"
echo ""

# Mantener el script corriendo
while true; do
    sleep 10
    
    # Verificar que los servicios principales sigan corriendo
    if ! kill -0 $BACKEND_PID 2>/dev/null; then
        print_error "Backend se detuvo inesperadamente"
        cleanup
    fi
    
    if ! kill -0 $HTTP_PID 2>/dev/null; then
        print_error "Servidor HTTP se detuvo inesperadamente"
        cleanup
    fi
done
