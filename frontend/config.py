import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    # Flask Configuration
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'your-secret-key-change-this-in-production'
    
    # Backend API Configuration
    BACKEND_API_URL = os.environ.get('BACKEND_API_URL') or 'http://localhost:3500/api/v1'
    
    # Blockchain Configuration
    WEB3_PROVIDER_URL = os.environ.get('WEB3_PROVIDER_URL') or 'http://localhost:8545'
    CHAIN_ID = int(os.environ.get('CHAIN_ID') or 1337)
    GAS_LIMIT = int(os.environ.get('GAS_LIMIT') or 6721975)
    GAS_PRICE = int(os.environ.get('GAS_PRICE') or 20000000000)
    
    # Contract Addresses
    CRYPTO_BINGO_CONTRACT_ADDRESS = os.environ.get('CRYPTO_BINGO_CONTRACT_ADDRESS') or ''
    
    # WebSocket Configuration
    SOCKET_IO_ASYNC_MODE = os.environ.get('SOCKET_IO_ASYNC_MODE') or 'eventlet'
