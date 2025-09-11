# 🦊 Configurar MetaMask para BingoChain

## 📋 Guía de Configuración

### 1. **Instalar MetaMask**
Si no tienes MetaMask instalado:
- Ve a https://metamask.io/download/
- Instala la extensión para tu navegador
- Crea una nueva wallet o importa una existente

### 2. **Agregar Red Ganache Local**

BingoChain funciona con Ganache CLI en tu máquina local. Necesitas agregar esta red a MetaMask:

#### Configuración Manual:
1. Abre MetaMask
2. Haz clic en el selector de red (arriba)
3. Selecciona "Agregar red" o "Add Network"
4. Selecciona "Agregar una red manualmente"
5. Completa los siguientes datos:

```
Nombre de la red: Ganache Local
Nueva URL de RPC: http://localhost:8545
ID de cadena: 1337
Símbolo de moneda: ETH
URL del explorador de bloques: (dejar vacío)
```

#### Configuración Automática:
- Al hacer clic en "Conectar Wallet" en BingoChain, se agregará automáticamente la red si no existe

### 3. **Importar Cuentas de Ganache**

Ganache CLI genera 10 cuentas con 1000 ETH cada una. Puedes importar estas cuentas a MetaMask:

#### Claves Privadas por Defecto:
```
Cuenta 0: 0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d
Cuenta 1: 0x6cbed15c793ce57650b9877cf6fa156fbef513c4e6134f022a85b1ffdd59b2a1
Cuenta 2: 0x6370fd033278c143179d81c5526140625662b8daa446c22ee2d73db3707e620c
Cuenta 3: 0x646f1ce2fdad0e6deeeb5c7e8e5543bdde65e86029e2fd9fc169899c440a7913
Cuenta 4: 0xadd53f9a7e588d003326d1cbf9e4a43c061aadd9bc938c843a79e7b4fd2ad743
```

#### Pasos para Importar:
1. En MetaMask, haz clic en el icono de cuenta (círculo arriba a la derecha)
2. Selecciona "Importar cuenta"
3. Pega una de las claves privadas de arriba
4. Haz clic en "Importar"

### 4. **Verificar Configuración**

Una vez configurado, deberías ver:
- ✅ Red: "Ganache Local" en MetaMask
- ✅ Balance: 1000 ETH (si importaste una cuenta)
- ✅ Dirección: Comenzando con 0x627306... (primera cuenta)

## 🚀 Usar BingoChain

### Pasos para Conectar:
1. Asegúrate de que Ganache CLI esté corriendo (`./start-bingochain.sh`)
2. Ve a http://localhost:8080/main.html
3. Haz clic en "🔗 Conectar Wallet"
4. MetaMask se abrirá automáticamente
5. Selecciona la cuenta que quieres usar
6. Autoriza la conexión

### Funcionalidades:
- **Ver Sorteos**: Lista de sorteos disponibles
- **Comprar Boletos**: Usa ETH para comprar boletos NFT
- **Ver Mis Boletos**: Revisa tus boletos comprados
- **Cambio Automático de Red**: BingoChain cambiará a la red correcta automáticamente

## 🔧 Solución de Problemas

### Error: "MetaMask no está instalado"
- Instala MetaMask desde https://metamask.io/download/
- Reinicia el navegador después de la instalación

### Error: "Red incorrecta"
- Verifica que estés en la red "Ganache Local"
- BingoChain intentará cambiar automáticamente la red

### Error: "No se encontraron cuentas"
- Asegúrate de haber desbloqueado MetaMask
- Verifica que tengas al menos una cuenta en MetaMask

### Error: "Ganache no responde"
- Verifica que `./start-bingochain.sh` esté corriendo
- Confirma que Ganache CLI esté en el puerto 8545:
  ```bash
  curl -X POST -H "Content-Type: application/json" \
    -d '{"jsonrpc":"2.0","method":"web3_clientVersion","params":[],"id":1}' \
    http://localhost:8545
  ```

### Sin Balance ETH
- Importa una de las cuentas predeterminadas de Ganache (ver claves privadas arriba)
- O transfiere ETH desde una cuenta con fondos

## 📱 Direcciones de Cuentas Ganache

Para referencia, estas son las direcciones públicas de las cuentas por defecto:

```
0x627306090abaB3A6e1400e9345bC60c78a8BEf57  (1000 ETH)
0xf17f52151EbEF6C7334FAD080c5704D77216b732  (1000 ETH)
0xC5fdf4076b8F3A5357c5E395ab970B5B54098Fef  (1000 ETH)
0x821aEa9a577a9b44299B9c15c88cf3087F3b5544  (1000 ETH)
0x0d1d4e623D10F9FBA5Db95830F7d3839406C6AF2  (1000 ETH)
0x2932b7A2355D6fecc4b5c0B6BD44cC31df247a2e  (1000 ETH)
0x2191eF87E392377ec08E7c08Eb105Ef5448eCED5  (1000 ETH)
0x0F4F2Ac550A1b4e2280d04c21cEa7EBD822934b5  (1000 ETH)
0x6330A553Fc93768F612722BB8c2eC78aC90B3bbc  (1000 ETH)
0x5AEDA56215b167893e80B4fE645BA6d5Bab767DE  (1000 ETH)
```

## ⚠️ Importante

- **Solo para Desarrollo**: Esta configuración es solo para desarrollo local
- **No usar en Producción**: Nunca uses estas claves privadas en redes principales
- **Reiniciar Ganache**: Si reinicias Ganache, puede que necesites reimportar las cuentas

¡Ahora estás listo para usar BingoChain con MetaMask real! 🎰✨
