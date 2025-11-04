// Configuración
const API_BASE_URL = 'http://localhost:3500/api/v1';

// Elementos del DOM
const form = document.getElementById('lotteryForm');
const submitBtn = document.getElementById('submitBtn');
const submitText = document.getElementById('submitText');
const submitLoading = document.getElementById('submitLoading');
const alertContainer = document.getElementById('alertContainer');
const adminTokenInput = document.getElementById('adminToken');
const salesStartTimeInput = document.getElementById('salesStartTime');
const salesEndTimeInput = document.getElementById('salesEndTime');
const salesDurationInput = document.getElementById('salesDuration');

// Establecer fecha mínima al día de hoy
const today = new Date();
today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
salesStartTimeInput.min = today.toISOString().slice(0, 16);
salesEndTimeInput.min = today.toISOString().slice(0, 16);

// Función para actualizar la fecha de fin automáticamente
function updateSalesEndTime() {
    const startTime = salesStartTimeInput.value;
    const duration = parseFloat(salesDurationInput.value) || 23.5;
    
    if (startTime) {
        const start = new Date(startTime);
        const end = new Date(start.getTime() + (duration * 60 * 60 * 1000));
        const endTimeStr = end.toISOString().slice(0, 16);
        salesEndTimeInput.value = endTimeStr;
    }
}

// Event listeners
salesStartTimeInput.addEventListener('change', updateSalesEndTime);
salesDurationInput.addEventListener('input', updateSalesEndTime);

// Función para mostrar alertas
function showAlert(message, type = 'info') {
    alertContainer.innerHTML = `
        <div class="alert alert-${type}">
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
            ${message}
        </div>
    `;
    
    // Auto-ocultar después de 5 segundos para mensajes de éxito
    if (type === 'success') {
        setTimeout(() => {
            alertContainer.innerHTML = '';
        }, 5000);
    }
}

// Función para formatear fecha al formato requerido por la API
function formatDateTimeForAPI(dateTimeLocal) {
    // Convertir de formato datetime-local (YYYY-MM-DDTHH:mm) a formato API (yyyy-MM-ddTHH:mm:ss)
    if (!dateTimeLocal) return null;
    const date = new Date(dateTimeLocal);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}

// Función para validar el formulario
function validateForm() {
    const lotteryName = document.getElementById('lotteryName').value.trim();
    const contractAddress = document.getElementById('contractAddress').value.trim();
    const ticketPrice = document.getElementById('ticketPrice').value;
    const salesStartTime = salesStartTimeInput.value;
    const salesEndTime = salesEndTimeInput.value;
    const adminToken = adminTokenInput.value.trim();

    if (!adminToken) {
        showAlert('Por favor ingresa el token de administrador', 'error');
        return false;
    }

    if (!lotteryName) {
        showAlert('Por favor ingresa el nombre del sorteo', 'error');
        return false;
    }

    if (!contractAddress) {
        showAlert('Por favor ingresa la dirección del contrato', 'error');
        return false;
    }

    if (!contractAddress.startsWith('0x') || contractAddress.length !== 42) {
        showAlert('La dirección del contrato debe ser una dirección Ethereum válida (0x seguido de 40 caracteres hexadecimales)', 'error');
        return false;
    }

    if (!ticketPrice || parseFloat(ticketPrice) <= 0) {
        showAlert('Por favor ingresa un precio válido mayor que 0', 'error');
        return false;
    }

    if (!salesStartTime) {
        showAlert('Por favor selecciona la fecha y hora de inicio', 'error');
        return false;
    }

    if (!salesEndTime) {
        showAlert('Por favor selecciona la fecha y hora de fin', 'error');
        return false;
    }

    const startDate = new Date(salesStartTime);
    const endDate = new Date(salesEndTime);

    if (endDate <= startDate) {
        showAlert('La fecha de fin debe ser posterior a la fecha de inicio', 'error');
        return false;
    }

    return true;
}

// Función para deshabilitar/habilitar el botón de envío
function setLoading(loading) {
    submitBtn.disabled = loading;
    submitText.style.display = loading ? 'none' : 'inline';
    submitLoading.style.display = loading ? 'inline-block' : 'none';
}

// Función para crear el sorteo
async function createLottery(event) {
    event.preventDefault();

    if (!validateForm()) {
        return;
    }

    const adminToken = adminTokenInput.value.trim();
    const lotteryData = {
        lotteryName: document.getElementById('lotteryName').value.trim(),
        contractAddress: document.getElementById('contractAddress').value.trim(),
        ticketPrice: parseFloat(document.getElementById('ticketPrice').value),
        salesStartTime: formatDateTimeForAPI(salesStartTimeInput.value),
        salesEndTime: formatDateTimeForAPI(salesEndTimeInput.value)
    };

    setLoading(true);
    showAlert('Creando sorteo...', 'info');

    try {
        const response = await fetch(`${API_BASE_URL}/lotteries`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Admin-Token': adminToken
            },
            body: JSON.stringify(lotteryData)
        });

        const data = await response.json();

        if (response.ok) {
            showAlert(
                `✅ Sorteo creado exitosamente!<br>
                <strong>ID:</strong> ${data.id}<br>
                <strong>Nombre:</strong> ${data.lotteryName}<br>
                <strong>Estado:</strong> ${data.status}`,
                'success'
            );
            
            // Limpiar el formulario
            form.reset();
            alertContainer.scrollIntoView({ behavior: 'smooth' });
        } else {
            showAlert(
                `❌ Error: ${data.message || data.error || 'Error desconocido'}`,
                'error'
            );
        }
    } catch (error) {
        console.error('Error al crear sorteo:', error);
        showAlert(
            '❌ Error de conexión. Por favor verifica que el servidor esté corriendo.',
            'error'
        );
    } finally {
        setLoading(false);
    }
}

// Event listener para el formulario
form.addEventListener('submit', createLottery);

// Validación en tiempo real para la dirección del contrato
document.getElementById('contractAddress').addEventListener('input', function(e) {
    const address = e.target.value.trim();
    if (address && (!address.startsWith('0x') || address.length !== 42)) {
        e.target.style.borderColor = 'rgba(244, 67, 54, 0.5)';
    } else {
        e.target.style.borderColor = 'rgba(255,255,255,0.3)';
    }
});

console.log('Panel de administrador cargado');


