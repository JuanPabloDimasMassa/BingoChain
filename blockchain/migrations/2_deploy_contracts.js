const CryptoBingo = artifacts.require("CryptoBingo");

module.exports = async function (deployer, network, accounts) {
  console.log(`Deploying to network: ${network}`);
  console.log(`Deployer account: ${accounts[0]}`);
  
  // Deploy CryptoBingo contract
  await deployer.deploy(CryptoBingo);
  const cryptoBingo = await CryptoBingo.deployed();
  
  console.log(`CryptoBingo deployed at: ${cryptoBingo.address}`);
  
  // Create a test lottery for development
  if (network === 'development' || network === 'ganache') {
    console.log('Creating test lottery...');
    
    const ticketPrice = web3.utils.toWei('0.01', 'ether'); // 0.01 ETH
    const currentTime = Math.floor(Date.now() / 1000);
    const salesStartTime = currentTime - 3600; // Started 1 hour ago
    
    try {
      const tx = await cryptoBingo.createWeeklyLottery(
        "Sorteo Semanal #1 - BingoChain",
        ticketPrice,
        salesStartTime,
        { from: accounts[0], gas: 2000000 }
      );
      
      console.log(`Test lottery created with ID: 1`);
      console.log(`Ticket price: 0.01 ETH`);
      console.log(`Sales started at: ${new Date(salesStartTime * 1000).toLocaleString()}`);
      
    } catch (error) {
      console.log('Error creating lottery:', error.message);
    }
  }
  
  // Save deployment info to a file
  const fs = require('fs');
  const path = require('path');
  
  const deploymentInfo = {
    network: network,
    contractAddress: cryptoBingo.address,
    deployer: accounts[0],
    deploymentTime: new Date().toISOString(),
    transactionHash: cryptoBingo.transactionHash
  };
  
  const deploymentPath = path.join(__dirname, '../deployments');
  if (!fs.existsSync(deploymentPath)) {
    fs.mkdirSync(deploymentPath, { recursive: true });
  }
  
  fs.writeFileSync(
    path.join(deploymentPath, `${network}.json`),
    JSON.stringify(deploymentInfo, null, 2)
  );
  
  console.log(`Deployment info saved to deployments/${network}.json`);
};
