// Purchase ticket JavaScript

let currentLottery = null;
let selectedNumbers = [];
let isProcessing = false;

/**
 * Initialize the purchase page
 */
async function initializePurchasePage() {
    try {
        // Check wallet connection
        if (!window.walletManager || !walletManager.isWalletConnected()) {
            showError('Debes conectar tu wallet para comprar boletos');
            setTimeout(() => {
                window.location.href = '/';
            }, 3000);
            return;
        }

        // Load lottery information
        await loadLotteryInfo();
        
        // Setup UI
        setupNumberGrid();
        setupEventListeners();
        updateWalletDisplay();
        
    } catch (error) {
        console.error('Error initializing purchase page:', error);
        showError('Error al cargar la página de compra');
    }
}

/**
 * Load lottery information
 */
async function loadLotteryInfo() {
    try {
        const response = await fetch(`/api/lotteries/${LOTTERY_ID}`);
        if (response.ok) {
            currentLottery = await response.json();
            displayLotteryInfo();
            
            // Check if lottery is available for purchase
            if (currentLottery.status !== 'TICKET_SALES') {
                showError('Este sorteo ya no está disponible para compra de boletos');
                setTimeout(() => {
                    window.location.href = '/';
                }, 3000);
                return;
            }
            
            // Check sales period
            const now = new Date();
            const salesStart = new Date(currentLottery.salesStartTime);
            const salesEnd = new Date(currentLottery.salesEndTime);
            
            if (now < salesStart) {
                showError('La venta de boletos aún no ha comenzado');
                setTimeout(() => {
                    window.location.href = '/';
                }, 3000);
                return;
            }
            
            if (now > salesEnd) {
                showError('La venta de boletos ha terminado');
                setTimeout(() => {
                    window.location.href = '/';
                }, 3000);
                return;
            }
            
            // Show purchase section
            document.getElementById('purchaseSection').style.display = 'block';
            
        } else {
            throw new Error('Lottery not found');
        }
    } catch (error) {
        console.error('Error loading lottery:', error);
        showError('Error al cargar información del sorteo');
    }
}

/**
 * Display lottery information
 */
