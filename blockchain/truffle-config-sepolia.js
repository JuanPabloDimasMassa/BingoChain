const HDWalletProvider = require('@truffle/hdwallet-provider');

// IMPORTANTE: Reemplaza con tu mnemonic real o usa variables de entorno
const mnemonic = process.env.MNEMONIC || "candy maple cake sugar pudding cream honey rich smooth crumble sweet treat";

module.exports = {
  networks: {
    // Red local Ganache
    development: {
      host: "127.0.0.1",
      port: 8545,
      network_id: "*",
      gas: 6721975,
      gasPrice: 20000000000,
    },
    
    // Red de test Sepolia
    sepolia: {
      provider: () => new HDWalletProvider(
        mnemonic,
        `https://sepolia.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161` // Infura endpoint público
      ),
      network_id: 11155111, // Sepolia chain ID
      gas: 4000000,
      gasPrice: 10000000000, // 10 gwei
      confirmations: 2,
      timeoutBlocks: 200,
      skipDryRun: true
    }
  },
  
  mocha: {
    timeout: 100000
  },
  
  compilers: {
    solc: {
      version: "0.8.19",
      settings: {
        optimizer: {
          enabled: true,
          runs: 200
        }
      }
    }
  }
};
