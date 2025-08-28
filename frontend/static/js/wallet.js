// Wallet connection and management
class WalletManager {
    constructor() {
        this.web3 = null;
        this.account = null;
        this.isConnected = false;
        this.chainId = null;
        
        this.connectButton = document.getElementById('connectWalletBtn');
        this.disconnectButton = document.getElementById('disconnectWalletBtn');
        this.walletInfo = document.getElementById('walletInfo');
        this.walletAddress = document.getElementById('walletAddress');
        
        this.initializeEventListeners();
        this.checkExistingConnection();
    }
    
    initializeEventListeners() {
        if (this.connectButton) {
            this.connectButton.addEventListener('click', () => this.connectWallet());
        }
        
        if (this.disconnectButton) {
            this.disconnectButton.addEventListener('click', () => this.disconnectWallet());
        }
        
        // Listen for account changes
        if (window.ethereum) {
            window.ethereum.on('accountsChanged', (accounts) => {
                if (accounts.length === 0) {
                    this.disconnectWallet();
                } else {
                    this.updateAccount(accounts[0]);
                }
            });
            
            window.ethereum.on('chainChanged', (chainId) => {
                this.chainId = chainId;
                this.checkNetwork();
            });
        }
    }
    
    async checkExistingConnection() {
        if (window.ethereum) {
            try {
                const accounts = await window.ethereum.request({ method: 'eth_accounts' });
                if (accounts.length > 0) {
                    await this.connectWallet();
                }
            } catch (error) {
                console.error('Error checking existing connection:', error);
            }
        }
    }
    
    async connectWallet() {
        if (!window.ethereum) {
            this.showError('MetaMask no está instalado. Por favor, instala MetaMask para continuar.');
            return;
        }
        
        try {
            // Show loading state
            this.setLoadingState(true);
            
            // Request account access
            const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
            
            if (accounts.length === 0) {
                throw new Error('No hay cuentas disponibles');
            }
            
            // Initialize Web3
            this.web3 = new Web3(window.ethereum);
            this.account = accounts[0];
            this.chainId = await this.web3.eth.getChainId();
            
            // Check if we're on the correct network
            await this.checkNetwork();
            
            // Update UI
            this.updateUI();
            this.isConnected = true;
            
            // Notify backend
            await this.notifyBackend();
            
            this.showSuccess('Wallet conectado exitosamente');
            
        } catch (error) {
            console.error('Error connecting wallet:', error);
            this.showError('Error al conectar wallet: ' + error.message);
        } finally {
            this.setLoadingState(false);
        }
    }
    
    async checkNetwork() {
        const expectedChainId = 1337; // Local development network
        
        if (this.chainId !== expectedChainId) {
            try {
                await window.ethereum.request({
                    method: 'wallet_switchEthereumChain',
                    params: [{ chainId: '0x' + expectedChainId.toString(16) }],
                });
            } catch (switchError) {
                // This error code indicates that the chain has not been added to MetaMask
                if (switchError.code === 4902) {
                    try {
                        await window.ethereum.request({
                            method: 'wallet_addEthereumChain',
                            params: [{
                                chainId: '0x' + expectedChainId.toString(16),
                                chainName: 'Local Development',
                                nativeCurrency: {
                                    name: 'ETH',
                                    symbol: 'ETH',
                                    decimals: 18
                                },
                                rpcUrls: ['http://localhost:8545'],
                            }],
                        });
                    } catch (addError) {
                        console.error('Error adding network:', addError);
                    }
                }
            }
        }
    }
    
    disconnectWallet() {
        this.web3 = null;
        this.account = null;
        this.isConnected = false;
        this.chainId = null;
        
        this.updateUI();
        this.showInfo('Wallet desconectado');
    }
    
    updateAccount(newAccount) {
        this.account = newAccount;
        this.updateUI();
    }
    
    updateUI() {
        if (this.isConnected && this.account) {
            // Show wallet info, hide connect button
            if (this.connectButton) {
                this.connectButton.classList.add('d-none');
            }
            if (this.walletInfo) {
                this.walletInfo.classList.remove('d-none');
            }
            if (this.walletAddress) {
                this.walletAddress.textContent = this.formatAddress(this.account);
            }
        } else {
            // Show connect button, hide wallet info
            if (this.connectButton) {
                this.connectButton.classList.remove('d-none');
            }
            if (this.walletInfo) {
                this.walletInfo.classList.add('d-none');
            }
        }
    }
    
    async notifyBackend() {
        try {
            const response = await fetch('/api/wallet/connect', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    address: this.account,
                    chainId: this.chainId
                })
            });
            
            if (!response.ok) {
                throw new Error('Failed to notify backend');
            }
            
        } catch (error) {
            console.error('Error notifying backend:', error);
        }
    }
    
    formatAddress(address) {
        return `${address.slice(0, 6)}...${address.slice(-4)}`;
    }
    
    setLoadingState(loading) {
        if (this.connectButton) {
            if (loading) {
                this.connectButton.disabled = true;
                this.connectButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Conectando...';
            } else {
                this.connectButton.disabled = false;
                this.connectButton.innerHTML = '<i class="fas fa-wallet"></i> Conectar Wallet';
            }
        }
    }
    
    showError(message) {
        this.showNotification(message, 'danger');
    }
    
    showSuccess(message) {
        this.showNotification(message, 'success');
    }
    
    showInfo(message) {
        this.showNotification(message, 'info');
    }
    
    showNotification(message, type) {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(notification);
        
        // Auto remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 5000);
    }
    
    // Utility methods for other components
    getAccount() {
        return this.account;
    }
    
    getWeb3() {
        return this.web3;
    }
    
    isWalletConnected() {
        return this.isConnected;
    }
    
    async getBalance() {
        if (!this.web3 || !this.account) {
            return '0';
        }
        
        try {
            const balance = await this.web3.eth.getBalance(this.account);
            return this.web3.utils.fromWei(balance, 'ether');
        } catch (error) {
            console.error('Error getting balance:', error);
            return '0';
        }
    }
}

// Initialize wallet manager when DOM is loaded
let walletManager;
document.addEventListener('DOMContentLoaded', () => {
    walletManager = new WalletManager();
});

// Export for use in other scripts
window.walletManager = walletManager;