function displayLotteryInfo() {
    const lotteryInfoContainer = document.getElementById('lotteryInfo');
    const priceInEth = parseFloat(currentLottery.ticketPrice || 0).toFixed(4);
    
    lotteryInfoContainer.innerHTML = `
        <div class="card-body">
            <div class="row">
                <div class="col-md-8">
                    <h5 class="card-title">${currentLottery.lotteryName || 'Sorteo Semanal'}</h5>
                    <p class="card-text">
                        <strong>Estado:</strong> <span class="badge bg-success">Abierto para Compras</span><br>
                        <strong>Precio del Boleto:</strong> ${priceInEth} ETH<br>
                        <strong>Pozo Acumulado:</strong> ${parseFloat(currentLottery.prizePool || 0).toFixed(4)} ETH<br>
                        <strong>Boletos Vendidos:</strong> ${currentLottery.totalTickets || 0}
                    </p>
                </div>
                <div class="col-md-4 text-md-end">
                    <div class="sales-timer">
                        <small class="text-muted">Venta termina:</small><br>
                        <div class="fw-bold text-danger" id="salesTimer">Calculando...</div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Update price display
    document.getElementById('ticketPrice').textContent = `${priceInEth} ETH`;
    document.getElementById('totalPrice').textContent = `${priceInEth} ETH`;
    
    // Start sales timer
    startSalesTimer();
}

/**
 * Setup number grid (1-100)
 */
function setupNumberGrid() {
    const numberGrid = document.getElementById('numberGrid');
    let html = '';
    
    for (let i = 1; i <= 100; i++) {
        html += `
            <button class="number-btn" data-number="${i}" onclick="toggleNumber(${i})">
                ${i}
            </button>
        `;
    }
    
    numberGrid.innerHTML = html;
}

/**
 * Setup event listeners
 */
function setupEventListeners() {
    // Quick pick button
    document.getElementById('quickPickBtn').addEventListener('click', quickPick);
    
    // Clear selection button
    document.getElementById('clearSelectionBtn').addEventListener('click', clearSelection);
    
    // Purchase button
    document.getElementById('purchaseBtn').addEventListener('click', purchaseTicket);
}

/**
 * Toggle number selection
 */
function toggleNumber(number) {
    if (selectedNumbers.includes(number)) {
        // Remove number
        selectedNumbers = selectedNumbers.filter(n => n !== number);
    } else {
        // Add number (max 6)
        if (selectedNumbers.length < 6) {
            selectedNumbers.push(number);
        } else {
            showError('Solo puedes seleccionar 6 números');
            return;
        }
    }
    
    // Sort selected numbers
    selectedNumbers.sort((a, b) => a - b);
    
    // Update UI
    updateNumberGrid();
    updateSelectedDisplay();
    updatePurchaseButton();
}

/**
 * Quick pick - random selection of 6 numbers
 */
function quickPick() {
    selectedNumbers = [];
    
    while (selectedNumbers.length < 6) {
        const randomNumber = Math.floor(Math.random() * 100) + 1;
        if (!selectedNumbers.includes(randomNumber)) {
            selectedNumbers.push(randomNumber);
        }
    }
    
    selectedNumbers.sort((a, b) => a - b);
    
    updateNumberGrid();
    updateSelectedDisplay();
    updatePurchaseButton();
}

/**
 * Clear all selections
 */
function clearSelection() {
    selectedNumbers = [];
    updateNumberGrid();
    updateSelectedDisplay();
    updatePurchaseButton();
}

/**
 * Update number grid visual state
 */
function updateNumberGrid() {
    const buttons = document.querySelectorAll('.number-btn');
    buttons.forEach(btn => {
        const number = parseInt(btn.dataset.number);
        if (selectedNumbers.includes(number)) {
            btn.classList.add('selected');
        } else {
            btn.classList.remove('selected');
        }
    });
}

/**
 * Update selected numbers display
 */
function updateSelectedDisplay() {
    const selectedDisplay = document.getElementById('selectedNumbers');
    const selectedCount = document.getElementById('selectedCount');
    
    if (selectedNumbers.length === 0) {
        selectedDisplay.innerHTML = '<span class="text-muted">Ninguno seleccionado</span>';
    } else {
        const numbersHtml = selectedNumbers.map(num => 
            `<span class="badge bg-primary me-1">${num}</span>`
        ).join('');
        selectedDisplay.innerHTML = numbersHtml;
    }
    
    selectedCount.textContent = `${selectedNumbers.length}/6`;
}

/**
 * Update purchase button state
 */
function updatePurchaseButton() {
    const purchaseBtn = document.getElementById('purchaseBtn');
    const isValidSelection = selectedNumbers.length === 6;
    const isWalletConnected = walletManager && walletManager.isWalletConnected();
    
    purchaseBtn.disabled = !isValidSelection || !isWalletConnected || isProcessing;
    
    if (!isWalletConnected) {
        purchaseBtn.innerHTML = '<i class="fas fa-wallet"></i> Conectar Wallet';
    } else if (!isValidSelection) {
        purchaseBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Selecciona 6 Números';
    } else {
        purchaseBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Comprar Boleto NFT';
    }
}

/**
 * Update wallet display
 */
function updateWalletDisplay() {
    const walletDisplay = document.getElementById('walletDisplay');
    
    if (walletManager && walletManager.isWalletConnected()) {
        const address = walletManager.getAccount();
        walletDisplay.textContent = `${address.slice(0, 6)}...${address.slice(-4)}`;
    } else {
        walletDisplay.textContent = 'No conectado';
    }
}

/**
 * Purchase ticket
 */
async function purchaseTicket() {
    if (isProcessing) return;
    
    try {
        isProcessing = true;
        
        // Validate selection
        if (selectedNumbers.length !== 6) {
            showError('Debes seleccionar exactamente 6 números');
            return;
        }
        
        // Validate wallet connection
        if (!walletManager || !walletManager.isWalletConnected()) {
            showError('Debes conectar tu wallet');
            return;
        }
        
        // Show loading
        document.getElementById('purchaseSection').style.display = 'none';
        document.getElementById('loadingPurchase').style.display = 'block';
        
        // Get Web3 instance
        const web3 = walletManager.getWeb3();
        if (!web3) {
            throw new Error('Web3 no disponible');
        }
        
        // Prepare transaction data
        const ticketPriceWei = web3.utils.toWei(currentLottery.ticketPrice.toString(), 'ether');
        const account = walletManager.getAccount();
        
        // For now, simulate the blockchain transaction
        // In a real implementation, you would call the smart contract here
        
        // Simulate transaction delay
        await new Promise(resolve => setTimeout(resolve, 3000));
        
        // Create ticket data for backend
        const ticketData = {
            walletAddress: account,
            lotteryId: currentLottery.id,
            chosenNumbers: JSON.stringify(selectedNumbers),
            ticketPrice: currentLottery.ticketPrice,
            transactionHash: '0x' + Math.random().toString(16).slice(2), // Simulate hash
            contractTicketId: Date.now().toString() // Simulate contract ticket ID
        };
        
        // Send to backend
        const response = await fetch('/api/tickets/purchase', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(ticketData)
        });
        
        if (response.ok) {
            const ticket = await response.json();
            showSuccess('¡Boleto NFT comprado exitosamente!');
            
            // Redirect to my tickets page after a delay
            setTimeout(() => {
                window.location.href = '/my-tickets';
            }, 3000);
            
        } else {
            throw new Error('Error al registrar el boleto en la base de datos');
        }
        
    } catch (error) {
        console.error('Error purchasing ticket:', error);
        showError('Error al comprar el boleto: ' + error.message);
        
        // Hide loading and show form again
        document.getElementById('loadingPurchase').style.display = 'none';
        document.getElementById('purchaseSection').style.display = 'block';
        
    } finally {
        isProcessing = false;
        updatePurchaseButton();
    }
}

/**
 * Start sales timer countdown
 */
function startSalesTimer() {
    if (!currentLottery.salesEndTime) return;
    
    const endTime = new Date(currentLottery.salesEndTime);
    
    function updateTimer() {
        const now = new Date();
        const timeLeft = endTime - now;
        
        if (timeLeft <= 0) {
            document.getElementById('salesTimer').textContent = 'Venta cerrada';
            return;
        }
        
        const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));
        const hours = Math.floor((timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((timeLeft % (1000 * 60)) / 1000);
        
        let timerText = '';
        if (days > 0) timerText += `${days}d `;
        timerText += `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        document.getElementById('salesTimer').textContent = timerText;
    }
    
    updateTimer();
    setInterval(updateTimer, 1000);
}

/**
 * Show error message
 */
function showError(message) {
    const alertHtml = `
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="fas fa-exclamation-triangle"></i> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    const container = document.querySelector('.container');
    container.insertAdjacentHTML('afterbegin', alertHtml);
}

/**
 * Show success message
 */
function showSuccess(message) {
    const alertHtml = `
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="fas fa-check-circle"></i> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    const container = document.querySelector('.container');
    container.insertAdjacentHTML('afterbegin', alertHtml);
}
