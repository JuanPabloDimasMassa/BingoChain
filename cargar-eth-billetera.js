// Script para cargar ETH a tu billetera real en Ganache
const Web3 = require('web3');

// Conectar a Ganache
const web3 = new Web3('http://127.0.0.1:8545');

// Tu dirección real
const tuDireccion = '0x51F6EB8bF0FC46c783527136a3B659EDFA1719C7';

// Cuenta de Ganache con ETH (para transferir)
const cuentaGanache = '0x627306090abaB3A6e1400e9345bC60c78a8BEf57';
const clavePrivadaGanache = '0xc87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3';

async function cargarETH() {
    try {
        console.log('🚀 Cargando 10 ETH a tu billetera...');
        
        // Crear cuenta desde clave privada
        const cuenta = web3.eth.accounts.privateKeyToAccount(clavePrivadaGanache);
        web3.eth.accounts.wallet.add(cuenta);
        
        // Verificar balance inicial
        const balanceInicial = await web3.eth.getBalance(tuDireccion);
        console.log(`💰 Balance inicial: ${web3.utils.fromWei(balanceInicial, 'ether')} ETH`);
        
        // Transferir 10 ETH
        const cantidadETH = web3.utils.toWei('10', 'ether');
        
        const transaccion = await web3.eth.sendTransaction({
            from: cuentaGanache,
            to: tuDireccion,
            value: cantidadETH,
            gas: 21000,
            gasPrice: web3.utils.toWei('20', 'gwei')
        });
        
        console.log(`✅ Transacción exitosa: ${transaccion.transactionHash}`);
        
        // Verificar balance final
        const balanceFinal = await web3.eth.getBalance(tuDireccion);
        console.log(`🎉 Balance final: ${web3.utils.fromWei(balanceFinal, 'ether')} ETH`);
        
        console.log('\n🎯 ¡Tu billetera ya tiene ETH para comprar boletos!');
        console.log(`📱 Dirección: ${tuDireccion}`);
        console.log(`💎 Balance: ${web3.utils.fromWei(balanceFinal, 'ether')} ETH`);
        
    } catch (error) {
        console.error('❌ Error cargando ETH:', error.message);
    }
}

// Ejecutar
cargarETH();
