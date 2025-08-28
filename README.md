# 🎰 BingoChain - Lotería Blockchain NFT

**Una plataforma de lotería descentralizada que genera boletos como NFTs**

## 🌟 Características

- 🎫 **Boletos NFT**: Cada boleto es un token ERC-721 único
- 🔗 **Blockchain**: Smart contracts en Solidity
- 🎯 **Mecánica Simple**: Elige 6 números del 1-100
- 📅 **Sorteos Semanales**: Ventas dominicales, sorteos de lunes a sábado
- 💳 **MetaMask**: Integración completa con billeteras Web3
- 🆓 **Testing Gratis**: Boletos gratuitos para pruebas

## 🏗️ Arquitectura

```
BingoChain/
├── 🔧 backend/          # Java Spring Boot + JPA
├── 🎨 frontend/         # Python Flask
├── ⛓️  blockchain/       # Solidity + Truffle
├── 🗄️  database/        # PostgreSQL
└── 🐳 docker/           # Docker Compose
```

## 🚀 Tecnologías

### Backend
- **Java 17** + Spring Boot 3.2
- **JPA/Hibernate** para ORM
- **PostgreSQL** como base de datos
- **Web3j** para integración blockchain
- **Redis** para caché y sesiones

### Frontend
- **Python 3.11** + Flask
- **Web3.js** para interacción blockchain
- **Bootstrap 5** para UI
- **Socket.IO** para tiempo real

### Blockchain
- **Solidity 0.8.19**
- **Truffle** para desarrollo
- **OpenZeppelin** para contratos seguros
- **Ganache** para testing local

### DevOps
- **Docker** + Docker Compose
- **Maven** para gestión Java
- **pip** para dependencias Python

## 🎮 Mecánica del Juego

### 📋 Reglas
1. **Sin límite** de jugadores
2. **6 números únicos** del 1-100 por boleto
3. **Ilimitados boletos** por jugador
4. **Ventas**: Domingo 12:30 - Lunes 12:00
5. **Sorteos**: 1 número diario a las 12:30 (6 días)

### 🏆 Premios
- **6 aciertos**: 50% del pozo
- **5 aciertos**: 30% del pozo
- **4 aciertos**: 15% del pozo
- **3 aciertos**: 5% del pozo

## 🛠️ Instalación

### 1. Clonar Repositorio
```bash
git clone https://github.com/tu-usuario/BingoChain.git
cd BingoChain
```

### 2. Levantar Servicios
```bash
# Iniciar PostgreSQL y Redis
docker-compose up -d postgres redis

# Terminal 1: Backend Java
cd backend
mvn spring-boot:run

# Terminal 2: Frontend Python
cd frontend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python app.py

# Terminal 3: Blockchain Local
npx ganache-cli -p 8545 -m "candy maple cake sugar pudding cream honey rich smooth crumble sweet treat"

# Terminal 4: Deploy Contratos
cd blockchain
npm install
npx truffle migrate

# Terminal 5: Servidor HTTP
python3 -m http.server 8080
```

### 3. Configurar MetaMask
- **Red**: Localhost 8545
- **Chain ID**: 1337
- **Moneda**: ETH
- **Importar cuenta** con clave:
  ```
  0xc87509a1c067bbde78bef793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3
  ```

## 🎯 Uso Rápido

### 🔗 Acceso Directo
1. **Demo Principal**: `http://localhost:8080/BingoChain/demo.html`
2. **Mis Boletos**: `http://localhost:8080/BingoChain/mis-boletos.html`
3. **Backend API**: `http://localhost:3500`
4. **Frontend Web**: `http://localhost:3000`

### 🎫 Comprar Boleto NFT
1. Conectar MetaMask
2. Seleccionar 6 números
3. Confirmar transacción
4. ¡Recibir NFT en wallet!

## 📊 Endpoints API

### 🎰 Sorteos
```bash
GET  /api/lotteries          # Lista todos los sorteos
GET  /api/lotteries/current  # Sorteo actual
POST /api/lotteries          # Crear sorteo
```

