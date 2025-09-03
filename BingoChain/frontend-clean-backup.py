#!/usr/bin/env python3
from flask import Flask, send_file, abort
import os

app = Flask(__name__)

@app.route('/')
def home():
    return '''
<!DOCTYPE html>
<html>
<head>
    <title>🎰 BingoChain - Lotería NFT</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            font-family: 'Segoe UI', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            margin: 0;
            padding: 20px;
            min-height: 100vh;
            color: white;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
        }
        .header {
            text-align: center;
            margin-bottom: 40px;
        }
        .header h1 {
            font-size: 3em;
            margin: 0;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }
        .wallet-section {
            background: rgba(255, 255, 255, 0.15);
            padding: 20px;
            border-radius: 15px;
            margin-bottom: 30px;
            text-align: center;
            backdrop-filter: blur(10px);
        }
        .connect-btn {
            background: linear-gradient(45deg, #ff6b6b, #ee5a24);
            border: none;
            padding: 15px 30px;
            border-radius: 25px;
            color: white;
            font-size: 16px;
            cursor: pointer;
            transition: all 0.3s;
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .connect-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.3);
        }
        .lottery-section {
            background: rgba(255, 255, 255, 0.1);
            padding: 30px;
            border-radius: 15px;
            backdrop-filter: blur(10px);
        }
        .lottery-card {
            background: rgba(255, 255, 255, 0.2);
            padding: 25px;
            border-radius: 12px;
            margin-bottom: 20px;
            transition: transform 0.3s;
        }
        .lottery-card:hover {
            transform: translateY(-3px);
            background: rgba(255, 255, 255, 0.25);
        }
        .lottery-status {
            display: inline-block;
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
            margin-bottom: 10px;
        }
        .status-open {
            background: #2ecc71;
            color: white;
        }
        .play-btn {
            background: linear-gradient(45deg, #00b894, #00a085);
            border: none;
            padding: 12px 25px;
            border-radius: 20px;
            color: white;
            cursor: pointer;
            transition: all 0.3s;
            margin-right: 10px;
        }
        .play-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .tickets-btn {
            background: linear-gradient(45deg, #6c5ce7, #5f3dc4);
            border: none;
            padding: 12px 25px;
            border-radius: 20px;
            color: white;
            cursor: pointer;
            transition: all 0.3s;
        }
        .tickets-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .hidden { display: none; }
        .wallet-info {
            background: rgba(0,0,0,0.2);
            padding: 10px;
            border-radius: 8px;
            margin-top: 10px;
            font-family: monospace;
            font-size: 0.9em;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎰 BingoChain</h1>
            <p>Lotería NFT Descentralizada</p>
        </div>

        <div class="wallet-section">
            <div id="wallet-disconnected">
                <h3>🔗 Conectar Billetera</h3>
                <p>Conecta tu MetaMask para participar en los sorteos</p>
                <button class="connect-btn" onclick="connectWallet()">Conectar MetaMask</button>
            </div>
            
            <div id="wallet-connected" class="hidden">
                <h3>✅ Billetera Conectada</h3>
                <div id="wallet-info" class="wallet-info"></div>
                <button class="connect-btn" onclick="disconnectWallet()">Desconectar</button>
            </div>
        </div>

        <div class="lottery-section">
            <h2>🎯 Sorteos Abiertos</h2>
            
            <div class="lottery-card">
                <div class="lottery-status status-open">ABIERTO</div>
                <h3>🎲 Sorteo Semanal #1</h3>
                <p><strong>Premio:</strong> 100 ETH en NFTs</p>
                <p><strong>Precio por boleto:</strong> 0.01 ETH</p>
                <p><strong>Números a elegir:</strong> 6 números del 1 al 100</p>
                <p><strong>Cierre de ventas:</strong> Lunes 12:00 hs</p>
                <p><strong>Sorteos diarios:</strong> 12:30 hs por 6 días</p>
                
                <div style="margin-top: 20px;">
                    <button class="play-btn" onclick="playLottery(1)">🎮 Jugar Ahora</button>
                    <button class="tickets-btn" onclick="viewMyTickets(1)">🎫 Mis Boletos</button>
                </div>
            </div>
        </div>
    </div>

    <script>
        let userAccount = null;
        let web3 = null;

        // Conexión MetaMask
        async function connectWallet() {
            if (typeof window.ethereum !== 'undefined') {
                try {
                    const accounts = await window.ethereum.request({ 
                        method: 'eth_requestAccounts' 
                    });
                    userAccount = accounts[0];
                    
                    document.getElementById('wallet-disconnected').classList.add('hidden');
                    document.getElementById('wallet-connected').classList.remove('hidden');
                    document.getElementById('wallet-info').innerHTML = 
                        `Cuenta: ${userAccount.substring(0,10)}...${userAccount.substring(userAccount.length-8)}`;
                    
                    console.log('Conectado:', userAccount);
                } catch (error) {
                    alert('Error conectando MetaMask: ' + error.message);
                }
            } else {
                alert('MetaMask no está instalado. Por favor instala MetaMask.');
            }
        }

        function disconnectWallet() {
            userAccount = null;
            document.getElementById('wallet-connected').classList.add('hidden');
            document.getElementById('wallet-disconnected').classList.remove('hidden');
        }

        function playLottery(lotteryId) {
            if (!userAccount) {
                alert('Por favor conecta tu billetera MetaMask primero');
                return;
            }
            // Redirigir al demo en la misma ventana (mismo puerto)
            window.location.href = '/demo';
        }

        function viewMyTickets(lotteryId) {
            if (!userAccount) {
                alert('Por favor conecta tu billetera MetaMask primero');
                return;
            }
            // Redirigir a mis boletos en la misma ventana (mismo puerto)
            window.location.href = '/mis-boletos';
        }

        // Auto-conectar si ya está autorizado
        window.addEventListener('load', async () => {
            if (typeof window.ethereum !== 'undefined') {
                const accounts = await window.ethereum.request({ method: 'eth_accounts' });
                if (accounts.length > 0) {
                    userAccount = accounts[0];
                    document.getElementById('wallet-disconnected').classList.add('hidden');
                    document.getElementById('wallet-connected').classList.remove('hidden');
                    document.getElementById('wallet-info').innerHTML = 
                        `Cuenta: ${userAccount.substring(0,10)}...${userAccount.substring(userAccount.length-8)}`;
                }
            }
        });
    </script>
</body>
</html>
    '''

@app.route('/health')
def health():
    return {'status': 'ok', 'service': 'BingoChain Frontend Clean'}

# Rutas para archivos estáticos
@app.route('/demo')
@app.route('/demo.html')
def demo():
    try:
        return send_file('demo.html')
    except FileNotFoundError:
        abort(404)

@app.route('/mis-boletos')
@app.route('/mis-boletos.html')
def mis_boletos():
    try:
        return send_file('mis-boletos.html')
    except FileNotFoundError:
        abort(404)

@app.route('/conectar-metamask')
@app.route('/conectar-metamask.html')
def conectar_metamask():
    try:
        return send_file('conectar-metamask.html')
    except FileNotFoundError:
        abort(404)

@app.route('/usar-billetera-real')
@app.route('/usar-billetera-real.html')
def usar_billetera_real():
    try:
        return send_file('usar-billetera-real.html')
    except FileNotFoundError:
        abort(404)

if __name__ == '__main__':
    print("🎰 BingoChain Frontend Limpio")
    print("🌐 Iniciando en: http://localhost:3000")
    app.run(host='0.0.0.0', port=3000, debug=False)
