#!/bin/bash

# Script para obtener las cuentas de Ganache con sus balances

echo "🔍 Obteniendo cuentas de Ganache..."
echo "=================================="
echo ""

# Verificar que Ganache esté corriendo
if ! curl -s http://localhost:8545 > /dev/null 2>&1; then
    echo "❌ Error: Ganache no está corriendo en http://localhost:8545"
    echo "Por favor inicia Ganache con: ./start-bingochain.sh"
    exit 1
fi

# Función para convertir hex a decimal (ETH)
hex_to_eth() {
    local hex=$1
    local wei=$(echo "ibase=16; $(echo $hex | tr '[:lower:]' '[:upper:]' | sed 's/^0X//')" | bc 2>/dev/null)
    if [ -z "$wei" ]; then
        echo "N/A"
    else
        # Convertir wei a ETH (1 ETH = 10^18 wei)
        echo "scale=4; $wei / 1000000000000000000" | bc 2>/dev/null
    fi
}

# Obtener lista de cuentas
ACCOUNTS=$(curl -s -X POST -H "Content-Type: application/json" \
    --data '{"jsonrpc":"2.0","method":"eth_accounts","params":[],"id":1}' \
    http://localhost:8545 | grep -o '"result":\[.*\]' | sed 's/"result":\[//' | sed 's/\]//' | tr ',' '\n' | tr -d ' "')

# Mnemonic usado en Ganache (del script start-bingochain.sh)
MNEMONIC="candy maple cake sugar pudding cream honey rich smooth crumble sweet treat"

echo "📝 Mnemonic usado:"
echo "$MNEMONIC"
echo ""
echo "🔑 Cuentas disponibles en Ganache:"
echo "=================================="
echo ""

counter=0
for account in $ACCOUNTS; do
    # Obtener balance
    balance_hex=$(curl -s -X POST -H "Content-Type: application/json" \
        --data "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBalance\",\"params\":[\"$account\",\"latest\"],\"id\":1}" \
        http://localhost:8545 | grep -o '"result":"[^"]*"' | cut -d'"' -f4)
    
    # Convertir balance a ETH (formato simple)
    balance_eth=$(echo "ibase=16; $(echo $balance_hex | tr '[:lower:]' '[:upper:]' | sed 's/^0X//') / DE0B6B3A7640000" | bc 2>/dev/null)
    
    if [ -z "$balance_eth" ]; then
        balance_eth="1000"  # Por defecto Ganache da 1000 ETH
    fi
    
    echo "Cuenta $counter:"
    echo "  Dirección: $account"
    echo "  Balance: $balance_eth ETH"
    
    # Generar clave privada desde el mnemonic usando Node.js si está disponible
    if command -v node &> /dev/null; then
        private_key=$(node -e "
            const { ethers } = require('ethers');
            const mnemonic = '$MNEMONIC';
            const wallet = ethers.Wallet.fromMnemonic(mnemonic, \"m/44'/60'/0'/0/$counter\");
            console.log(wallet.privateKey);
        " 2>/dev/null)
        
        if [ ! -z "$private_key" ]; then
            echo "  🔐 Clave Privada: $private_key"
        fi
    fi
    
    echo ""
    counter=$((counter + 1))
done

echo "=================================="
echo ""
echo "💡 Para importar en MetaMask:"
echo "1. Abre MetaMask"
echo "2. Selecciona la red 'Ganache Local' (localhost:8545, Chain ID: 1337)"
echo "3. Haz clic en el icono de cuenta → 'Importar cuenta'"
echo "4. Pega la clave privada de cualquier cuenta de arriba"
echo "5. ¡Listo! Tendrás 1000 ETH para probar"
echo ""

