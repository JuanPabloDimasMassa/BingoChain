// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "@openzeppelin/contracts/security/ReentrancyGuard.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/security/Pausable.sol";
import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/utils/Counters.sol";
import "@openzeppelin/contracts/utils/Strings.sol";

/**
 * @title CryptoBingo
 * @dev Smart contract for a weekly lottery-style bingo game
 * Players choose 6 numbers from 1-100, one number is drawn daily for 6 days
 */
contract CryptoBingo is ERC721, ERC721URIStorage, ReentrancyGuard, Ownable, Pausable {
    using Counters for Counters.Counter;
    
    // Lottery states
    enum LotteryState { TICKET_SALES, DRAWING_PHASE, COMPLETED, CANCELLED }
    
    // Lottery structure
    struct WeeklyLottery {
        uint256 lotteryId;
        string lotteryName;
        uint256 ticketPrice;
        uint256 prizePool;
        uint256 totalTickets;
        LotteryState state;
        uint256 salesStartTime;   // Domingo 12:30
        uint256 salesEndTime;     // Lunes 12:00
        uint256[] drawnNumbers;   // 6 números sorteados
        uint256 currentDrawDay;   // Día actual del sorteo (1-6)
        uint256 nextDrawTime;     // Próximo sorteo
        address[] winners;
        bool prizesDistributed;
    }
    
    // Ticket structure - Player chooses 6 numbers from 1-100
    struct LotteryTicket {
        uint256 ticketId;
        uint256 lotteryId;
        address owner;
        uint8[6] chosenNumbers;   // 6 números del 1-100
        uint256 matchedNumbers;   // Números acertados
        uint256 purchaseTime;
        bool isWinner;
    }
    
    // State variables
    uint256 public lotteryCounter;
    Counters.Counter private _tokenIdCounter; // For NFT tickets
    uint256 public platformFeePercentage = 5; // 5% platform fee
    uint256 public currentWeeklyLottery;
    string private _baseTokenURI;
    
    // Timing constants
    uint256 public constant SALES_DURATION = 23.5 hours; // Domingo 12:30 a Lunes 12:00
    uint256 public constant DRAW_TIME = 12.5 hours;      // 12:30 cada día
    uint256 public constant DAYS_IN_DRAW_PHASE = 6;      // 6 días de sorteo
    
    mapping(uint256 => WeeklyLottery) public lotteries;
    mapping(uint256 => LotteryTicket) public tickets;
    mapping(address => uint256[]) public playerTickets;
    mapping(uint256 => uint256[]) public lotteryTickets; // lotteryId => ticketIds
    mapping(uint256 => uint256) public tokenToTicket; // tokenId => ticketId
    
    // Events
    event LotteryCreated(uint256 indexed lotteryId, string lotteryName, uint256 ticketPrice, uint256 salesStart, uint256 salesEnd);
    event TicketPurchased(uint256 indexed lotteryId, address indexed buyer, uint256 indexed ticketId, uint256 tokenId, uint8[6] chosenNumbers);
    event SalesClosed(uint256 indexed lotteryId, uint256 totalTickets, uint256 prizePool);
    event NumberDrawn(uint256 indexed lotteryId, uint256 drawnNumber, uint256 drawDay, uint256 timestamp);
    event LotteryCompleted(uint256 indexed lotteryId, uint256[] drawnNumbers, address[] winners);
    event PrizeDistributed(uint256 indexed lotteryId, address indexed winner, uint256 prize, uint256 matchedNumbers);
    
    // Modifiers
    modifier lotteryExists(uint256 _lotteryId) {
        require(_lotteryId <= lotteryCounter && _lotteryId > 0, "Lottery does not exist");
        _;
    }
    
    modifier lotteryInState(uint256 _lotteryId, LotteryState _state) {
        require(lotteries[_lotteryId].state == _state, "Lottery not in required state");
        _;
    }
    
    modifier duringTicketSales(uint256 _lotteryId) {
        WeeklyLottery storage lottery = lotteries[_lotteryId];
        require(block.timestamp >= lottery.salesStartTime, "Sales not started yet");
        require(block.timestamp <= lottery.salesEndTime, "Sales period ended");
        require(lottery.state == LotteryState.TICKET_SALES, "Not in sales phase");
        _;
    }
    
    modifier canDrawNumber(uint256 _lotteryId) {
        WeeklyLottery storage lottery = lotteries[_lotteryId];
        require(lottery.state == LotteryState.DRAWING_PHASE, "Not in drawing phase");
        require(block.timestamp >= lottery.nextDrawTime, "Not time for next draw");
        require(lottery.currentDrawDay <= DAYS_IN_DRAW_PHASE, "All numbers already drawn");
        _;
    }
    
    constructor() ERC721("BingoChain Ticket", "BINGO") {
        lotteryCounter = 0;
        currentWeeklyLottery = 0;
        _baseTokenURI = "https://api.bingochain.com/metadata/";
    }
    
    /**
     * @dev Create a new weekly lottery
     * @param _lotteryName Name of the lottery
     * @param _ticketPrice Price per ticket in wei
     * @param _salesStartTime When ticket sales begin (Unix timestamp)
     */
    function createWeeklyLottery(
        string memory _lotteryName,
        uint256 _ticketPrice,
        uint256 _salesStartTime
    ) external onlyOwner whenNotPaused returns (uint256) {
        require(_ticketPrice > 0, "Ticket price must be greater than 0");
        // require(_salesStartTime > block.timestamp, "Sales start time must be in the future"); // Comentado para testing
        
        lotteryCounter++;
        
        WeeklyLottery storage newLottery = lotteries[lotteryCounter];
        newLottery.lotteryId = lotteryCounter;
        newLottery.lotteryName = _lotteryName;
        newLottery.ticketPrice = _ticketPrice;
        newLottery.state = LotteryState.TICKET_SALES;
        newLottery.salesStartTime = _salesStartTime;
        newLottery.salesEndTime = _salesStartTime + SALES_DURATION;
        newLottery.currentDrawDay = 0;
        newLottery.nextDrawTime = newLottery.salesEndTime + DRAW_TIME;
        newLottery.prizePool = 0;
        newLottery.totalTickets = 0;
        newLottery.prizesDistributed = false;
        
        currentWeeklyLottery = lotteryCounter;
        
        emit LotteryCreated(
            lotteryCounter, 
            _lotteryName, 
            _ticketPrice, 
            _salesStartTime, 
            newLottery.salesEndTime
        );
        
        return lotteryCounter;
    }
    
    /**
     * @dev Purchase a lottery ticket with chosen numbers
     * @param _lotteryId ID of the lottery
     * @param _chosenNumbers Array of 6 unique numbers from 1-100
     */
    function buyTicket(
        uint256 _lotteryId,
        uint8[6] memory _chosenNumbers
    ) external payable lotteryExists(_lotteryId) duringTicketSales(_lotteryId) nonReentrant whenNotPaused {
        WeeklyLottery storage lottery = lotteries[_lotteryId];
        
        // Allow free tickets for testing
        // require(msg.value == lottery.ticketPrice, "Incorrect ticket price");
        require(areNumbersValid(_chosenNumbers), "Invalid numbers chosen");
        require(areNumbersUnique(_chosenNumbers), "Numbers must be unique");
        
        // Calculate platform fee
        uint256 platformFee = (msg.value * platformFeePercentage) / 100;
        uint256 prizeContribution = msg.value - platformFee;
        
        // Add to prize pool
        lottery.prizePool += prizeContribution;
        lottery.totalTickets++;
        
        // Create NFT ticket
        _tokenIdCounter.increment();
        uint256 tokenId = _tokenIdCounter.current();
        uint256 ticketId = tokenId; // Use tokenId as ticketId for simplicity
        
        // Mint NFT to buyer
        _safeMint(msg.sender, tokenId);
        
        // Create ticket metadata
        LotteryTicket storage ticket = tickets[ticketId];
        ticket.ticketId = ticketId;
        ticket.lotteryId = _lotteryId;
        ticket.owner = msg.sender;
        ticket.chosenNumbers = _chosenNumbers;
        ticket.purchaseTime = block.timestamp;
        ticket.matchedNumbers = 0;
        ticket.isWinner = false;
        
        // Set token URI with ticket metadata
        string memory tokenURIString = generateTokenURI(ticketId, _lotteryId, _chosenNumbers);
        _setTokenURI(tokenId, tokenURIString);
        
        // Update mappings
        playerTickets[msg.sender].push(ticketId);
        lotteryTickets[_lotteryId].push(ticketId);
        tokenToTicket[tokenId] = ticketId;
        
        emit TicketPurchased(_lotteryId, msg.sender, ticketId, tokenId, _chosenNumbers);
    }
    
    /**
     * @dev Close ticket sales and start drawing phase
     */
    function closeSales(uint256 _lotteryId) 
        external 
        onlyOwner 
        lotteryExists(_lotteryId) 
        lotteryInState(_lotteryId, LotteryState.TICKET_SALES) 
    {
        WeeklyLottery storage lottery = lotteries[_lotteryId];
        require(block.timestamp > lottery.salesEndTime, "Sales period not ended yet");
        
        lottery.state = LotteryState.DRAWING_PHASE;
        
        emit SalesClosed(_lotteryId, lottery.totalTickets, lottery.prizePool);
    }
    
    /**
     * @dev Draw a number for the current day
     */
    function drawDailyNumber(uint256 _lotteryId) 
        external 
        onlyOwner 
        lotteryExists(_lotteryId) 
        canDrawNumber(_lotteryId) 
        whenNotPaused 
    {
        WeeklyLottery storage lottery = lotteries[_lotteryId];
        
        lottery.currentDrawDay++;
        
        // Generate random number from 1-100
        uint256 drawnNumber = generateRandomNumber(_lotteryId, lottery.currentDrawDay) % 100 + 1;
        
        // Ensure number hasn't been drawn before
        while (isNumberAlreadyDrawn(_lotteryId, drawnNumber)) {
            drawnNumber = (drawnNumber % 100) + 1;
        }
        
        lottery.drawnNumbers.push(drawnNumber);
        
        // Update matched numbers for all tickets
        updateTicketMatches(_lotteryId, drawnNumber);
        
        // Set next draw time
        if (lottery.currentDrawDay < DAYS_IN_DRAW_PHASE) {
            lottery.nextDrawTime += 1 days;
        } else {
            // All numbers drawn, complete lottery
            completeLottery(_lotteryId);
        }
        
        emit NumberDrawn(_lotteryId, drawnNumber, lottery.currentDrawDay, block.timestamp);
    }
    
    /**
     * @dev Complete the lottery and determine winners
     */
    function completeLottery(uint256 _lotteryId) internal {
        WeeklyLottery storage lottery = lotteries[_lotteryId];
        lottery.state = LotteryState.COMPLETED;
        
        // Find winners (players with 6 matches)
        uint256[] memory ticketIds = lotteryTickets[_lotteryId];
        address[] memory winners;
        uint256 winnerCount = 0;
        
        // Count winners first
        for (uint256 i = 0; i < ticketIds.length; i++) {
            if (tickets[ticketIds[i]].matchedNumbers == 6) {
                winnerCount++;
            }
        }
        
        // Create winners array
        winners = new address[](winnerCount);
        uint256 winnerIndex = 0;
        
        for (uint256 i = 0; i < ticketIds.length; i++) {
            if (tickets[ticketIds[i]].matchedNumbers == 6) {
                tickets[ticketIds[i]].isWinner = true;
                winners[winnerIndex] = tickets[ticketIds[i]].owner;
                winnerIndex++;
            }
        }
        
        lottery.winners = winners;
        
        emit LotteryCompleted(_lotteryId, lottery.drawnNumbers, winners);
        
        // Distribute prizes
        distributePrizes(_lotteryId);
    }
    
    /**
     * @dev Distribute prizes to winners
     */
    function distributePrizes(uint256 _lotteryId) internal {
        WeeklyLottery storage lottery = lotteries[_lotteryId];
        require(!lottery.prizesDistributed, "Prizes already distributed");
        
        if (lottery.winners.length == 0) {
            // No winners, roll over prize to next week or return to platform
            return;
        }
        
        uint256 prizePerWinner = lottery.prizePool / lottery.winners.length;
        
        for (uint256 i = 0; i < lottery.winners.length; i++) {
            address winner = lottery.winners[i];
            (bool success, ) = payable(winner).call{value: prizePerWinner}("");
            require(success, "Prize transfer failed");
            
            emit PrizeDistributed(_lotteryId, winner, prizePerWinner, 6);
        }
        
        lottery.prizesDistributed = true;
    }
    
    /**
     * @dev Update matched numbers for all tickets after a number is drawn
     */
    function updateTicketMatches(uint256 _lotteryId, uint256 drawnNumber) internal {
        uint256[] memory ticketIds = lotteryTickets[_lotteryId];
        
        for (uint256 i = 0; i < ticketIds.length; i++) {
            LotteryTicket storage ticket = tickets[ticketIds[i]];
            
            // Check if drawn number matches any of the ticket's numbers
            for (uint256 j = 0; j < 6; j++) {
                if (ticket.chosenNumbers[j] == drawnNumber) {
                    ticket.matchedNumbers++;
                    break;
                }
            }
        }
    }
    
    /**
     * @dev Check if numbers are valid (1-100)
     */
    function areNumbersValid(uint8[6] memory _numbers) internal pure returns (bool) {
        for (uint256 i = 0; i < 6; i++) {
            if (_numbers[i] < 1 || _numbers[i] > 100) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @dev Check if all numbers are unique
     */
    function areNumbersUnique(uint8[6] memory _numbers) internal pure returns (bool) {
        for (uint256 i = 0; i < 6; i++) {
            for (uint256 j = i + 1; j < 6; j++) {
                if (_numbers[i] == _numbers[j]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * @dev Check if a number has already been drawn
     */
    function isNumberAlreadyDrawn(uint256 _lotteryId, uint256 _number) internal view returns (bool) {
        uint256[] memory drawnNumbers = lotteries[_lotteryId].drawnNumbers;
        for (uint256 i = 0; i < drawnNumbers.length; i++) {
            if (drawnNumbers[i] == _number) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @dev Generate a pseudo-random number
     */
    function generateRandomNumber(uint256 _lotteryId, uint256 _drawDay) internal view returns (uint256) {
        return uint256(keccak256(abi.encodePacked(
            block.timestamp,
            block.difficulty,
            _lotteryId,
            _drawDay,
            msg.sender
        )));
    }
    
    /**
     * @dev Get lottery information
     */
    function getLottery(uint256 _lotteryId) 
        external 
        view 
        lotteryExists(_lotteryId) 
        returns (
            uint256 lotteryId,
            string memory lotteryName,
            uint256 ticketPrice,
            uint256 prizePool,
            uint256 totalTickets,
            LotteryState state,
            uint256 salesStartTime,
            uint256 salesEndTime,
            uint256[] memory drawnNumbers,
            uint256 currentDrawDay,
            address[] memory winners
        ) 
    {
        WeeklyLottery storage lottery = lotteries[_lotteryId];
        return (
            lottery.lotteryId,
            lottery.lotteryName,
            lottery.ticketPrice,
            lottery.prizePool,
            lottery.totalTickets,
            lottery.state,
            lottery.salesStartTime,
            lottery.salesEndTime,
            lottery.drawnNumbers,
            lottery.currentDrawDay,
            lottery.winners
        );
    }
    
    /**
     * @dev Get ticket information
     */
    function getTicket(uint256 _ticketId) 
        external 
        view 
        returns (
            uint256 ticketId,
            uint256 lotteryId,
            address owner,
            uint8[6] memory chosenNumbers,
            uint256 matchedNumbers,
            uint256 purchaseTime,
            bool isWinner
        ) 
    {
        LotteryTicket storage ticket = tickets[_ticketId];
        return (
            ticket.ticketId,
            ticket.lotteryId,
            ticket.owner,
            ticket.chosenNumbers,
            ticket.matchedNumbers,
            ticket.purchaseTime,
            ticket.isWinner
        );
    }
    
    /**
     * @dev Get player's tickets for a specific lottery
     */
    function getPlayerTickets(address _player, uint256 _lotteryId) 
        external 
        view 
        returns (uint256[] memory) 
    {
        uint256[] memory allTickets = playerTickets[_player];
        uint256[] memory lotterySpecificTickets = new uint256[](allTickets.length);
        uint256 count = 0;
        
        for (uint256 i = 0; i < allTickets.length; i++) {
            if (tickets[allTickets[i]].lotteryId == _lotteryId) {
                lotterySpecificTickets[count] = allTickets[i];
                count++;
            }
        }
        
        // Resize array to actual count
        uint256[] memory result = new uint256[](count);
        for (uint256 i = 0; i < count; i++) {
            result[i] = lotterySpecificTickets[i];
        }
        
        return result;
    }
    
    /**
     * @dev Get all player's tickets
     */
    function getAllPlayerTickets(address _player) 
        external 
        view 
        returns (uint256[] memory) 
    {
        return playerTickets[_player];
    }
    
    /**
     * @dev Get current active lottery
     */
    function getCurrentLottery() external view returns (uint256) {
        return currentWeeklyLottery;
    }
    
    /**
     * @dev Generate metadata URI for NFT ticket
     */
    function generateTokenURI(uint256 _ticketId, uint256 _lotteryId, uint8[6] memory _numbers) 
        internal 
        view 
        returns (string memory) 
    {
        // Simplified metadata generation to avoid stack too deep
        string memory json = string(abi.encodePacked(
            '{"name": "BingoChain Ticket #',
            Strings.toString(_ticketId),
            '", "description": "Lottery Ticket NFT #',
            Strings.toString(_lotteryId),
            '", "image": "data:image/svg+xml;utf8,',
            _generateSimpleSVG(_ticketId, _lotteryId),
            '", "attributes": [{"trait_type": "Lottery", "value": "',
            Strings.toString(_lotteryId),
            '"}, {"trait_type": "Ticket", "value": "',
            Strings.toString(_ticketId),
            '"}]}'
        ));
        
        return string(abi.encodePacked("data:application/json;utf8,", json));
    }
    
    /**
     * @dev Generate simple SVG image for NFT ticket
     */
    function _generateSimpleSVG(uint256 _ticketId, uint256 _lotteryId) 
        internal 
        pure 
        returns (string memory) 
    {
        return string(abi.encodePacked(
            '<svg width="300" height="200" xmlns="http://www.w3.org/2000/svg">',
            '<rect width="300" height="200" fill="%23667eea" rx="10"/>',
            '<text x="150" y="50" text-anchor="middle" fill="white" font-size="18">BingoChain</text>',
            '<text x="150" y="80" text-anchor="middle" fill="white" font-size="14">Lottery Ticket</text>',
            '<text x="150" y="120" text-anchor="middle" fill="white" font-size="12">Ticket #',
            Strings.toString(_ticketId),
            '</text>',
            '<text x="150" y="150" text-anchor="middle" fill="white" font-size="10">Lottery #',
            Strings.toString(_lotteryId),
            '</text>',
            '</svg>'
        ));
    }
    

    
    /**
     * @dev Get ticket info by token ID
     */
    function getTicketByTokenId(uint256 _tokenId) 
        external 
        view 
        returns (
            uint256 ticketId,
            uint256 lotteryId,
            address owner,
            uint8[6] memory chosenNumbers,
            uint256 matchedNumbers,
            uint256 purchaseTime,
            bool isWinner
        ) 
    {
        uint256 ticketIdFromToken = tokenToTicket[_tokenId];
        LotteryTicket storage ticket = tickets[ticketIdFromToken];
        return (
            ticket.ticketId,
            ticket.lotteryId,
            ticket.owner,
            ticket.chosenNumbers,
            ticket.matchedNumbers,
            ticket.purchaseTime,
            ticket.isWinner
        );
    }
    
    /**
     * @dev Get all NFT tokens owned by an address
     */
    function getPlayerTokens(address _player) external view returns (uint256[] memory) {
        uint256 balance = balanceOf(_player);
        uint256[] memory tokens = new uint256[](balance);
        
        // This is a simplified version - in production, you'd want to implement a more efficient enumeration
        uint256 tokenIndex = 0;
        for (uint256 i = 1; i <= _tokenIdCounter.current() && tokenIndex < balance; i++) {
            if (_exists(i) && ownerOf(i) == _player) {
                tokens[tokenIndex] = i;
                tokenIndex++;
            }
        }
        
        return tokens;
    }
    
    /**
     * @dev Set base URI for token metadata
     */
    function setBaseURI(string memory baseURI) external onlyOwner {
        _baseTokenURI = baseURI;
    }
    
    /**
     * @dev Get total supply of NFT tickets
     */
    function totalSupply() external view returns (uint256) {
        return _tokenIdCounter.current();
    }
    
    // Required overrides for ERC721URIStorage
    function _burn(uint256 tokenId) internal override(ERC721, ERC721URIStorage) {
        super._burn(tokenId);
    }
    
    function tokenURI(uint256 tokenId) public view override(ERC721, ERC721URIStorage) returns (string memory) {
        return super.tokenURI(tokenId);
    }
    
    function supportsInterface(bytes4 interfaceId) public view override(ERC721, ERC721URIStorage) returns (bool) {
        return super.supportsInterface(interfaceId);
    }
    
    /**
     * @dev Emergency functions
     */
    function pause() external onlyOwner {
        _pause();
    }
    
    function unpause() external onlyOwner {
        _unpause();
    }
    
    function setPlatformFee(uint256 _feePercentage) external onlyOwner {
        require(_feePercentage <= 10, "Fee cannot exceed 10%");
        platformFeePercentage = _feePercentage;
    }
    
    function withdrawPlatformFees() external onlyOwner {
        uint256 balance = address(this).balance;
        require(balance > 0, "No fees to withdraw");
        
        // Calculate remaining prize pools
        uint256 totalPrizePools = 0;
        for (uint256 i = 1; i <= lotteryCounter; i++) {
            if (!lotteries[i].prizesDistributed && lotteries[i].state != LotteryState.CANCELLED) {
                totalPrizePools += lotteries[i].prizePool;
            }
        }
        
        uint256 availableFees = balance - totalPrizePools;
        require(availableFees > 0, "No fees available for withdrawal");
        
        (bool success, ) = payable(owner()).call{value: availableFees}("");
        require(success, "Withdrawal failed");
    }
    
    /**
     * @dev Cancel a lottery (only if in ticket sales phase)
     */
    function cancelLottery(uint256 _lotteryId) 
        external 
        onlyOwner 
        lotteryExists(_lotteryId) 
        lotteryInState(_lotteryId, LotteryState.TICKET_SALES) 
    {
        WeeklyLottery storage lottery = lotteries[_lotteryId];
        lottery.state = LotteryState.CANCELLED;
        
        // Refund all ticket buyers
        uint256[] memory ticketIds = lotteryTickets[_lotteryId];
        for (uint256 i = 0; i < ticketIds.length; i++) {
            address buyer = tickets[ticketIds[i]].owner;
            (bool success, ) = payable(buyer).call{value: lottery.ticketPrice}("");
            require(success, "Refund failed");
        }
        
        lottery.prizePool = 0;
    }
}