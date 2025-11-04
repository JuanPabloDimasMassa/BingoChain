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

# Función para verificar si un puerto está en uso (múltiples métodos)
check_port() {
    local port=$1
    
    # Método 1: lsof
    if command -v lsof &> /dev/null; then
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            return 0  # Puerto en uso
        fi
    fi
    
    # Método 2: netstat (disponible en muchos sistemas)
    if command -v netstat &> /dev/null; then
        if netstat -tuln 2>/dev/null | grep -q ":$port.*LISTEN"; then
            return 0  # Puerto en uso
        fi
    fi
    
    # Método 3: ss (disponible en sistemas modernos Linux)
    if command -v ss &> /dev/null; then
        if ss -tuln 2>/dev/null | grep -q ":$port.*LISTEN"; then
            return 0  # Puerto en uso
        fi
    fi
    
    # Método 4: Prueba de conexión TCP directa
    if command -v nc &> /dev/null; then
        if nc -z localhost $port >/dev/null 2>&1; then
            return 0  # Puerto en uso
        fi
    elif command -v timeout &> /dev/null; then
        # Usar timeout con bash para probar conexión TCP
        if timeout 1 bash -c "echo > /dev/tcp/localhost/$port" >/dev/null 2>&1; then
            return 0  # Puerto en uso
        fi
    fi
    
    return 1  # Puerto no detectado
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

# Función para verificar conexión real a PostgreSQL
check_postgres_connection() {
    local host=${1:-localhost}
    local port=${2:-5432}
    local user=${3:-postgres}
    local password=${4:-root}
    local database=${5:-bingo_crypto}
    
    # Método 1: psql (más confiable)
    if command -v psql &> /dev/null; then
        if PGPASSWORD=$password psql -h $host -p $port -U $user -d $database -c "SELECT 1;" >/dev/null 2>&1; then
            return 0  # Conexión exitosa
        fi
    fi
    
    # Método 2: Prueba de conexión TCP (verificar que el puerto acepta conexiones)
    if command -v nc &> /dev/null; then
        if nc -z -w 2 $host $port >/dev/null 2>&1; then
            return 0  # Puerto acepta conexiones
        fi
    elif command -v timeout &> /dev/null; then
        if timeout 2 bash -c "echo > /dev/tcp/$host/$port" >/dev/null 2>&1; then
            return 0  # Puerto acepta conexiones
        fi
    fi
    
    return 1  # No se pudo conectar
}

# 1. Verificar PostgreSQL externo y levantar Redis
print_status "Verificando PostgreSQL externo..."

# Primero intentar verificación de puerto (no crítica, solo informativa)
PORT_DETECTED=false
if check_port 5432; then
    print_success "Puerto 5432 detectado en uso"
    PORT_DETECTED=true
else
    print_warning "No se pudo detectar el puerto 5432 en uso (esto puede ser normal)"
fi

# Verificación crítica: conexión real a PostgreSQL
print_status "Verificando conexión a PostgreSQL en localhost:5432..."
if check_postgres_connection localhost 5432 postgres root bingo_crypto; then
    print_success "Conexión a PostgreSQL exitosa!"
    if [ "$PORT_DETECTED" = false ]; then
        print_warning "Nota: El puerto no se detectó pero la conexión funciona correctamente"
    fi
else
    print_error "No se pudo conectar a PostgreSQL"
    print_error "Por favor verifica:"
    print_error "  1. PostgreSQL está corriendo y escuchando en el puerto 5432"
    print_error "  2. La base de datos 'bingo_crypto' existe"
    print_error "  3. Las credenciales son correctas (usuario: postgres, password: root)"
    print_error "  4. El acceso desde localhost está permitido en pg_hba.conf"
    exit 1
fi

# Verificación adicional de la base de datos específica
print_status "Verificando acceso a la base de datos 'bingo_crypto'..."
if command -v psql &> /dev/null; then
    if PGPASSWORD=root psql -h localhost -p 5432 -U postgres -d bingo_crypto -c "SELECT 1;" >/dev/null 2>&1; then
        print_success "Base de datos 'bingo_crypto' accesible!"
    else
        print_error "No se pudo acceder a la base de datos 'bingo_crypto'"
        print_error "La base de datos puede no existir. Verifica o créala con:"
        print_error "  psql -h localhost -p 5432 -U postgres -c \"CREATE DATABASE bingo_crypto;\""
        exit 1
    fi
else
    print_warning "psql no está disponible, no se puede verificar el acceso a la base de datos específica"
    print_warning "Asegúrate de que la base de datos 'bingo_crypto' existe"
fi

# Levantar solo Redis
print_status "Levantando Redis..."
docker-compose up -d redis

# Esperar a que Redis esté listo
print_status "Esperando a que Redis esté listo..."
sleep 3

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

# Verificar si Ganache CLI ya está corriendo
if check_port 8545; then
    print_success "Ganache CLI ya está corriendo en el puerto 8545"
else
    print_status "Iniciando Ganache CLI..."
    cd blockchain
    npx ganache-cli -p 8545 -m "candy maple cake sugar pudding cream honey rich smooth crumble sweet treat" &
    GANACHE_PID=$!
    cd ..
    
    # Esperar a que Ganache esté listo
    wait_for_service "http://localhost:8545" "Ganache CLI" || exit 1
fi

# Compilar y desplegar contratos
print_status "Compilando contratos inteligentes..."
cd blockchain
npx truffle compile

print_status "Desplegando contratos..."
npx truffle migrate --network development

# Obtener la dirección del contrato desplegado
DEPLOYMENT_FILE="deployments/development.json"

if [ -f "$DEPLOYMENT_FILE" ]; then
    CONTRACT_ADDRESS=$(cat "$DEPLOYMENT_FILE" | grep -o '"contractAddress"[[:space:]]*:[[:space:]]*"[^"]*"' | cut -d'"' -f4)
    if [ -z "$CONTRACT_ADDRESS" ]; then
        # Método alternativo usando jq si está disponible
        if command -v jq &> /dev/null; then
            CONTRACT_ADDRESS=$(cat "$DEPLOYMENT_FILE" | jq -r '.contractAddress')
        else
            # Método manual más robusto
            CONTRACT_ADDRESS=$(cat "$DEPLOYMENT_FILE" | sed -n 's/.*"contractAddress"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
        fi
    fi
    print_success "Contrato desplegado en: $CONTRACT_ADDRESS"
else
    print_error "No se encontró el archivo de deployment: $DEPLOYMENT_FILE"
    print_warning "Usando dirección por defecto..."
    CONTRACT_ADDRESS="0x345cA3e014Aaf5dcA488057592ee47305D9B3e10"
fi

cd ..

# Actualizar configuración del backend con la dirección del contrato
print_status "Actualizando configuración del backend..."
# Actualizar la dirección del contrato
sed -i "s/address: \".*\"/address: \"$CONTRACT_ADDRESS\"/" backend/src/main/resources/application.yml
# Asegurar configuración para Ganache CLI
sed -i "s|network-url: .*|network-url: http://localhost:8545|" backend/src/main/resources/application.yml
sed -i "s/chain-id: .*/chain-id: 1337/" backend/src/main/resources/application.yml

# Actualizar contract address en archivos HTML
print_status "Actualizando contract address en frontend..."
sed -i "s/CONTRACT_ADDRESS: '0x[^']*'/CONTRACT_ADDRESS: '$CONTRACT_ADDRESS'/" main.html
sed -i "s/CONTRACT_ADDRESS: '0x[^']*'/CONTRACT_ADDRESS: '$CONTRACT_ADDRESS'/" demo.html  
sed -i "s/CONTRACT_ADDRESS: '0x[^']*'/CONTRACT_ADDRESS: '$CONTRACT_ADDRESS'/" mis-boletos.html
# Actualizar también la dirección mostrada en el HTML
sed -i "s/<code>0x[^<]*<\/code>/<code>$CONTRACT_ADDRESS<\/code>/" main.html

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
echo "  🗄️  PostgreSQL:     localhost:5432 (externo)"
echo "  🔴 Redis:          localhost:6379"
echo "  ⛓️  Ganache CLI:    localhost:8545"
echo "  🔧 Backend API:    http://localhost:3500/api/v1"
echo "  📁 Frontend Web:   http://localhost:8080"
echo ""
print_success "URLs principales:"
echo "  🏠 Página de Inicio:    http://localhost:8080/"
echo "  📊 Panel Principal:     http://localhost:8080/main.html"
echo "  🎮 Demo Principal:      http://localhost:8080/demo.html"
echo "  🎫 Mis Boletos:         http://localhost:8080/mis-boletos.html"
echo "  🔐 Panel Admin:         http://localhost:8080/admin.html"
echo ""
print_success "URLs de desarrollo:"
echo "  📚 Swagger UI:         http://localhost:3500/api/v1/swagger-ui/index.html"
echo "  🔍 API Docs:           http://localhost:3500/api/v1/api-docs"
echo "  🏥 Health Check:       http://localhost:3500/api/v1/health"
echo ""
print_success "Información del contrato:"
echo "  📍 Dirección: $CONTRACT_ADDRESS"
echo "  🌐 Red: Ganache CLI (Chain ID: 1337)"
echo "  🔗 URL: http://localhost:8545"
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
