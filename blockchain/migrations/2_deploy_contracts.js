const CryptoBingo = artifacts.require("CryptoBingo");

module.exports = async function (deployer, network, accounts) {
  console.log(`Deploying to network: ${network}`);
  console.log(`Deployer account: ${accounts[0]}`);
  
  // Deploy CryptoBingo contract
  await deployer.deploy(CryptoBingo);
  const cryptoBingo = await CryptoBingo.deployed();
  
  console.log(`CryptoBingo deployed at: ${cryptoBingo.address}`);
  
  // Optional: Create a test game for development
  if (network === 'development' || network === 'ganache') {
    console.log('Creating test game...');
    
    const entryFee = web3.utils.toWei('0.01', 'ether'); // 0.01 ETH
    const maxPlayers = 10;
    
    const salesStartTime = Math.floor(Date.now() / 1000) + 60; // Start in 1 minute
    
    const tx = await cryptoBingo.createWeeklyLottery(
      "Test Weekly Lottery",
      entryFee,
      salesStartTime,
      { from: accounts[0] }
    );
    
    console.log(`Test lottery created with ID: ${tx.logs[0].args.lotteryId}`);
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
