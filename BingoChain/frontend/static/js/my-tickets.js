// My Tickets JavaScript

let playerTickets = [];
let playerStats = null;
let currentFilter = 'all';

/**
 * Initialize my tickets page
 */
function initializeMyTicketsPage() {
    // Check wallet connection
    if (!window.walletManager || !walletManager.isWalletConnected()) {
        showWalletRequired();
        return;
    }

    showTicketsSection();
    setupEventListeners();
    loadPlayerData();
}

/**
 * Setup event listeners
 */
function setupEventListeners() {
    // Connect wallet button
    document.getElementById('connectWalletBtn').addEventListener('click', function() {
        if (window.walletManager) {
            walletManager.connectWallet();
        }
    });

    // Status filter
    document.getElementById('statusFilter').addEventListener('change', function(e) {
        currentFilter = e.target.value;
        filterAndDisplayTickets();
    });

    // Refresh button
    document.getElementById('refreshBtn').addEventListener('click', function() {
        loadPlayerData();
    });

    // Wallet manager events
    if (window.walletManager) {
        const originalUpdateUI = walletManager.updateUI;
        walletManager.updateUI = function() {
            originalUpdateUI.call(this);
            
            if (this.isConnected && this.account) {
                showTicketsSection();
                loadPlayerData();
            } else {
                showWalletRequired();
            }
        };
    }
}

/**
 * Load player data (tickets and stats)
 */
async function loadPlayerData() {
    if (!walletManager || !walletManager.isWalletConnected()) {
        return;
    }

    try {
        document.getElementById('loadingTickets').style.display = 'block';
        
        const walletAddress = walletManager.getAccount();
        
        // Load tickets and stats in parallel
        const [ticketsResponse, statsResponse] = await Promise.all([
            fetch(`/api/tickets/player/${walletAddress}`),
            fetch(`/api/tickets/player/${walletAddress}/statistics`)
        ]);

        if (ticketsResponse.ok) {
            playerTickets = await ticketsResponse.json();
        } else {
            throw new Error('Error loading tickets');
        }

        if (statsResponse.ok) {
            playerStats = await statsResponse.json();
        } else {
            console.warn('Could not load player statistics');
            playerStats = { totalTickets: 0, winningTickets: 0, totalSpent: 0, totalWon: 0 };
        }

        // Update UI
        displayPlayerStats();
        filterAndDisplayTickets();

    } catch (error) {
        console.error('Error loading player data:', error);
        showError('Error al cargar tus boletos');
    } finally {
        document.getElementById('loadingTickets').style.display = 'none';
    }
}

/**
 * Display player statistics
 */
function displayPlayerStats() {
    if (!playerStats) return;

    document.getElementById('totalTickets').textContent = playerStats.totalTickets || 0;
    document.getElementById('winningTickets').textContent = playerStats.winningTickets || 0;
    document.getElementById('totalSpent').textContent = `${parseFloat(playerStats.totalSpent || 0).toFixed(4)} ETH`;
    document.getElementById('totalWon').textContent = `${parseFloat(playerStats.totalWon || 0).toFixed(4)} ETH`;
}

/**
 * Filter and display tickets
 */
function filterAndDisplayTickets() {
    let filteredTickets = playerTickets;

    // Apply filter
    switch (currentFilter) {
        case 'active':
            filteredTickets = playerTickets.filter(ticket => 
                ticket.weeklyLottery && 
                (ticket.weeklyLottery.status === 'TICKET_SALES' || ticket.weeklyLottery.status === 'DRAWING_PHASE')
            );
            break;
        case 'winners':
            filteredTickets = playerTickets.filter(ticket => ticket.isWinner);
            break;
        case 'completed':
            filteredTickets = playerTickets.filter(ticket => 
                ticket.weeklyLottery && ticket.weeklyLottery.status === 'COMPLETED'
            );
            break;
        default:
            // Show all tickets
            break;
    }

    displayTickets(filteredTickets);
}

/**
 * Display tickets grid
 */
function displayTickets(tickets) {
    const ticketsGrid = document.getElementById('ticketsGrid');
    const noTicketsMsg = document.getElementById('noTickets');

    if (tickets.length === 0) {
        ticketsGrid.innerHTML = '';
        noTicketsMsg.style.display = 'block';
        return;
    }

    noTicketsMsg.style.display = 'none';

    let html = '';
    tickets.forEach(ticket => {
        html += `<div class="col-md-6 col-lg-4 mb-4">${createTicketCard(ticket)}</div>`;
    });

    ticketsGrid.innerHTML = html;
}