### 🎫 Boletos
```bash
GET  /api/tickets            # Todos los boletos
GET  /api/tickets/player/{address}  # Boletos de jugador
POST /api/tickets            # Registrar boleto
```

## 🔧 Configuración

### 🐘 PostgreSQL
```yaml
Host: localhost:5434
Database: bingo_crypto
User: postgres
Password: root
```

### ⛓️ Smart Contract
```javascript
// Ganache Local
Contract: 0xAa588d3737B611baFD7bD713445b314BD453a5C8
Network: localhost:8545
Chain ID: 1337
```

## 🧪 Testing

### 🎮 Demo Interactivo
```bash
# Abrir demo
xdg-open http://localhost:8080/BingoChain/demo.html

# Características del demo:
✅ Detección automática MetaMask
✅ Conexión de wallet
✅ Selección de números
✅ Compra de boletos NFT GRATIS
✅ Verificación de transacciones
```

### 🔍 Verificar Boletos
```bash
# Ver boletos comprados
xdg-open http://localhost:8080/BingoChain/mis-boletos.html
```

## 📁 Estructura del Proyecto

```
BingoChain/
├── 📱 demo.html                    # Demo principal
├── 🎫 mis-boletos.html            # Ver boletos NFT
├── ⚙️  configurar-sepolia.html     # Setup testnet
├── 🐳 docker-compose.yml          # Servicios Docker
│
├── 🔧 backend/
│   ├── 📋 pom.xml                 # Dependencias Maven
│   ├── ⚙️  application.yml        # Configuración Spring
│   └── 📂 src/main/java/com/bingochain/
│       ├── 🎯 controller/         # REST Controllers
│       ├── 📊 model/              # Entidades JPA
│       ├── 🗄️  repository/        # Repositorios
│       └── 🔧 service/            # Lógica de negocio
│
├── 🎨 frontend/
│   ├── 📋 requirements.txt        # Dependencias Python
│   ├── 🐍 app.py                  # Aplicación Flask
│   ├── 📂 templates/              # Plantillas HTML
│   └── 📂 static/                 # CSS/JS
│
├── ⛓️  blockchain/
│   ├── 📋 package.json            # Dependencias Node
│   ├── ⚙️  truffle-config.js      # Configuración Truffle
│   ├── 📂 contracts/
│   │   └── 🎰 CryptoBingo.sol     # Smart Contract principal
│   └── 📂 migrations/             # Scripts de deploy
│
└── 🗄️  database/
    └── 📂 init/
        └── 📄 01-init-database.sql # Schema inicial
```

## 🔐 Seguridad

### ⚠️ Nunca Commitear:
- ❌ Claves privadas
- ❌ Mnemonics de wallets
- ❌ Variables de entorno
- ❌ Credenciales de base de datos

### ✅ Buenas Prácticas:
- ✅ Usar `.env` para secretos
- ✅ Validar inputs en smart contracts
- ✅ Implementar pausas de emergencia
- ✅ Auditar contratos antes de mainnet

## 🚨 Troubleshooting

### MetaMask no detectado
```bash
# Usar servidor HTTP, NO file://
python3 -m http.server 8080
# Acceder: http://localhost:8080/BingoChain/demo.html
```

### Error "revert"
```bash
# Verificar:
✅ Red correcta (Ganache)
✅ Contrato desplegado
✅ Números válidos (1-100)
✅ 6 números únicos
```

### Docker port busy
```bash
# Verificar puertos
docker-compose down
sudo netstat -tulpn | grep :5434
```

## 🤝 Contribuir

1. Fork el proyecto
2. Crear branch (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push branch (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver [LICENSE](LICENSE) para detalles.

## 👨‍💻 Autor

**Tu Nombre** - [GitHub](https://github.com/tu-usuario)

---

### 🎉 ¡Disfruta jugando BingoChain!

**¿Problemas?** Abre un [Issue](https://github.com/tu-usuario/BingoChain/issues)
**¿Sugerencias?** Envía un [Pull Request](https://github.com/tu-usuario/BingoChain/pulls)