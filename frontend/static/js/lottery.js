// Lottery management JavaScript

// Global variables
let currentLotteries = [];
let activeLottery = null;

/**
 * Load all lotteries from the backend
 */
async function loadLotteries() {
    try {
        document.getElementById('loadingLotteries').style.display = 'block';
        
        const response = await fetch('/api/lotteries');
        if (response.ok) {
            const lotteries = await response.json();
            currentLotteries = lotteries;
            
            // Find active lottery (TICKET_SALES status)
            activeLottery = lotteries.find(lottery => lottery.status === 'TICKET_SALES');
            
            // Display lotteries
            displayActiveLottery();
            displayAllLotteries();
        } else {
            showError('Error al cargar los sorteos');
        }
    } catch (error) {
        console.error('Error loading lotteries:', error);
        showError('Error al conectar con el servidor');
    } finally {
        document.getElementById('loadingLotteries').style.display = 'none';
    }
}

/**
 * Display the active lottery
 */
function displayActiveLottery() {
    const activeLotteryContainer = document.getElementById('activeLottery');
    
    if (activeLottery) {
        activeLotteryContainer.innerHTML = createLotteryCard(activeLottery, true);
    } else {
        activeLotteryContainer.innerHTML = `
            <div class="alert alert-info">
                <h5><i class="fas fa-info-circle"></i> No hay sorteos activos</h5>
                <p>Actualmente no hay sorteos con venta de boletos abierta. Revisa pronto para nuevos sorteos.</p>
            </div>
        `;
    }
}

/**
 * Display all lotteries
 */
function displayAllLotteries() {
    const allLotteriesContainer = document.getElementById('allLotteries');
    
    if (currentLotteries.length === 0) {
        allLotteriesContainer.innerHTML = `
            <div class="col-12">
                <div class="alert alert-warning text-center">
                    <h5><i class="fas fa-exclamation-triangle"></i> No hay sorteos disponibles</h5>
                    <p>Aún no se han creado sorteos en el sistema.</p>
                </div>
            </div>
        `;
        return;
    }
    
    let html = '';
    currentLotteries.forEach(lottery => {
        html += `<div class="col-md-6 col-lg-4 mb-4">${createLotteryCard(lottery, false)}</div>`;
    });
    
    allLotteriesContainer.innerHTML = html;
}

/**
 * Create a lottery card HTML
 */
function createLotteryCard(lottery, isActive = false) {
    const status = getLotteryStatus(lottery);
    const statusClass = getStatusClass(lottery.status);
    const salesTime = formatSalesTime(lottery);
    const priceInEth = parseFloat(lottery.ticketPrice || 0).toFixed(4);
    
    return `
        <div class="card ${isActive ? 'border-success' : ''} h-100">
            ${isActive ? '<div class="card-header bg-success text-white"><h6 class="mb-0"><i class="fas fa-star"></i> Sorteo Activo</h6></div>' : ''}
            <div class="card-body">
                <h5 class="card-title">
                    ${lottery.lotteryName || 'Sorteo Semanal'}
                    <span class="badge ${statusClass} ms-2">${status.text}</span>
                </h5>
                
                <div class="row mb-3">
                    <div class="col-6">
                        <small class="text-muted">Precio del Boleto</small>
                        <div class="fw-bold">${priceInEth} ETH</div>
                    </div>
                    <div class="col-6">
                        <small class="text-muted">Pozo Acumulado</small>
                        <div class="fw-bold text-success">${parseFloat(lottery.prizePool || 0).toFixed(4)} ETH</div>
                    </div>
                </div>
                
                <div class="row mb-3">
                    <div class="col-12">
                        <small class="text-muted">Boletos Vendidos</small>
                        <div class="fw-bold">${lottery.totalTickets || 0}</div>
                    </div>
                </div>
                
                <div class="mb-3">
                    <small class="text-muted">Periodo de Ventas</small>
                    <div class="small">${salesTime}</div>
                </div>
                
                ${lottery.drawnNumbers && lottery.drawnNumbers !== '[]' ? `
                    <div class="mb-3">
                        <small class="text-muted">Números Sorteados</small>
                        <div class="drawn-numbers">
                            ${formatDrawnNumbers(lottery.drawnNumbers)}
                        </div>
                    </div>
                ` : ''}
                
                ${lottery.status === 'DRAWING_PHASE' ? `
                    <div class="mb-3">
                        <small class="text-muted">Día del Sorteo</small>
                        <div class="fw-bold">${lottery.currentDrawDay || 0}/6</div>
                    </div>
                ` : ''}
            </div>
            
            <div class="card-footer">
                ${createLotteryActions(lottery)}
            </div>
        </div>
    `;
}

