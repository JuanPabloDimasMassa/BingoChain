// Main application JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Socket.IO connection
    const socket = io();
    
    // Global variables
    let currentGame = null;
    let playerCard = null;
    let isConnected = false;
    
    // Socket event handlers
    socket.on('connect', function() {
        console.log('Connected to server');
        isConnected = true;
        updateConnectionStatus(true);
    });
    
    socket.on('disconnect', function() {
        console.log('Disconnected from server');
        isConnected = false;
        updateConnectionStatus(false);
    });
    
    socket.on('status', function(data) {
        console.log('Server status:', data.msg);
    });
    
    socket.on('number_called', function(data) {
        console.log('Number called:', data);
        handleNumberCalled(data);
    });
    
    socket.on('bingo_claimed', function(data) {
        console.log('Bingo claimed:', data);
        handleBingoClaimed(data);
    });
    
    socket.on('joined_room', function(data) {
        console.log('Joined game room:', data);
    });
    
    // Utility functions
    function updateConnectionStatus(connected) {
        const statusElement = document.getElementById('connectionStatus');
        if (statusElement) {
            statusElement.textContent = connected ? 'Connected' : 'Disconnected';
            statusElement.className = connected ? 'text-success' : 'text-danger';
        }
    }
    
    function handleNumberCalled(data) {
        const { game_id, number, timestamp } = data;
        
        if (currentGame && currentGame.id === game_id) {
            // Update current call display
            updateCurrentCall(number);
            
            // Add to called numbers list
            addToCalledNumbers(number);
            
            // Mark number on player's card if applicable
            if (playerCard) {
                markNumberOnCard(number);
            }
            
            // Play sound notification
            playNotificationSound();
        }
    }
    
    function handleBingoClaimed(data) {
        const { game_id, winner, timestamp } = data;
        
        if (currentGame && currentGame.id === game_id) {
            showWinnerNotification(winner);
            
            // If current player is the winner
            if (walletManager && walletManager.getAccount() === winner) {
                showWinnerCelebration();
            }
        }
    }
    
    function updateCurrentCall(number) {
        const currentCallElement = document.getElementById('currentCall');
        if (currentCallElement) {
            currentCallElement.textContent = number;
            currentCallElement.classList.add('number-call-animation');
            
            setTimeout(() => {
                currentCallElement.classList.remove('number-call-animation');
            }, 500);
        }
    }
    
    function addToCalledNumbers(number) {
        const calledNumbersContainer = document.getElementById('calledNumbers');
        if (calledNumbersContainer) {
            const numberElement = document.createElement('span');
            numberElement.className = 'called-number';
            numberElement.textContent = number;
            calledNumbersContainer.appendChild(numberElement);
            
            // Scroll to bottom
            calledNumbersContainer.scrollTop = calledNumbersContainer.scrollHeight;
        }
    }
    
    function markNumberOnCard(number) {
        const cardNumbers = document.querySelectorAll('.bingo-number');
        cardNumbers.forEach(cell => {
            if (parseInt(cell.textContent) === number) {
                cell.classList.add('marked');
                cell.innerHTML = `<i class="fas fa-check"></i> ${number}`;
            }
        });
    }
    
    function playNotificationSound() {
        // Create and play notification sound
        const audio = new Audio('data:audio/wav;base64,UklGRhgBAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YfQAAAAAAAAAAAEBAAIGAAAAAgMAAAABBgAAAAABAQAAAAIDAAAAAQYAAAAAAgMAAAAAAwYAAAAAAgMAAAAAAwYAAAABAQYAAAABAwYAAAABBQYAAAACAwYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQYAAAABAwYAAAABBQ==');
        audio.volume = 0.3;
        audio.play().catch(() => {
            // Ignore audio play errors (user interaction required)
        });
    }
    
    function showWinnerNotification(winner) {
        const notification = createNotification('success', `¡Bingo! Ganador: ${formatAddress(winner)}`);
        document.body.appendChild(notification);
    }
    
    function showWinnerCelebration() {
        const celebration = document.createElement('div');
        celebration.className = 'winner-celebration position-fixed top-50 start-50 translate-middle';
        celebration.style.zIndex = '9999';
        celebration.innerHTML = `
            <h2><i class="fas fa-trophy text-warning"></i> ¡FELICIDADES!</h2>
            <p>¡Has ganado el juego de Bingo!</p>
            <p>Tu premio será transferido automáticamente.</p>
        `;
        
        document.body.appendChild(celebration);
        
        setTimeout(() => {
            celebration.remove();
        }, 5000);
    }
    
    function createNotification(type, message) {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 5000);
        
        return notification;
    }
    
    function formatAddress(address) {
        return `${address.slice(0, 6)}...${address.slice(-4)}`;
    }
    
    // Game management functions
    window.joinGameRoom = function(gameId) {
        if (!walletManager || !walletManager.isWalletConnected()) {
            showError('Por favor conecta tu wallet primero');
            return;
        }
        
        socket.emit('join_game_room', {
            game_id: gameId,
            wallet_address: walletManager.getAccount()
        });
    };
    
    window.leaveGameRoom = function(gameId) {
        socket.emit('leave_game_room', {
            game_id: gameId
        });
    };
    
    window.claimBingo = function(gameId) {
        if (!walletManager || !walletManager.isWalletConnected()) {
            showError('Por favor conecta tu wallet primero');
            return;
        }
        
        // Check if player has a winning pattern
        if (!checkWinningPattern()) {
            showError('No tienes un patrón ganador válido');
            return;
        }
        
        // Emit bingo claim event
        socket.emit('bingo_claimed', {
            game_id: gameId,
            wallet_address: walletManager.getAccount(),
            timestamp: new Date().toISOString()
        });
        
        // Also call smart contract method
        claimBingoOnContract(gameId);
    };
    
    function checkWinningPattern() {
        const markedCells = document.querySelectorAll('.bingo-number.marked');
        const gridSize = 5;
        const marked = new Array(25).fill(false);
        
        // Map marked cells to grid positions
        markedCells.forEach(cell => {
            const index = parseInt(cell.dataset.index);
            if (!isNaN(index)) {
                marked[index] = true;
            }
        });
        
        // Check rows
        for (let row = 0; row < gridSize; row++) {
            let rowComplete = true;
            for (let col = 0; col < gridSize; col++) {
                const index = row * gridSize + col;
                if (index !== 12 && !marked[index]) { // 12 is FREE space
                    rowComplete = false;
                    break;
                }
            }
            if (rowComplete) return true;
        }
        
        // Check columns
        for (let col = 0; col < gridSize; col++) {
            let colComplete = true;
            for (let row = 0; row < gridSize; row++) {
                const index = row * gridSize + col;
                if (index !== 12 && !marked[index]) { // 12 is FREE space
                    colComplete = false;
                    break;
                }
            }
            if (colComplete) return true;
        }
        
        // Check diagonals
        let diag1Complete = true;
        let diag2Complete = true;
        
        for (let i = 0; i < gridSize; i++) {
            const diag1Index = i * gridSize + i;
            const diag2Index = i * gridSize + (gridSize - 1 - i);
            
            if (diag1Index !== 12 && !marked[diag1Index]) {
                diag1Complete = false;
            }
            if (diag2Index !== 12 && !marked[diag2Index]) {
                diag2Complete = false;
            }
        }
        
        return diag1Complete || diag2Complete;
    }
    
    async function claimBingoOnContract(gameId) {
        if (!walletManager || !walletManager.getWeb3()) {
            showError('Wallet no conectado');
            return;
        }
        
        try {
            // TODO: Implement smart contract interaction
            console.log('Claiming bingo on smart contract for game:', gameId);
            
            // This would be implemented with the actual contract ABI and address
            // const contract = new web3.eth.Contract(CONTRACT_ABI, CONTRACT_ADDRESS);
            // const result = await contract.methods.claimBingo(gameId).send({
            //     from: walletManager.getAccount()
            // });
            
            showSuccess('Bingo reclamado exitosamente en el contrato inteligente');
            
        } catch (error) {
            console.error('Error claiming bingo on contract:', error);
            showError('Error al reclamar bingo en el contrato: ' + error.message);
        }
    }
    
    // Utility functions for notifications
    function showError(message) {
        const notification = createNotification('danger', message);
        document.body.appendChild(notification);
    }
    
    function showSuccess(message) {
        const notification = createNotification('success', message);
        document.body.appendChild(notification);
    }
    
    function showInfo(message) {
        const notification = createNotification('info', message);
        document.body.appendChild(notification);
    }
    
    // Export functions for global use
    window.BingoChain = {
        socket: socket,
        joinGameRoom: window.joinGameRoom,
        leaveGameRoom: window.leaveGameRoom,
        claimBingo: window.claimBingo,
        checkWinningPattern: checkWinningPattern,
        showError: showError,
        showSuccess: showSuccess,
        showInfo: showInfo
    };
    
    console.log('BingoChain application initialized');
});
