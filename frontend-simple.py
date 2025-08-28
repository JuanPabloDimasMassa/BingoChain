#!/usr/bin/env python3
from flask import Flask, render_template_string, jsonify
from flask_cors import CORS
import os

app = Flask(__name__)
CORS(app)

# Template HTML principal
INDEX_TEMPLATE = """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🎰 BingoChain - Frontend</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { 
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        .card { 
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border: none;
            border-radius: 15px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
        }
        .card-header {
            background: linear-gradient(45deg, #667eea, #764ba2);
            border-radius: 15px 15px 0 0 !important;
        }
        .btn-primary { 
            background: linear-gradient(45deg, #667eea, #764ba2);
            border: none;
            border-radius: 10px;
            padding: 10px 20px;
            font-weight: bold;
        }
        .btn-success {
            background: linear-gradient(45deg, #56ab2f, #a8e6cf);
            border: none;
            border-radius: 10px;
            padding: 10px 20px;
            font-weight: bold;
        }
        .badge {
            border-radius: 20px;
            padding: 8px 12px;
        }
        .list-group-item {
            background: rgba(255, 255, 255, 0.7);
            border: 1px solid rgba(255, 255, 255, 0.2);
            margin-bottom: 5px;
            border-radius: 10px;
        }
        .alert {
            background: rgba(255, 255, 255, 0.9);
            border: none;
            border-radius: 10px;
        }
        .status-indicator {
            display: inline-block;
            width: 10px;
            height: 10px;
            border-radius: 50%;
            margin-right: 8px;
        }
        .status-active { background-color: #28a745; }
        .status-inactive { background-color: #dc3545; }
        .status-warning { background-color: #ffc107; }
    </style>
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-10">
                <div class="card">
                    <div class="card-header text-white text-center">
                        <h1 class="mb-2">🎰 BingoChain</h1>
                        <p class="mb-0">Lotería Blockchain con NFTs - Frontend de Control</p>
                    </div>
                    <div class="card-body">
                        
                        <!-- Accesos Principales -->
                        <div class="row mb-4">
                            <div class="col-md-6 mb-3">
                                <div class="card h-100">
                                    <div class="card-body text-center">
                                        <h5 class="card-title">🎮 Demo Principal</h5>
                                        <p class="card-text">Compra boletos NFT con MetaMask</p>
                                        <a href="http://localhost:8080/BingoChain/demo.html" 
                                           class="btn btn-primary w-100" target="_blank">
                                            🚀 Ir al Demo
                                        </a>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <div class="card h-100">
                                    <div class="card-body text-center">
                                        <h5 class="card-title">🎫 Mis Boletos NFT</h5>
                                        <p class="card-text">Ver tus NFTs comprados</p>
                                        <a href="http://localhost:8080/BingoChain/mis-boletos.html" 
                                           class="btn btn-success w-100" target="_blank">
                                            🔍 Ver Boletos
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Estado de Servicios -->
                        <div class="row">
                            <div class="col-12">
                                <div class="card">
                                    <div class="card-header">
                                        <h5 class="mb-0">📊 Estado de Servicios BingoChain</h5>
                                    </div>
                                    <div class="card-body">
                                        <div class="list-group list-group-flush">
                                            <div class="list-group-item d-flex justify-content-between align-items-center">
                                                <span>
                                                    <span class="status-indicator status-active"></span>
                                                    🎮 Demo NFT (Principal)
                                                </span>
                                                <span class="badge bg-success">Puerto 8080</span>
                                            </div>
                                            <div class="list-group-item d-flex justify-content-between align-items-center">
                                                <span>
                                                    <span class="status-indicator status-active"></span>
                                                    🔧 Backend Spring Boot
                                                </span>
                                                <span class="badge bg-success">Puerto 3500</span>
                                            </div>
                                            <div class="list-group-item d-flex justify-content-between align-items-center">
                                                <span>
                                                    <span class="status-indicator status-active"></span>
                                                    ⛓️ Blockchain Ganache
                                                </span>
                                                <span class="badge bg-success">Puerto 8545</span>
                                            </div>
                                            <div class="list-group-item d-flex justify-content-between align-items-center">
                                                <span>
                                                    <span class="status-indicator status-active"></span>
                                                    🌐 Frontend Flask (Este)
                                                </span>
                                                <span class="badge bg-info">Puerto 3000</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Información Técnica -->
                        <div class="row mt-4">
                            <div class="col-md-6">
                                <div class="card">
                                    <div class="card-header">
                                        <h6 class="mb-0">🔧 Información Técnica</h6>
                                    </div>
                                    <div class="card-body">
                                        <small>
                                            <strong>Smart Contract:</strong><br>
                                            <code>0x345cA3e014Aaf5dcA488057592ee47305D9B3e10</code><br><br>
                                            <strong>Red:</strong> Ganache Local<br>
                                            <strong>Chain ID:</strong> 1337<br>
                                            <strong>RPC:</strong> http://localhost:8545
                                        </small>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="card">
                                    <div class="card-header">
                                        <h6 class="mb-0">🚀 Guía Rápida</h6>
                                    </div>
                                    <div class="card-body">
                                        <small>
                                            <ol>
                                                <li>Haz clic en "Ir al Demo"</li>
                                                <li>Conecta MetaMask (red Ganache)</li>
                                                <li>Importa cuenta con 100 ETH</li>
                                                <li>Selecciona 6 números únicos</li>
                                                <li>¡Compra boleto NFT GRATIS!</li>
                                            </ol>
                                        </small>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Cuenta de Ganache -->
                        <div class="alert alert-warning mt-4">
                            <h6>🔑 Cuenta de Testing Ganache:</h6>
                            <div class="row">
                                <div class="col-md-6">
                                    <strong>Dirección:</strong><br>
                                    <code>0x627306090abaB3A6e1400e9345bC60c78a8BEf57</code>
                                </div>
                                <div class="col-md-6">
                                    <strong>Clave Privada:</strong><br>
                                    <code style="font-size: 10px;">0xc87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3</code>
                                </div>
                            </div>
                        </div>
                        
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
"""

@app.route('/')
def index():
    return render_template_string(INDEX_TEMPLATE)

@app.route('/health')
def health():
    return jsonify({
        "status": "ok", 
        "service": "BingoChain Frontend",
        "version": "1.0.0",
        "services": {
            "demo": "http://localhost:8080/BingoChain/demo.html",
            "nfts": "http://localhost:8080/BingoChain/mis-boletos.html",
            "backend": "http://localhost:3500",
            "blockchain": "http://localhost:8545"
        }
    })

@app.route('/status')
def status():
    return jsonify({
        "ganache": "running",
        "backend": "running", 
        "frontend": "running",
        "demo": "active"
    })

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 3000))
    print("🎰 BingoChain Frontend Simple")
    print("=" * 50)
    print(f"🌐 Frontend iniciando en: http://localhost:{port}")
    print(f"🎮 Demo principal: http://localhost:8080/BingoChain/demo.html")
    print(f"🎫 NFTs: http://localhost:8080/BingoChain/mis-boletos.html")
    print(f"🔧 Backend: http://localhost:3500")
    print("=" * 50)
    
    app.run(host='0.0.0.0', port=port, debug=True, threaded=True)