/**
 * Create ticket card HTML
 */
function createTicketCard(ticket) {
    const isWinner = ticket.isWinner;
    const lottery = ticket.weeklyLottery;
    const numbers = parseNumbers(ticket.chosenNumbers);
    const matchedNumbers = ticket.matchedNumbers || 0;
    const purchaseDate = new Date(ticket.purchasedAt).toLocaleDateString('es-ES');
    
    const cardClass = isWinner ? 'ticket-card winning-ticket' : 'ticket-card';
    const statusBadge = getTicketStatusBadge(ticket);

    return `
        <div class="card ${cardClass} h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <div>
                    <h6 class="mb-0">
                        <i class="fas fa-gem text-warning"></i>
                        Boleto NFT #${ticket.ticketId}
                    </h6>
                    <small class="text-muted">${lottery ? lottery.lotteryName : 'Sorteo'}</small>
                </div>
                ${statusBadge}
            </div>
            
            <div class="card-body">
                <div class="mb-3">
                    <small class="text-muted">Tus Números:</small>
                    <div class="ticket-numbers">
                        ${numbers.map(num => {
                            const isMatched = lottery && lottery.drawnNumbers && 
                                            isNumberMatched(num, lottery.drawnNumbers);
                            return `<span class="ticket-number ${isMatched ? 'matched' : ''}">${num}</span>`;
                        }).join('')}
                    </div>
                </div>
                
                ${lottery && lottery.drawnNumbers && lottery.drawnNumbers !== '[]' ? `
                    <div class="mb-3">
                        <small class="text-muted">Números Sorteados:</small>
                        <div class="drawn-numbers">
                            ${formatDrawnNumbers(lottery.drawnNumbers)}
                        </div>
                    </div>
                ` : ''}
                
                <div class="row text-center">
                    <div class="col-6">
                        <small class="text-muted">Aciertos</small>
                        <div class="fw-bold ${matchedNumbers === 6 ? 'text-success' : 'text-primary'}">${matchedNumbers}/6</div>
                    </div>
                    <div class="col-6">
                        <small class="text-muted">Pagado</small>
                        <div class="fw-bold">${parseFloat(ticket.ticketPricePaid || 0).toFixed(4)} ETH</div>
                    </div>
                </div>
                
                ${isWinner ? `
                    <div class="mt-3 text-center">
                        <div class="alert alert-success mb-0">
                            <i class="fas fa-trophy"></i>
                            <strong>¡GANADOR!</strong><br>
                            Premio: ${parseFloat(ticket.prizeAmount || 0).toFixed(4)} ETH
                        </div>
                    </div>
                ` : ''}
                
                <div class="mt-3">
                    <small class="text-muted">Comprado: ${purchaseDate}</small>
                </div>
            </div>
            
            <div class="card-footer">
                <button class="btn btn-outline-primary btn-sm w-100" onclick="showTicketDetails('${ticket.ticketId}')">
                    <i class="fas fa-eye"></i> Ver Detalles
                </button>
            </div>
        </div>
    `;
}

/**
 * Get ticket status badge
 */
function getTicketStatusBadge(ticket) {
    if (ticket.isWinner) {
        return '<span class="badge bg-success"><i class="fas fa-trophy"></i> Ganador</span>';
    }
    
    if (!ticket.weeklyLottery) {
        return '<span class="badge bg-secondary">Sin Sorteo</span>';
    }
    
    switch (ticket.weeklyLottery.status) {
        case 'TICKET_SALES':
            return '<span class="badge bg-primary">Abierto</span>';
        case 'DRAWING_PHASE':
            return '<span class="badge bg-warning text-dark">Sorteando</span>';
        case 'COMPLETED':
            return '<span class="badge bg-secondary">Finalizado</span>';
        case 'CANCELLED':
            return '<span class="badge bg-danger">Cancelado</span>';
        default:
            return '<span class="badge bg-secondary">Desconocido</span>';
    }
}

/**
 * Parse numbers from JSON string
 */
function parseNumbers(numbersStr) {
    try {
        if (!numbersStr) return [];
        return JSON.parse(numbersStr);
    } catch (error) {
        console.error('Error parsing numbers:', error);
        return [];
    }
}

/**
 * Check if a number is matched in drawn numbers
 */
function isNumberMatched(number, drawnNumbersStr) {
    try {
        if (!drawnNumbersStr || drawnNumbersStr === '[]') return false;
        const drawnNumbers = JSON.parse(drawnNumbersStr);
        return drawnNumbers.includes(number);
    } catch (error) {
        return false;
    }
}

