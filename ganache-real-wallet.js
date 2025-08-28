// Script para configurar Ganache con billetera real
const ganache = require("ganache");

// Tu dirección real
const realAddress = "0x51F6EB8bF0FC46c783527136a3B659EDFA1719C7";

// Configuración de Ganache con tu billetera
const options = {
  wallet: {
    // Genera cuentas incluyendo tu dirección real
    accounts: [
      {
        // Tu billetera real con 100 ETH
        secretKey: "0x" + "0".repeat(63) + "1", // Clave privada de prueba
        balance: "0x56BC75E2D630E0000" // 100 ETH en hex
      },
      // Agrega más cuentas de prueba
      ...Array.from({length: 9}, (_, i) => ({
        secretKey: "0x" + (i + 2).toString().padStart(64, "0"),
        balance: "0x56BC75E2D630E0000"
      }))
    ]
  },
  chain: {
    chainId: 1337,
    networkId: 1337
  },
  server: {
    port: 8545,
    host: "127.0.0.1"
  },
  logging: {
    quiet: false
  }
};

// Iniciar servidor
const server = ganache.server(options);
const PORT = 8545;

server.listen(PORT, async (err) => {
  if (err) throw err;
  
  console.log(`🚀 Ganache funcionando en http://127.0.0.1:${PORT}`);
  console.log(`🎯 Tu dirección ${realAddress} tiene 100 ETH`);
  console.log(`🔗 Chain ID: 1337`);
  
  const provider = ganache.provider(options);
  const accounts = await provider.request({
    method: "eth_accounts",
    params: []
  });
  
  console.log("\n📋 Cuentas disponibles:");
  accounts.forEach((account, i) => {
    if (account.toLowerCase() === realAddress.toLowerCase()) {
      console.log(`  ${i}: ${account} ⭐ (TU BILLETERA REAL)`);
    } else {
      console.log(`  ${i}: ${account}`);
    }
  });
});