/**
 * Create lottery action buttons
 */
function createLotteryActions(lottery) {
    if (lottery.status === 'TICKET_SALES') {
        return `
            <a href="/purchase/${lottery.id}" class="btn btn-primary w-100">
                <i class="fas fa-ticket-alt"></i> Comprar Boletos
            </a>
        `;
    } else if (lottery.status === 'DRAWING_PHASE') {
        return `
            <button class="btn btn-info w-100" onclick="viewLotteryDetails(${lottery.id})">
                <i class="fas fa-eye"></i> Ver Sorteo en Curso
            </button>
        `;
    } else if (lottery.status === 'COMPLETED') {
        return `
            <button class="btn btn-success w-100" onclick="viewLotteryResults(${lottery.id})">
                <i class="fas fa-trophy"></i> Ver Resultados
            </button>
        `;
    } else {
        return `
            <button class="btn btn-secondary w-100" disabled>
                <i class="fas fa-ban"></i> Sorteo ${lottery.status === 'CANCELLED' ? 'Cancelado' : 'No Disponible'}
            </button>
        `;
    }
}

/**
 * Get lottery status text and icon
 */
function getLotteryStatus(lottery) {
    switch (lottery.status) {
        case 'TICKET_SALES':
            return { text: 'Abierto', icon: 'fas fa-ticket-alt' };
        case 'DRAWING_PHASE':
            return { text: 'Sorteando', icon: 'fas fa-dice' };
        case 'COMPLETED':
            return { text: 'Cerrado', icon: 'fas fa-check-circle' };
        case 'CANCELLED':
            return { text: 'Cancelado', icon: 'fas fa-times-circle' };
        default:
            return { text: 'Desconocido', icon: 'fas fa-question-circle' };
    }
}

/**
 * Get CSS class for status badge
 */
function getStatusClass(status) {
    switch (status) {
        case 'TICKET_SALES':
            return 'bg-success';
        case 'DRAWING_PHASE':
            return 'bg-warning text-dark';
        case 'COMPLETED':
            return 'bg-secondary';
        case 'CANCELLED':
            return 'bg-danger';
        default:
            return 'bg-secondary';
    }
}

/**
 * Format sales time for display
 */
function formatSalesTime(lottery) {
    if (!lottery.salesStartTime || !lottery.salesEndTime) {
        return 'No definido';
    }
    
    const start = new Date(lottery.salesStartTime);
    const end = new Date(lottery.salesEndTime);
    const now = new Date();
    
    const startStr = start.toLocaleDateString('es-ES', { 
        weekday: 'short', 
        day: '2-digit', 
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
    
    const endStr = end.toLocaleDateString('es-ES', { 
        weekday: 'short', 
        day: '2-digit', 
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
    
    if (now < start) {
        return `Inicia: ${startStr}`;
    } else if (now >= start && now <= end) {
        return `Termina: ${endStr}`;
    } else {
        return `${startStr} - ${endStr}`;
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
 * View lottery details
 */
function viewLotteryDetails(lotteryId) {
    window.location.href = `/lottery/${lotteryId}`;
}

/**
 * View lottery results
 */
function viewLotteryResults(lotteryId) {
    window.location.href = `/lottery/${lotteryId}`;
}

/**
 * Refresh lotteries data
 */
function refreshLotteries() {
    loadLotteries();
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
    
    // Insert at the top of the container
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
    
    // Insert at the top of the container
    const container = document.querySelector('.container');
    container.insertAdjacentHTML('afterbegin', alertHtml);
}

// Auto-refresh lotteries every 30 seconds
setInterval(refreshLotteries, 30000);
