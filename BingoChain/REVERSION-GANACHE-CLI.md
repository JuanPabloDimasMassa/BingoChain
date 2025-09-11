# 🎰 Reversión a Ganache CLI Únicamente

## ✅ Cambios Realizados

### 1. **Eliminación de Ganache GUI**
- ✅ Removido soporte para Ganache GUI del script `start-bingochain.sh`
- ✅ Eliminado archivo `start-ganache-gui.sh`
- ✅ Eliminada documentación específica de Ganache GUI (`README-GANACHE.md`)

### 2. **Simplificación del Script Principal**
- ✅ `start-bingochain.sh` ahora solo maneja Ganache CLI
- ✅ Configuración fija para puerto 8545 y Chain ID 1337
- ✅ Eliminada lógica de detección automática entre GUI/CLI

### 3. **Configuración de Red**
- ✅ Truffle configurado solo para red `development` (puerto 8545)
- ✅ Eliminada configuración de red `ganache` del `truffle-config.js`
- ✅ Backend configurado para `http://localhost:8545` y Chain ID 1337

### 4. **Limpieza de Archivos**
- ✅ Removidas referencias a Ganache GUI en `docker-compose.yml`
- ✅ Simplificados mensajes de salida del script
- ✅ Eliminados archivos de documentación específicos de GUI

## 🚀 Configuración Final

### Ganache CLI
- **Puerto**: 8545
- **Chain ID**: 1337
- **Mnemonic**: `candy maple cake sugar pudding cream honey rich smooth crumble sweet treat`
- **Cuentas**: 10 (con 1000 ETH cada una)

### Servicios Disponibles
- **🗄️ PostgreSQL**: `localhost:5434`
- **🔴 Redis**: `localhost:6379`
- **⛓️ Ganache CLI**: `localhost:8545`
- **🔧 Backend API**: `http://localhost:3500/api/v1`
- **📁 Frontend Web**: `http://localhost:8080`

## 🎯 Cómo Usar

### Inicio Simple
```bash
./start-bingochain.sh
```

El script ahora:
1. Verifica dependencias
2. Levanta PostgreSQL y Redis con Docker
3. Inicia Ganache CLI automáticamente
4. Compila y despliega contratos inteligentes
5. Configura el backend con la dirección del contrato
6. Inicia el backend Java Spring Boot
7. Inicia el servidor HTTP para el frontend

### Comandos de Desarrollo

```bash
# Compilar contratos
cd blockchain && npx truffle compile

# Desplegar contratos
cd blockchain && npx truffle migrate --network development

# Consola de Truffle
cd blockchain && npx truffle console --network development

# Ver cuentas disponibles
cd blockchain && npx truffle console --network development
truffle(development)> web3.eth.getAccounts()
```

## 💡 Beneficios de la Simplificación

1. **🎯 Simplicidad**: Un solo tipo de Ganache, menos configuraciones
2. **⚡ Velocidad**: Menos verificaciones y configuraciones dinámicas
3. **🔧 Mantenimiento**: Código más simple y fácil de mantener
4. **📦 Menos Dependencias**: No necesita Ganache GUI instalado
5. **🚀 Inicio Rápido**: Un solo comando para todo

## 🛠️ Configuración de Red

La aplicación ahora está configurada exclusivamente para:

- **Red**: Development (Ganache CLI)
- **URL**: http://localhost:8545
- **Chain ID**: 1337
- **Network ID**: * (cualquiera)
- **Gas Limit**: 6,721,975
- **Gas Price**: 20 Gwei

## 📊 Archivos Modificados

### `start-bingochain.sh`
- Simplificado para solo Ganache CLI
- Eliminada detección automática de GUI/CLI
- Configuración fija de red

### `blockchain/truffle-config.js`
- Eliminada configuración de red `ganache`
- Solo mantiene red `development`

### `docker-compose.yml`
- Limpieza de comentarios de Ganache
- Simplificación de configuración

## ✨ Resultado

BingoChain ahora funciona exclusivamente con Ganache CLI, proporcionando una experiencia más simple y directa para el desarrollo. El sistema es más ligero y fácil de usar, perfecto para desarrollo y testing sin necesidad de interfaz gráfica.
