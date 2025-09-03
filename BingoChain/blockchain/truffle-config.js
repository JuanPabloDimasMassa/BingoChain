const HDWalletProvider = require('@truffle/hdwallet-provider');
require('dotenv').config();

const mnemonic = process.env.MNEMONIC || 'candy maple cake sugar pudding cream honey rich smooth crumble sweet treat';
const infuraKey = process.env.INFURA_API_KEY || '';

module.exports = {
  networks: {
    // Local development network
    development: {
      host: "127.0.0.1",
      port: 8545,
      network_id: "*", // Match any network id
      gas: 6721975,
      gasPrice: 20000000000, // 20 Gwei
    },
    
    // Ganache GUI
    ganache: {
      host: "127.0.0.1",
      port: 7545,
      network_id: "*",
      gas: 6721975,
      gasPrice: 20000000000,
    },
    
    // Ethereum testnet (Sepolia)
    sepolia: {
      provider: () => new HDWalletProvider(
        mnemonic,
        `https://sepolia.infura.io/v3/${infuraKey}`
      ),
      network_id: 11155111,
      gas: 4000000,
      gasPrice: 10000000000, // 10 Gwei
      confirmations: 2,
      timeoutBlocks: 200,
      skipDryRun: true
    },
    
    // Polygon Mumbai testnet
    mumbai: {
      provider: () => new HDWalletProvider(
        mnemonic,
        `https://polygon-mumbai.infura.io/v3/${infuraKey}`
      ),
      network_id: 80001,
      gas: 2000000,
      gasPrice: 10000000000,
      confirmations: 2,
      timeoutBlocks: 200,
      skipDryRun: true
    },
    
    // BSC testnet
    bscTestnet: {
      provider: () => new HDWalletProvider(
        mnemonic,
        'https://data-seed-prebsc-1-s1.binance.org:8545/'
      ),
      network_id: 97,
      gas: 2000000,
      gasPrice: 10000000000,
      confirmations: 2,
      timeoutBlocks: 200,
      skipDryRun: true
    }
  },

  // Set default mocha options here, use special reporters etc.
  mocha: {
    timeout: 100000
  },

  // Configure your compilers
  compilers: {
    solc: {
      version: "0.8.19",
      settings: {
        optimizer: {
          enabled: true,
          runs: 200
        },
        evmVersion: "byzantium"
      }
    }
  },

  // Truffle DB is currently disabled by default; to enable it, change enabled:
  // false to enabled: true. The default storage location can also be
  // overridden by specifying the adapter settings, as shown in the commented code below.
  db: {
    enabled: false,
  },

  // Plugin configuration
  plugins: [
    'truffle-plugin-verify'
  ],

  // API keys for contract verification
  api_keys: {
    etherscan: process.env.ETHERSCAN_API_KEY || '',
    bscscan: process.env.BSCSCAN_API_KEY || '',
    polygonscan: process.env.POLYGONSCAN_API_KEY || ''
  }
};
