package com.bingochain.controller;

import com.bingochain.model.LotteryTicket;
import com.bingochain.service.LotteryTicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080", "http://127.0.0.1:8080"})
@Tag(name = "Tickets", description = "🎫 API para gestión de boletos NFT")
public class LotteryTicketController {

    @Autowired
    private LotteryTicketService lotteryTicketService;

    private static final Logger logger = LoggerFactory.getLogger(LotteryTicketController.class);

    @GetMapping("/{id}")
    @Operation(
        summary = "🎫 Obtener boleto por ID",
        description = "Retorna un boleto específico usando su ID interno de la base de datos",
        tags = {"Tickets"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Boleto encontrado"),
        @ApiResponse(responseCode = "404", description = "❌ Boleto no encontrado")
    })
    public ResponseEntity<LotteryTicket> getTicketById(
        @Parameter(description = "ID interno del boleto", example = "1")
        @PathVariable Long id) {
        Optional<LotteryTicket> ticket = lotteryTicketService.getTicketById(id);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/contract/{ticketId}")
    @Operation(
        summary = "🎫 Obtener boleto por ID del contrato",
        description = "Retorna un boleto específico usando su ID del contrato inteligente",
        tags = {"Tickets"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Boleto encontrado"),
        @ApiResponse(responseCode = "404", description = "❌ Boleto no encontrado")
    })
    public ResponseEntity<LotteryTicket> getTicketByTicketId(
        @Parameter(description = "ID del boleto en el contrato", example = "TICKET_001")
        @PathVariable String ticketId) {
        Optional<LotteryTicket> ticket = lotteryTicketService.getTicketByTicketId(ticketId);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/player/{walletAddress}")
    @Operation(
        summary = "👤 Obtener boletos de un jugador",
        description = "Retorna todos los boletos comprados por una dirección de wallet específica",
        tags = {"Tickets"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Boletos obtenidos exitosamente")
    })
    public ResponseEntity<List<LotteryTicket>> getPlayerTickets(
        @Parameter(description = "Dirección de wallet del jugador", example = "0x1234567890123456789012345678901234567890")
        @PathVariable String walletAddress) {
        List<LotteryTicket> tickets = lotteryTicketService.getPlayerTickets(walletAddress);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/player/{walletAddress}/lottery/{lotteryId}")
    @Operation(
        summary = "👤🎲 Obtener boletos de un jugador en sorteo específico",
        description = "Retorna los boletos de un jugador para un sorteo en particular",
        tags = {"Tickets"}
    )
    public ResponseEntity<List<Map<String, Object>>> getPlayerTicketsForLottery(
        @Parameter(description = "Dirección de wallet del jugador", example = "0x1234567890123456789012345678901234567890")
        @PathVariable String walletAddress,
        @Parameter(description = "ID del sorteo", example = "1")
        @PathVariable Long lotteryId) {
        List<LotteryTicket> tickets = lotteryTicketService.getPlayerTicketsForLottery(walletAddress, lotteryId);
        
        // Convertir a formato JSON controlado para evitar problemas de serialización
        List<Map<String, Object>> ticketsResponse = new ArrayList<>();
        for (LotteryTicket ticket : tickets) {
            Map<String, Object> ticketMap = new HashMap<>();
            ticketMap.put("id", ticket.getId());
            ticketMap.put("ticketId", ticket.getTicketId());
            ticketMap.put("walletAddress", ticket.getWalletAddress());
            ticketMap.put("lotteryId", ticket.getWeeklyLottery() != null ? ticket.getWeeklyLottery().getId() : lotteryId);
            ticketMap.put("chosenNumbers", ticket.getChosenNumbers());
            ticketMap.put("matchedNumbers", ticket.getMatchedNumbers());
            ticketMap.put("ticketPricePaid", ticket.getTicketPricePaid());
            ticketMap.put("isWinner", ticket.getIsWinner());
            ticketMap.put("prizeAmount", ticket.getPrizeAmount());
            ticketMap.put("transactionHash", ticket.getTransactionHash());
            ticketMap.put("purchasedAt", ticket.getPurchasedAt());
            ticketMap.put("updatedAt", ticket.getUpdatedAt());
            ticketsResponse.add(ticketMap);
        }
        
        return ResponseEntity.ok(ticketsResponse);
    }

    /**
     * Get all tickets for a lottery
     */
    @GetMapping("/lottery/{lotteryId}")
    public ResponseEntity<List<LotteryTicket>> getLotteryTickets(@PathVariable Long lotteryId) {
        List<LotteryTicket> tickets = lotteryTicketService.getLotteryTickets(lotteryId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get winning tickets for a lottery
     */
    @GetMapping("/lottery/{lotteryId}/winners")
    public ResponseEntity<List<LotteryTicket>> getWinningTickets(@PathVariable Long lotteryId) {
        List<LotteryTicket> tickets = lotteryTicketService.getWinningTickets(lotteryId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get all winning tickets
     */
    @GetMapping("/winners")
    public ResponseEntity<List<LotteryTicket>> getAllWinningTickets() {
        List<LotteryTicket> tickets = lotteryTicketService.getAllWinningTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get player statistics
     */
    @GetMapping("/player/{walletAddress}/statistics")
    public ResponseEntity<LotteryTicketService.PlayerStats> getPlayerStatistics(@PathVariable String walletAddress) {
        LotteryTicketService.PlayerStats stats = lotteryTicketService.getPlayerStatistics(walletAddress);
        return ResponseEntity.ok(stats);
    }

    /**
     * Purchase a ticket
     */
    @PostMapping("/purchase")
    @Operation(
        summary = "🛒 Comprar boleto",
        description = "Registra la compra de un boleto en la base de datos después de la transacción en blockchain",
        tags = {"Tickets"}
    )
    public ResponseEntity<Map<String, Object>> purchaseTicket(@RequestBody PurchaseTicketRequest request) {
        logger.info("Solicitud de compra de boleto recibida - Wallet: {}, Lottery ID: {}", 
            request.getWalletAddress(), request.getLotteryId());
        
        try {
            // Validar campos requeridos
            if (request.getWalletAddress() == null || request.getWalletAddress().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "walletAddress es requerido");
                logger.warn("Compra rechazada: walletAddress faltante");
                return ResponseEntity.badRequest().body(error);
            }

            if (request.getLotteryId() == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "lotteryId es requerido");
                logger.warn("Compra rechazada: lotteryId faltante");
                return ResponseEntity.badRequest().body(error);
            }

            if (request.getChosenNumbers() == null || request.getChosenNumbers().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "chosenNumbers es requerido");
                logger.warn("Compra rechazada: chosenNumbers faltante");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate chosen numbers
            if (!lotteryTicketService.validateChosenNumbers(request.getChosenNumbers())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Los números elegidos no son válidos");
                logger.warn("Compra rechazada: números inválidos - {}", request.getChosenNumbers());
                return ResponseEntity.badRequest().body(error);
            }

            logger.info("Datos validados correctamente, procesando compra...");
            
            LotteryTicket ticket = lotteryTicketService.purchaseTicket(
                request.getWalletAddress(),
                request.getLotteryId(),
                request.getChosenNumbers(),
                request.getTicketPrice(),
                request.getTransactionHash(),
                request.getContractTicketId()
            );

            logger.info("Boleto creado exitosamente en BD con ID: {}", ticket.getId());
            
            // Retornar respuesta en formato JSON controlado
            Map<String, Object> response = new HashMap<>();
            response.put("id", ticket.getId());
            response.put("ticketId", ticket.getTicketId());
            response.put("walletAddress", ticket.getWalletAddress());
            response.put("lotteryId", ticket.getWeeklyLottery() != null ? ticket.getWeeklyLottery().getId() : request.getLotteryId());
            response.put("chosenNumbers", ticket.getChosenNumbers());
            response.put("ticketPricePaid", ticket.getTicketPricePaid());
            response.put("transactionHash", ticket.getTransactionHash());
            response.put("purchasedAt", ticket.getPurchasedAt());
            response.put("message", "Boleto registrado exitosamente en la base de datos");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al comprar boleto: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error inesperado al comprar boleto", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update ticket matched numbers (for draw processing)
     */
    @PutMapping("/{id}/matches")
    public ResponseEntity<LotteryTicket> updateMatchedNumbers(
            @PathVariable Long id,
            @RequestBody UpdateMatchesRequest request) {
        try {
            LotteryTicket ticket = lotteryTicketService.updateMatchedNumbers(id, request.getMatchedNumbers());
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark ticket as winner
     */
    @PutMapping("/{id}/winner")
    public ResponseEntity<LotteryTicket> markAsWinner(
            @PathVariable Long id,
            @RequestBody MarkWinnerRequest request) {
        try {
            LotteryTicket ticket = lotteryTicketService.markAsWinner(id, request.getPrizeAmount());
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Validate chosen numbers format
     */
    @PostMapping("/validate-numbers")
    public ResponseEntity<ValidateNumbersResponse> validateNumbers(@RequestBody ValidateNumbersRequest request) {
        boolean isValid = lotteryTicketService.validateChosenNumbers(request.getChosenNumbers());
        return ResponseEntity.ok(new ValidateNumbersResponse(isValid));
    }

    // Request DTOs
    public static class PurchaseTicketRequest {
        private String walletAddress;
        private Long lotteryId;
        private String chosenNumbers;
        private BigDecimal ticketPrice;
        private String transactionHash;
        private String contractTicketId;

        // Getters and setters
        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
        
        public Long getLotteryId() { return lotteryId; }
        public void setLotteryId(Long lotteryId) { this.lotteryId = lotteryId; }
        
        public String getChosenNumbers() { return chosenNumbers; }
        public void setChosenNumbers(String chosenNumbers) { this.chosenNumbers = chosenNumbers; }
        
        public BigDecimal getTicketPrice() { return ticketPrice; }
        public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
        
        public String getTransactionHash() { return transactionHash; }
        public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
        
        public String getContractTicketId() { return contractTicketId; }
        public void setContractTicketId(String contractTicketId) { this.contractTicketId = contractTicketId; }
    }

    public static class UpdateMatchesRequest {
        private Integer matchedNumbers;

        public Integer getMatchedNumbers() { return matchedNumbers; }
        public void setMatchedNumbers(Integer matchedNumbers) { this.matchedNumbers = matchedNumbers; }
    }

    public static class MarkWinnerRequest {
        private BigDecimal prizeAmount;

        public BigDecimal getPrizeAmount() { return prizeAmount; }
        public void setPrizeAmount(BigDecimal prizeAmount) { this.prizeAmount = prizeAmount; }
    }

    public static class ValidateNumbersRequest {
        private String chosenNumbers;

        public String getChosenNumbers() { return chosenNumbers; }
        public void setChosenNumbers(String chosenNumbers) { this.chosenNumbers = chosenNumbers; }
    }

    public static class ValidateNumbersResponse {
        private boolean valid;

        public ValidateNumbersResponse(boolean valid) {
            this.valid = valid;
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
    }
}