/**
 * Format drawn numbers for display
 */
function formatDrawnNumbers(drawnNumbersStr) {
    try {
        if (!drawnNumbersStr || drawnNumbersStr === '[]') {
            return '<span class="text-muted">Ninguno aún</span>';
        }
        
        const numbers = JSON.parse(drawnNumbersStr);
        return numbers.map(num => `<span class="badge bg-primary me-1">${num}</span>`).join('');
    } catch (error) {
        return '<span class="text-muted">Error al cargar números</span>';
    }
}

/**
 * Show ticket details modal
 */
function showTicketDetails(ticketId) {
    const ticket = playerTickets.find(t => t.ticketId === ticketId);
    if (!ticket) return;

    const modalBody = document.getElementById('ticketModalBody');
    const numbers = parseNumbers(ticket.chosenNumbers);
    const lottery = ticket.weeklyLottery;
    
    modalBody.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <h6><i class="fas fa-gem text-warning"></i> Información del NFT</h6>
                <table class="table table-sm">
                    <tr>
                        <td><strong>ID del Boleto:</strong></td>
                        <td>#${ticket.ticketId}</td>
                    </tr>
                    <tr>
                        <td><strong>Contrato:</strong></td>
                        <td class="small">${ticket.transactionHash || 'N/A'}</td>
                    </tr>
                    <tr>
                        <td><strong>Estado:</strong></td>
                        <td>${ticket.isWinner ? '<span class="badge bg-success">Ganador</span>' : '<span class="badge bg-secondary">Regular</span>'}</td>
                    </tr>
                    <tr>
                        <td><strong>Fecha de Compra:</strong></td>
                        <td>${new Date(ticket.purchasedAt).toLocaleString('es-ES')}</td>
                    </tr>
                </table>
            </div>
            
            <div class="col-md-6">
                <h6><i class="fas fa-list-ol"></i> Números del Boleto</h6>
                <div class="ticket-numbers mb-3">
                    ${numbers.map(num => {
                        const isMatched = lottery && lottery.drawnNumbers && 
                                        isNumberMatched(num, lottery.drawnNumbers);
                        return `<span class="ticket-number ${isMatched ? 'matched' : ''}">${num}</span>`;
                    }).join('')}
                </div>
                
                <h6><i class="fas fa-chart-line"></i> Resultados</h6>
                <table class="table table-sm">
                    <tr>
                        <td><strong>Números Acertados:</strong></td>
                        <td>${ticket.matchedNumbers || 0}/6</td>
                    </tr>
                    <tr>
                        <td><strong>Precio Pagado:</strong></td>
                        <td>${parseFloat(ticket.ticketPricePaid || 0).toFixed(4)} ETH</td>
                    </tr>
                    ${ticket.isWinner ? `
                        <tr>
                            <td><strong>Premio Ganado:</strong></td>
                            <td class="text-success">${parseFloat(ticket.prizeAmount || 0).toFixed(4)} ETH</td>
                        </tr>
                    ` : ''}
                </table>
            </div>
        </div>
        
        ${lottery ? `
            <hr>
            <div class="row">
                <div class="col-12">
                    <h6><i class="fas fa-dice"></i> Información del Sorteo</h6>
                    <table class="table table-sm">
                        <tr>
                            <td><strong>Nombre del Sorteo:</strong></td>
                            <td>${lottery.lotteryName}</td>
                        </tr>
                        <tr>
                            <td><strong>Estado:</strong></td>
                            <td>${getTicketStatusBadge(ticket)}</td>
                        </tr>
                        <tr>
                            <td><strong>Números Sorteados:</strong></td>
                            <td>${formatDrawnNumbers(lottery.drawnNumbers)}</td>
                        </tr>
                        <tr>
                            <td><strong>Pozo Total:</strong></td>
                            <td>${parseFloat(lottery.prizePool || 0).toFixed(4)} ETH</td>
                        </tr>
                    </table>
                </div>
            </div>
        ` : ''}
    `;

    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('ticketModal'));
    modal.show();
}

/**
 * Show wallet required section
 */
function showWalletRequired() {
    document.getElementById('walletRequired').style.display = 'block';
    document.getElementById('ticketsSection').style.display = 'none';
}

/**
 * Show tickets section
 */
function showTicketsSection() {
    document.getElementById('walletRequired').style.display = 'none';
    document.getElementById('ticketsSection').style.display = 'block';
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
