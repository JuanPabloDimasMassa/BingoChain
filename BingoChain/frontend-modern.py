#!/usr/bin/env python3
from flask import Flask, send_file, abort
import os

app = Flask(__name__)

@app.route('/')
def home():
    return '''
<!DOCTYPE html>
<html lang="es">
<head>
    <title>🎰 BingoChain - Lotería NFT</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Poppins', sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
            min-height: 100vh;
            color: white;
            padding: 20px;
            position: relative;
            overflow-x: hidden;
        }
        
        /* Partículas animadas de fondo */
        .particles {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: 0;
        }
        
        .particle {
            position: absolute;
            width: 4px;
            height: 4px;
            background: rgba(255, 255, 255, 0.5);
            border-radius: 50%;
            animation: float 6s ease-in-out infinite;
        }
        
        .particle:nth-child(1) { top: 20%; left: 10%; animation-delay: 0s; }
        .particle:nth-child(2) { top: 50%; left: 20%; animation-delay: 1s; }
        .particle:nth-child(3) { top: 80%; left: 30%; animation-delay: 2s; }
        .particle:nth-child(4) { top: 30%; left: 70%; animation-delay: 3s; }
        .particle:nth-child(5) { top: 60%; left: 80%; animation-delay: 4s; }
        .particle:nth-child(6) { top: 10%; left: 90%; animation-delay: 5s; }
        
        @keyframes float {
            0%, 100% { transform: translateY(0) rotate(0deg); }
            50% { transform: translateY(-20px) rotate(180deg); }
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            position: relative;
            z-index: 1;
        }
        
        .header {
            text-align: center;
            margin-bottom: 60px;
            animation: fadeInDown 1s ease-out;
        }
        
        .header h1 {
            font-size: clamp(3em, 6vw, 5em);
            margin-bottom: 20px;
            background: linear-gradient(45deg, #FFD700, #FFA500, #FF6B6B, #667eea);
            background-size: 300% 300%;
            animation: gradientShift 4s ease infinite;
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            font-weight: 900;
            letter-spacing: 3px;
            text-shadow: 0 4px 8px rgba(0,0,0,0.3);
        }
        
        .header p {
            font-size: 1.4em;
            opacity: 0.95;
            font-weight: 300;
            text-shadow: 0 2px 4px rgba(0,0,0,0.2);
            margin-bottom: 30px;
        }
        
        .header-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-top: 30px;
        }
        
        .stat-card {
            background: rgba(255, 255, 255, 0.1);
            padding: 25px;
            border-radius: 20px;
            backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            text-align: center;
            transition: all 0.3s ease;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 35px rgba(0,0,0,0.2);
        }
        
        .stat-number {
            font-size: 2.5em;
            font-weight: 800;
            color: #FFD700;
            margin-bottom: 10px;
        }
        
        .stat-label {
            font-size: 1em;
            opacity: 0.8;
            font-weight: 500;
        }
        
        .wallet-section {
            background: rgba(255, 255, 255, 0.15);
            padding: 40px;
            border-radius: 30px;
            margin-bottom: 50px;
            text-align: center;
            backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            animation: fadeInUp 1s ease-out 0.2s both;
            position: relative;
            overflow: hidden;
        }
        
        .wallet-section::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #FFD700, #FFA500, #FF6B6B, #667eea);
            animation: gradientShift 3s ease infinite;
        }
        
        .connect-btn {
            background: linear-gradient(45deg, #667eea, #764ba2, #f093fb);
            background-size: 300% 300%;
            animation: gradientShift 4s ease infinite;
            border: none;
            padding: 20px 50px;
            border-radius: 50px;
            color: white;
            font-size: 18px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            position: relative;
            overflow: hidden;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .connect-btn::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
            transition: left 0.6s;
        }
        
        .connect-btn:hover::before {
            left: 100%;
        }
        
        .connect-btn:hover {
            transform: translateY(-5px) scale(1.05);
            box-shadow: 0 20px 40px rgba(0,0,0,0.3);
        }
        
        .connect-btn:active {
            transform: translateY(-2px) scale(1.02);
        }
        
        .wallet-info {
            background: linear-gradient(135deg, rgba(0,0,0,0.3), rgba(0,0,0,0.1));
            padding: 20px;
            border-radius: 15px;
            margin: 20px 0;
            font-family: 'Courier New', monospace;
            font-size: 1.1em;
            border: 1px solid rgba(255, 255, 255, 0.2);
            backdrop-filter: blur(10px);
        }
        
        .lottery-section {
            background: rgba(255, 255, 255, 0.1);
            padding: 50px;
            border-radius: 30px;
            backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            animation: fadeInUp 1s ease-out 0.4s both;
        }
        
        .section-title {
            font-size: 2.5em;
            font-weight: 700;
            text-align: center;
            margin-bottom: 40px;
            background: linear-gradient(45deg, #FFD700, #FFA500);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        
        .lottery-card {
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.25), rgba(255, 255, 255, 0.1));
            padding: 40px;
            border-radius: 25px;
            margin-bottom: 30px;
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            border: 1px solid rgba(255, 255, 255, 0.3);
            box-shadow: 0 15px 35px rgba(0,0,0,0.1);
            position: relative;
            overflow: hidden;
        }
        
        .lottery-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #2ecc71, #27ae60, #00b894);
            animation: gradientShift 3s ease infinite;
        }
        
        .lottery-card:hover {
            transform: translateY(-10px) scale(1.02);
            background: rgba(255, 255, 255, 0.3);
            box-shadow: 0 25px 50px rgba(0,0,0,0.2);
        }
        
        .lottery-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
            flex-wrap: wrap;
        }
        
        .lottery-title {
            font-size: 1.8em;
            font-weight: 700;
            color: #FFD700;
            margin-bottom: 10px;
        }
        
        .lottery-status {
            display: inline-flex;
            align-items: center;
            gap: 10px;
            padding: 12px 25px;
            border-radius: 30px;
            font-size: 14px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 1px;
            animation: pulse 2s ease infinite;
        }
        
        .status-open {
            background: linear-gradient(45deg, #2ecc71, #27ae60);
            color: white;
            box-shadow: 0 6px 20px rgba(46, 204, 113, 0.4);
        }
        
        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.05); }
        }
        
        .lottery-info {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 25px;
            margin: 30px 0;
        }
        
        .info-item {
            background: rgba(255, 255, 255, 0.1);
            padding: 25px;
            border-radius: 20px;
            text-align: center;
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            transition: all 0.3s ease;
        }
        
        .info-item:hover {
            transform: translateY(-3px);
            background: rgba(255, 255, 255, 0.15);
        }
        
        .info-icon {
            font-size: 2em;
            margin-bottom: 15px;
            color: #FFD700;
        }
        
        .info-label {
            font-size: 0.9em;
            opacity: 0.8;
            margin-bottom: 10px;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .info-value {
            font-size: 1.5em;
            font-weight: 700;
            color: #FFD700;
        }
        
        .action-buttons {
            display: flex;
            gap: 20px;
            justify-content: center;
            margin-top: 30px;
            flex-wrap: wrap;
        }
        
        .play-btn {
            background: linear-gradient(45deg, #00b894, #00a085);
            border: none;
            padding: 18px 40px;
            border-radius: 30px;
            color: white;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            box-shadow: 0 8px 25px rgba(0, 184, 148, 0.3);
            position: relative;
            overflow: hidden;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .play-btn::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            background: rgba(255, 255, 255, 0.2);
            border-radius: 50%;
            transform: translate(-50%, -50%);
            transition: width 0.6s, height 0.6s;
        }
        
        .play-btn:hover::before {
            width: 300px;
            height: 300px;
        }
        
        .play-btn:hover {
            transform: translateY(-5px) scale(1.05);
            box-shadow: 0 15px 35px rgba(0, 184, 148, 0.4);
        }
        
        .tickets-btn {
            background: linear-gradient(45deg, #ff6b6b, #ee5a24);
            border: none;
            padding: 18px 40px;
            border-radius: 30px;
            color: white;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            box-shadow: 0 8px 25px rgba(255, 107, 107, 0.3);
            position: relative;
            overflow: hidden;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .tickets-btn::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            background: rgba(255, 255, 255, 0.2);
            border-radius: 50%;
            transform: translate(-50%, -50%);
            transition: width 0.6s, height 0.6s;
        }
        
        .tickets-btn:hover::before {
            width: 300px;
            height: 300px;
        }
        
        .tickets-btn:hover {
            transform: translateY(-5px) scale(1.05);
            box-shadow: 0 15px 35px rgba(255, 107, 107, 0.4);
        }
        
        .hidden { 
            display: none; 
        }
        
        /* Animaciones */
        @keyframes fadeInDown {
            from {
                opacity: 0;
                transform: translateY(-50px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(50px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        @keyframes gradientShift {
            0% { background-position: 0% 50%; }
            50% { background-position: 100% 50%; }
            100% { background-position: 0% 50%; }
        }
        
        /* Responsive Design */
        @media (max-width: 768px) {
            .container {
                padding: 0 10px;
            }
            
            .header h1 {
                font-size: 2.5em;
            }
            
            .wallet-section, .lottery-section {
                padding: 30px 20px;
            }
            
            .lottery-card {
                padding: 30px 20px;
            }
            
            .action-buttons {
                flex-direction: column;
            }
            
            .play-btn, .tickets-btn {
                width: 100%;
                margin: 5px 0;
            }
            
            .lottery-header {
                flex-direction: column;
                text-align: center;
            }
            
            .lottery-info {
                grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            }
        }
        
        /* Efectos adicionales */
        .glow {
            animation: glow 2s ease-in-out infinite alternate;
        }
        
        @keyframes glow {
            from { text-shadow: 0 0 20px rgba(255, 215, 0, 0.5); }
            to { text-shadow: 0 0 30px rgba(255, 215, 0, 0.8); }
        }
    </style>
</head>
<body>
    <!-- Partículas de fondo -->
    <div class="particles">
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
    </div>

    <div class="container">
        <div class="header">
            <h1 class="glow">🎰 BingoChain</h1>
            <p>La Primera Lotería NFT Descentralizada del Mundo</p>
            
            <div class="header-stats">
                <div class="stat-card">
                    <div class="stat-number">1</div>
                    <div class="stat-label">Sorteos Activos</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">0.01</div>
                    <div class="stat-label">ETH por Boleto</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">6</div>
                    <div class="stat-label">Números por Boleto</div>
                </div>
            </div>
        </div>

        <div class="wallet-section">
            <div id="wallet-disconnected">
                <i class="fas fa-wallet" style="font-size: 3em; margin-bottom: 20px; color: #FFD700;"></i>
                <h3 style="font-size: 1.8em; margin-bottom: 15px;">🔗 Conectar Billetera</h3>
                <p style="font-size: 1.1em; margin-bottom: 25px; opacity: 0.9;">Conecta tu MetaMask para participar en los sorteos NFT</p>
                <button class="connect-btn" onclick="connectWallet()">
                    <i class="fab fa-ethereum"></i> Conectar MetaMask
                </button>
            </div>
            
            <div id="wallet-connected" class="hidden">
                <i class="fas fa-check-circle" style="font-size: 3em; margin-bottom: 20px; color: #2ecc71;"></i>
                <h3 style="font-size: 1.8em; margin-bottom: 15px;">✅ Billetera Conectada</h3>
                <div id="wallet-info" class="wallet-info"></div>
                <button class="connect-btn" onclick="disconnectWallet()" style="background: linear-gradient(45deg, #e74c3c, #c0392b);">
                    <i class="fas fa-sign-out-alt"></i> Desconectar
                </button>
            </div>
        </div>

        <div class="lottery-section">
            <h2 class="section-title">
                <i class="fas fa-trophy"></i> Sorteos Abiertos
            </h2>
            
            <div class="lottery-card">
                <div class="lottery-header">
                    <div>
                        <div class="lottery-title">
                            <i class="fas fa-dice"></i> Sorteo Semanal #1
                        </div>
                        <div class="lottery-status status-open">
                            <i class="fas fa-play"></i> ABIERTO
                        </div>
                    </div>
                </div>
                
                <div class="lottery-info">
                    <div class="info-item">
                        <div class="info-icon"><i class="fas fa-coins"></i></div>
                        <div class="info-label">Precio del Boleto</div>
                        <div class="info-value">0.01 ETH</div>
                    </div>
                    <div class="info-item">
                        <div class="info-icon"><i class="fas fa-ticket-alt"></i></div>
                        <div class="info-label">Boletos Vendidos</div>
                        <div class="info-value">∞</div>
                    </div>
                    <div class="info-item">
                        <div class="info-icon"><i class="fas fa-gift"></i></div>
                        <div class="info-label">Premio Acumulado</div>
                        <div class="info-value">∞ ETH</div>
                    </div>
                    <div class="info-item">
                        <div class="info-icon"><i class="fas fa-clock"></i></div>
                        <div class="info-label">Tiempo Restante</div>
                        <div class="info-value">∞</div>
                    </div>
                </div>
                
                <div class="action-buttons">
                    <button class="play-btn" onclick="window.location.href = '/demo';">
                        <i class="fas fa-play"></i> Jugar Ahora
                    </button>
                    <button class="tickets-btn" onclick="window.location.href = '/mis-boletos';">
                        <i class="fas fa-ticket-alt"></i> Mis Boletos
                    </button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/web3@1.10.0/dist/web3.min.js"></script>
    <script>
        let web3, userAccount;

        async function connectWallet() {
            if (typeof window.ethereum !== 'undefined') {
                try {
                    const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
                    userAccount = accounts[0];
                    web3 = new Web3(window.ethereum);
                    
                    document.getElementById('wallet-disconnected').classList.add('hidden');
                    document.getElementById('wallet-connected').classList.remove('hidden');
                    document.getElementById('wallet-info').innerHTML = 
                        `<strong>Cuenta:</strong> ${userAccount.substring(0,10)}...${userAccount.substring(userAccount.length-8)}<br>
                         <strong>Red:</strong> ${await getNetworkName()}`;
                    
                } catch (error) {
                    console.error('Error conectando:', error);
                    alert('Error conectando MetaMask');
                }
            } else {
                alert('MetaMask no está instalado. Por favor instala MetaMask.');
            }
        }

        function disconnectWallet() {
            userAccount = null;
            web3 = null;
            document.getElementById('wallet-disconnected').classList.remove('hidden');
            document.getElementById('wallet-connected').classList.add('hidden');
        }

        async function getNetworkName() {
            try {
                const chainId = await window.ethereum.request({ method: 'eth_chainId' });
                switch (chainId) {
                    case '0x1': return 'Ethereum Mainnet';
                    case '0x539': return 'Ganache Local';
                    default: return `Red ${parseInt(chainId, 16)}`;
                }
            } catch (error) {
                return 'Desconocida';
            }
        }

        // Auto-conectar si ya está autorizado
        window.addEventListener('load', async () => {
            if (typeof window.ethereum !== 'undefined') {
                try {
                    const accounts = await window.ethereum.request({ method: 'eth_accounts' });
                    if (accounts.length > 0) {
                        await connectWallet();
                    }
                } catch (error) {
                    console.log('No hay conexión previa');
                }
            }
        });

        // Listeners para cambios en MetaMask
        if (window.ethereum) {
            window.ethereum.on('accountsChanged', (accounts) => {
                if (accounts.length === 0) {
                    disconnectWallet();
                } else {
                    connectWallet();
                }
            });

            window.ethereum.on('chainChanged', () => {
                if (userAccount) connectWallet();
            });
        }
    </script>
</body>
</html>
    '''

# Rutas para servir archivos estáticos
@app.route('/demo')
@app.route('/demo.html')
def demo():
    file_path = os.path.join(os.getcwd(), 'demo.html')
    if os.path.exists(file_path):
        return send_file(file_path)
    else:
        abort(404)

@app.route('/mis-boletos')
@app.route('/mis-boletos.html')
def mis_boletos():
    file_path = os.path.join(os.getcwd(), 'mis-boletos.html')
    if os.path.exists(file_path):
        return send_file(file_path)
    else:
        abort(404)

@app.route('/conectar-metamask')
@app.route('/conectar-metamask.html')
def conectar_metamask():
    file_path = os.path.join(os.getcwd(), 'conectar-metamask.html')
    if os.path.exists(file_path):
        return send_file(file_path)
    else:
        abort(404)

@app.route('/usar-billetera-real')
@app.route('/usar-billetera-real.html')
def usar_billetera_real():
    file_path = os.path.join(os.getcwd(), 'usar-billetera-real.html')
    if os.path.exists(file_path):
        return send_file(file_path)
    else:
        abort(404)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000, debug=True)


