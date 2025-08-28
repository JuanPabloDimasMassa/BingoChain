from flask import Flask, render_template, request, jsonify, session
from flask_cors import CORS
import requests
import json
import os
from web3 import Web3
from config import Config
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
app.config.from_object(Config)

# Initialize extensions
CORS(app, origins=["http://localhost:3000", "http://127.0.0.1:3000"])

# Initialize Web3
try:
    w3 = Web3(Web3.HTTPProvider(Config.WEB3_PROVIDER_URL))
    logger.info(f"Connected to Web3: {w3.is_connected()}")
except Exception as e:
    logger.error(f"Failed to connect to Web3: {e}")
    w3 = None

# Routes
@app.route('/')
def index():
    """Main page - Show all lotteries"""
    return render_template('index.html')

@app.route('/lottery/<int:lottery_id>')
def lottery_detail(lottery_id):
    """Lottery detail page"""
    return render_template('lottery_detail.html', lottery_id=lottery_id)

@app.route('/my-tickets')
def my_tickets():
    """Player's tickets page"""
    return render_template('my_tickets.html')

@app.route('/purchase/<int:lottery_id>')
def purchase_ticket(lottery_id):
    """Purchase ticket page"""
    return render_template('purchase_ticket.html', lottery_id=lottery_id)

# API Routes
@app.route('/api/lotteries', methods=['GET'])
def get_lotteries():
    """Get all lotteries"""
    try:
        response = requests.get(f"{Config.BACKEND_API_URL}/lotteries")
        if response.status_code == 200:
            return jsonify(response.json())
        else:
            return jsonify({"error": "Failed to fetch lotteries"}), 500
    except Exception as e:
        logger.error(f"Error fetching lotteries: {e}")
        return jsonify({"error": "Internal server error"}), 500

@app.route('/api/lotteries/<int:lottery_id>', methods=['GET'])
def get_lottery(lottery_id):
    """Get lottery by ID"""
    try:
        response = requests.get(f"{Config.BACKEND_API_URL}/lotteries/{lottery_id}")
        if response.status_code == 200:
            return jsonify(response.json())
        else:
            return jsonify({"error": "Lottery not found"}), 404
    except Exception as e:
        logger.error(f"Error fetching lottery: {e}")
        return jsonify({"error": "Internal server error"}), 500

@app.route('/api/tickets/player/<wallet_address>', methods=['GET'])
def get_player_tickets(wallet_address):
    """Get player's tickets"""
    try:
        response = requests.get(f"{Config.BACKEND_API_URL}/tickets/player/{wallet_address}")
        if response.status_code == 200:
            return jsonify(response.json())
        else:
            return jsonify({"error": "Failed to fetch tickets"}), 500
    except Exception as e:
        logger.error(f"Error fetching player tickets: {e}")
        return jsonify({"error": "Internal server error"}), 500

@app.route('/api/tickets/purchase', methods=['POST'])
def purchase_ticket_api():
    """Purchase a ticket"""
    try:
        ticket_data = request.get_json()
        response = requests.post(f"{Config.BACKEND_API_URL}/tickets/purchase", json=ticket_data)
        if response.status_code == 200:
            return jsonify(response.json())
        else:
            return jsonify({"error": "Failed to purchase ticket"}), 500
    except Exception as e:
        logger.error(f"Error purchasing ticket: {e}")
        return jsonify({"error": "Internal server error"}), 500

@app.route('/api/tickets/validate-numbers', methods=['POST'])
def validate_numbers():
    """Validate chosen numbers"""
    try:
        data = request.get_json()
        response = requests.post(f"{Config.BACKEND_API_URL}/tickets/validate-numbers", json=data)
        if response.status_code == 200:
            return jsonify(response.json())
        else:
            return jsonify({"error": "Failed to validate numbers"}), 500
    except Exception as e:
        logger.error(f"Error validating numbers: {e}")
        return jsonify({"error": "Internal server error"}), 500

@app.route('/api/wallet/connect', methods=['POST'])
def connect_wallet():
    """Connect wallet endpoint"""
    try:
        wallet_data = request.get_json()
        wallet_address = wallet_data.get('address')
        
        if not wallet_address:
            return jsonify({"error": "Wallet address is required"}), 400
        
        # Validate wallet address
        if w3 and not w3.is_address(wallet_address):
            return jsonify({"error": "Invalid wallet address"}), 400
        
        # Store wallet address in session
        session['wallet_address'] = wallet_address
        
        return jsonify({
            "success": True,
            "address": wallet_address,
            "network": "localhost" if "localhost" in Config.WEB3_PROVIDER_URL else "mainnet"
        })
        
    except Exception as e:
        logger.error(f"Error connecting wallet: {e}")
        return jsonify({"error": "Internal server error"}), 500

# WebSocket Events
@socketio.on('connect')
def handle_connect():
    """Handle client connection"""
    logger.info(f"Client connected: {request.sid}")
    emit('status', {'msg': 'Connected to BingoChain server'})

@socketio.on('disconnect')
def handle_disconnect():
    """Handle client disconnection"""
    logger.info(f"Client disconnected: {request.sid}")

@socketio.on('join_game_room')
def handle_join_game_room(data):
    """Handle joining a game room"""
    game_id = data.get('game_id')
    wallet_address = data.get('wallet_address')
    
    if game_id and wallet_address:
        room = f"game_{game_id}"
        join_room(room)
        logger.info(f"Client {request.sid} joined room {room}")
        emit('joined_room', {'room': room, 'game_id': game_id}, room=room)

@socketio.on('leave_game_room')
def handle_leave_game_room(data):
    """Handle leaving a game room"""
    game_id = data.get('game_id')
    
    if game_id:
        room = f"game_{game_id}"
        leave_room(room)
        logger.info(f"Client {request.sid} left room {room}")
        emit('left_room', {'room': room, 'game_id': game_id}, room=room)

@socketio.on('number_called')
def handle_number_called(data):
    """Handle number being called in a game"""
    game_id = data.get('game_id')
    number = data.get('number')
    
    if game_id and number:
        room = f"game_{game_id}"
        emit('number_called', {
            'game_id': game_id,
            'number': number,
            'timestamp': data.get('timestamp')
        }, room=room)

@socketio.on('bingo_claimed')
def handle_bingo_claimed(data):
    """Handle bingo being claimed"""
    game_id = data.get('game_id')
    wallet_address = data.get('wallet_address')
    
    if game_id and wallet_address:
        room = f"game_{game_id}"
        emit('bingo_claimed', {
            'game_id': game_id,
            'winner': wallet_address,
            'timestamp': data.get('timestamp')
        }, room=room)

# Error handlers
@app.errorhandler(404)
def not_found(error):
    return render_template('error.html', error="Page not found"), 404

@app.errorhandler(500)
def internal_error(error):
    return render_template('error.html', error="Internal server error"), 500

if __name__ == '__main__':
    port = int(os.environ.get('FLASK_PORT', 3000))
    app.run(host='0.0.0.0', port=port, debug=True)
