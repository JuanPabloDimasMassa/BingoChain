package com.bingochain.controller;

import com.bingochain.model.WeeklyLottery;
import com.bingochain.service.WeeklyLotteryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080", "http://127.0.0.1:8080"})
@Tag(name = "Lotteries", description = "🎲 API para gestión de sorteos y loterías")
public class LotteryController {

    @Autowired
    private WeeklyLotteryService weeklyLotteryService;

    @Value("${admin.token}")
    private String adminToken;

    private static final Logger logger = LoggerFactory.getLogger(LotteryController.class);

    @GetMapping("/lotteries")
    @Operation(
        summary = "📋 Obtener todos los sorteos",
        description = """
            Retorna la lista completa de sorteos desde la base de datos.
            
            ## Estados de Sorteo:
            - **TICKET_SALES**: Venta de boletos activa
            - **DRAWING_PHASE**: Sorteo en proceso de extracción
            - **COMPLETED**: Sorteo finalizado
            - **CANCELLED**: Sorteo cancelado
            
            ## Información incluida:
            - Detalles completos del sorteo
            - Precios y premios actuales
            - Estado actual y fechas
            - Números ganadores (si aplica)
            - Estadísticas de boletos vendidos
            """,
        tags = {"Lotteries"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Lista de sorteos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class),
                examples = @ExampleObject(
                    name = "Sorteos Ejemplo",
                    value = """
                    [
                      {
                        "id": 1,
                        "lotteryName": "Demo Weekly Lottery #1",
                        "contractAddress": "0x1234567890123456789012345678901234567890",
                        "ticketPrice": 0.01,
                        "prizePool": 0.0,
                        "totalTickets": 0,
                        "status": "TICKET_SALES",
                        "salesStartTime": "2025-09-11T10:00:00",
                        "salesEndTime": "2025-09-18T10:00:00",
                        "nextDrawTime": "2025-09-18T15:00:00",
                        "drawnNumbers": null,
                        "winnerAddresses": null
                      }
                    ]
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    public ResponseEntity<List<Map<String, Object>>> getLotteries() {
        try {
            List<WeeklyLottery> lotteries = weeklyLotteryService.getAllLotteries();
            List<Map<String, Object>> lotteriesResponse = new ArrayList<>();
            
            for (WeeklyLottery lottery : lotteries) {
                Map<String, Object> lotteryMap = new HashMap<>();
                lotteryMap.put("id", lottery.getId());
                lotteryMap.put("lotteryName", lottery.getLotteryName());
                lotteryMap.put("contractAddress", lottery.getContractAddress());
                lotteryMap.put("ticketPrice", lottery.getTicketPrice());
                lotteryMap.put("prizePool", lottery.getPrizePool());
                lotteryMap.put("totalTickets", lottery.getTotalTickets());
                lotteryMap.put("status", lottery.getStatus().toString());
                lotteryMap.put("salesStartTime", lottery.getSalesStartTime());
                lotteryMap.put("salesEndTime", lottery.getSalesEndTime());
                lotteryMap.put("nextDrawTime", lottery.getNextDrawTime());
                lotteryMap.put("drawnNumbers", lottery.getDrawnNumbers());
                lotteryMap.put("winnerAddresses", lottery.getWinnerAddresses());
                lotteryMap.put("prizesDistributed", lottery.getPrizesDistributed());
                lotteryMap.put("createdAt", lottery.getCreatedAt());
                
                lotteriesResponse.add(lotteryMap);
            }
            
            return ResponseEntity.ok(lotteriesResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lotteries/{id}")
    @Operation(
        summary = "🎲 Obtener sorteo por ID",
        description = "Retorna los detalles completos de un sorteo específico por su ID",
        tags = {"Lotteries"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Sorteo encontrado"),
        @ApiResponse(responseCode = "404", description = "❌ Sorteo no encontrado")
    })
    public ResponseEntity<Map<String, Object>> getLotteryById(
        @Parameter(description = "ID del sorteo", example = "1") 
        @PathVariable Long id) {
        
        Optional<WeeklyLottery> lotteryOpt = weeklyLotteryService.getLotteryById(id);
        
        if (lotteryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WeeklyLottery lottery = lotteryOpt.get();
        Map<String, Object> lotteryMap = new HashMap<>();
        lotteryMap.put("id", lottery.getId());
        lotteryMap.put("lotteryName", lottery.getLotteryName());
        lotteryMap.put("contractAddress", lottery.getContractAddress());
        lotteryMap.put("ticketPrice", lottery.getTicketPrice());
        lotteryMap.put("prizePool", lottery.getPrizePool());
        lotteryMap.put("totalTickets", lottery.getTotalTickets());
        lotteryMap.put("status", lottery.getStatus().toString());
        lotteryMap.put("salesStartTime", lottery.getSalesStartTime());
        lotteryMap.put("salesEndTime", lottery.getSalesEndTime());
        lotteryMap.put("nextDrawTime", lottery.getNextDrawTime());
        lotteryMap.put("drawnNumbers", lottery.getDrawnNumbers());
        lotteryMap.put("winnerAddresses", lottery.getWinnerAddresses());
        lotteryMap.put("prizesDistributed", lottery.getPrizesDistributed());
        lotteryMap.put("createdAt", lottery.getCreatedAt());
        
        return ResponseEntity.ok(lotteryMap);
    }

    @GetMapping("/lotteries/active")
    @Operation(
        summary = "🔴 Obtener sorteos activos",
        description = "Retorna solo los sorteos que tienen venta de boletos activa",
        tags = {"Lotteries"}
    )
    public ResponseEntity<List<Map<String, Object>>> getActiveLotteries() {
        try {
            List<WeeklyLottery> lotteries = weeklyLotteryService.getActiveTicketSales();
            List<Map<String, Object>> lotteriesResponse = new ArrayList<>();
            
            for (WeeklyLottery lottery : lotteries) {
                Map<String, Object> lotteryMap = new HashMap<>();
                lotteryMap.put("id", lottery.getId());
                lotteryMap.put("lotteryName", lottery.getLotteryName());
                lotteryMap.put("contractAddress", lottery.getContractAddress());
                lotteryMap.put("ticketPrice", lottery.getTicketPrice());
                lotteryMap.put("prizePool", lottery.getPrizePool());
                lotteryMap.put("totalTickets", lottery.getTotalTickets());
                lotteryMap.put("status", lottery.getStatus().toString());
                lotteryMap.put("salesStartTime", lottery.getSalesStartTime());
                lotteryMap.put("salesEndTime", lottery.getSalesEndTime());
                lotteryMap.put("nextDrawTime", lottery.getNextDrawTime());
                
                lotteriesResponse.add(lotteryMap);
            }
            
            return ResponseEntity.ok(lotteriesResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lotteries/completed")
    @Operation(
        summary = "✅ Obtener sorteos completados",
        description = "Retorna solo los sorteos que han sido finalizados",
        tags = {"Lotteries"}
    )
    public ResponseEntity<List<Map<String, Object>>> getCompletedLotteries() {
        try {
            List<WeeklyLottery> lotteries = weeklyLotteryService.getCompletedLotteries();
            List<Map<String, Object>> lotteriesResponse = new ArrayList<>();
            
            for (WeeklyLottery lottery : lotteries) {
                Map<String, Object> lotteryMap = new HashMap<>();
                lotteryMap.put("id", lottery.getId());
                lotteryMap.put("lotteryName", lottery.getLotteryName());
                lotteryMap.put("contractAddress", lottery.getContractAddress());
                lotteryMap.put("ticketPrice", lottery.getTicketPrice());
                lotteryMap.put("prizePool", lottery.getPrizePool());
                lotteryMap.put("totalTickets", lottery.getTotalTickets());
                lotteryMap.put("status", lottery.getStatus().toString());
                lotteryMap.put("salesStartTime", lottery.getSalesStartTime());
                lotteryMap.put("salesEndTime", lottery.getSalesEndTime());
                lotteryMap.put("nextDrawTime", lottery.getNextDrawTime());
                lotteryMap.put("drawnNumbers", lottery.getDrawnNumbers());
                lotteryMap.put("winnerAddresses", lottery.getWinnerAddresses());
                lotteryMap.put("prizesDistributed", lottery.getPrizesDistributed());
                
                lotteriesResponse.add(lotteryMap);
            }
            
            return ResponseEntity.ok(lotteriesResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lotteries/current")
    @Operation(
        summary = "📍 Obtener sorteo actual",
        description = "Retorna el sorteo más reciente (actual)",
        tags = {"Lotteries"}
    )
    public ResponseEntity<Map<String, Object>> getCurrentLottery() {
        Optional<WeeklyLottery> lotteryOpt = weeklyLotteryService.getCurrentLottery();
        
        if (lotteryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WeeklyLottery lottery = lotteryOpt.get();
        Map<String, Object> lotteryMap = new HashMap<>();
        lotteryMap.put("id", lottery.getId());
        lotteryMap.put("lotteryName", lottery.getLotteryName());
        lotteryMap.put("contractAddress", lottery.getContractAddress());
        lotteryMap.put("ticketPrice", lottery.getTicketPrice());
        lotteryMap.put("prizePool", lottery.getPrizePool());
        lotteryMap.put("totalTickets", lottery.getTotalTickets());
        lotteryMap.put("status", lottery.getStatus().toString());
        lotteryMap.put("salesStartTime", lottery.getSalesStartTime());
        lotteryMap.put("salesEndTime", lottery.getSalesEndTime());
        lotteryMap.put("nextDrawTime", lottery.getNextDrawTime());
        lotteryMap.put("drawnNumbers", lottery.getDrawnNumbers());
        lotteryMap.put("winnerAddresses", lottery.getWinnerAddresses());
        lotteryMap.put("prizesDistributed", lottery.getPrizesDistributed());
        lotteryMap.put("createdAt", lottery.getCreatedAt());
        
        return ResponseEntity.ok(lotteryMap);
    }

    @PostMapping("/lotteries")
    @Operation(
        summary = "➕ Crear nuevo sorteo (Solo Administradores)",
        description = """
            Crea un nuevo sorteo semanal. Requiere autenticación de administrador mediante header X-Admin-Token.
            
            ## Campos requeridos:
            - **lotteryName**: Nombre del sorteo
            - **contractAddress**: Dirección del contrato inteligente en la blockchain
            - **ticketPrice**: Precio de cada boleto (BigDecimal)
            - **salesStartTime**: Fecha y hora de inicio de venta (formato: yyyy-MM-ddTHH:mm:ss)
            - **salesEndTime**: Fecha y hora de fin de venta (formato: yyyy-MM-ddTHH:mm:ss)
            
            ## Headers requeridos:
            - **X-Admin-Token**: Token de administrador
            """,
        tags = {"Lotteries"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "✅ Sorteo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "❌ Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "❌ No autorizado - Token de admin inválido"),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    public ResponseEntity<Map<String, Object>> createLottery(
        @RequestHeader(value = "X-Admin-Token", required = false) String token,
        @RequestBody Map<String, Object> request) {
        
        logger.info("Intento de crear sorteo recibido");
        
        // Validar token de administrador
        if (token == null || !token.equals(adminToken)) {
            logger.warn("Intento de crear sorteo con token inválido o faltante");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "Token de administrador inválido o faltante");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        logger.info("Token de administrador válido, procesando creación de sorteo");

        try {
            // Validar campos requeridos
            String lotteryName = (String) request.get("lotteryName");
            String contractAddress = (String) request.get("contractAddress");
            Object ticketPriceObj = request.get("ticketPrice");
            String salesStartTimeStr = (String) request.get("salesStartTime");
            String salesEndTimeStr = (String) request.get("salesEndTime");

            if (lotteryName == null || lotteryName.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Validation Error");
                errorResponse.put("message", "lotteryName es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (contractAddress == null || contractAddress.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Validation Error");
                errorResponse.put("message", "contractAddress es requerido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            BigDecimal ticketPrice;
            try {
                if (ticketPriceObj instanceof Number) {
                    ticketPrice = BigDecimal.valueOf(((Number) ticketPriceObj).doubleValue());
                } else if (ticketPriceObj instanceof String) {
                    ticketPrice = new BigDecimal((String) ticketPriceObj);
                } else {
                    throw new IllegalArgumentException("ticketPrice debe ser un número");
                }
                if (ticketPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("ticketPrice debe ser mayor que 0");
                }
            } catch (Exception e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Validation Error");
                errorResponse.put("message", "ticketPrice inválido: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            LocalDateTime salesStartTime;
            LocalDateTime salesEndTime;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                salesStartTime = LocalDateTime.parse(salesStartTimeStr, formatter);
                salesEndTime = LocalDateTime.parse(salesEndTimeStr, formatter);
                
                if (salesEndTime.isBefore(salesStartTime)) {
                    throw new IllegalArgumentException("salesEndTime debe ser posterior a salesStartTime");
                }
            } catch (DateTimeParseException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Validation Error");
                errorResponse.put("message", "Formato de fecha inválido. Use: yyyy-MM-ddTHH:mm:ss");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } catch (Exception e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Validation Error");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Calcular nextDrawTime (23.5 horas después de salesEndTime)
            LocalDateTime nextDrawTime = salesEndTime.plusHours(23).plusMinutes(30);

            // Crear el sorteo
            WeeklyLottery lottery = new WeeklyLottery();
            lottery.setLotteryName(lotteryName.trim());
            lottery.setContractAddress(contractAddress.trim());
            lottery.setTicketPrice(ticketPrice);
            lottery.setSalesStartTime(salesStartTime);
            lottery.setSalesEndTime(salesEndTime);
            lottery.setNextDrawTime(nextDrawTime);
            lottery.setStatus(WeeklyLottery.LotteryStatus.TICKET_SALES);
            lottery.setPrizePool(BigDecimal.ZERO);
            lottery.setTotalTickets(0);
            lottery.setCurrentDrawDay(0);
            lottery.setPrizesDistributed(false);

            WeeklyLottery createdLottery = weeklyLotteryService.createLottery(lottery);
            logger.info("Sorteo creado exitosamente en la base de datos con ID: {}", createdLottery.getId());

            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("id", createdLottery.getId());
            response.put("lotteryName", createdLottery.getLotteryName());
            response.put("contractAddress", createdLottery.getContractAddress());
            response.put("ticketPrice", createdLottery.getTicketPrice());
            response.put("prizePool", createdLottery.getPrizePool());
            response.put("totalTickets", createdLottery.getTotalTickets());
            response.put("status", createdLottery.getStatus().toString());
            response.put("salesStartTime", createdLottery.getSalesStartTime());
            response.put("salesEndTime", createdLottery.getSalesEndTime());
            response.put("nextDrawTime", createdLottery.getNextDrawTime());
            response.put("createdAt", createdLottery.getCreatedAt());
            response.put("message", "Sorteo creado exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error al crear el sorteo", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Error al crear el sorteo: " + e.getMessage());
            e.printStackTrace(); // Para depuración
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

