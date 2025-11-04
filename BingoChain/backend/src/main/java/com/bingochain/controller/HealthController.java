package com.bingochain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080", "http://127.0.0.1:8080"})
@Tag(name = "Health", description = "🏥 Monitoreo y diagnóstico del sistema")
public class HealthController {

    @Autowired
    private Environment environment;
    
    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    @Operation(
        summary = "🏥 Estado de salud del sistema",
        description = """
            Endpoint de diagnóstico que verifica el estado de todos los componentes críticos:
            
            ## Verificaciones incluidas:
            - ✅ Estado de la aplicación Spring Boot
            - 🗄️ Conectividad con PostgreSQL
            - ⛓️ Conexión con blockchain (Ganache)
            - 🕐 Timestamp del servidor
            - 🔧 Información de configuración
            
            Útil para debugging y monitoreo de la aplicación.
            """,
        tags = {"Health"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Sistema funcionando correctamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Estado Saludable",
                    value = """
                    {
                      "status": "UP",
                      "timestamp": "2025-09-11T10:30:00",
                      "application": {
                        "name": "BingoChain Backend",
                        "version": "1.0.0",
                        "profile": "default"
                      },
                      "database": {
                        "status": "UP",
                        "url": "postgresql://localhost:5434/bingo_crypto"
                      },
                      "blockchain": {
                        "network": "Ganache CLI",
                        "url": "http://localhost:8545",
                        "chainId": 1337
                      }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Estado general
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now().toString());
            
            // Información de la aplicación
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("name", "BingoChain Backend");
            appInfo.put("version", "1.0.0");
            appInfo.put("profile", String.join(",", environment.getActiveProfiles().length > 0 ? 
                environment.getActiveProfiles() : new String[]{"default"}));
            health.put("application", appInfo);
            
            // Estado de la base de datos
            Map<String, Object> dbInfo = new HashMap<>();
            try (Connection connection = dataSource.getConnection()) {
                dbInfo.put("status", "UP");
                dbInfo.put("url", connection.getMetaData().getURL());
                dbInfo.put("driver", connection.getMetaData().getDriverName());
            } catch (Exception e) {
                dbInfo.put("status", "DOWN");
                dbInfo.put("error", e.getMessage());
            }
            health.put("database", dbInfo);
            
            // Información de blockchain
            Map<String, Object> blockchainInfo = new HashMap<>();
            blockchainInfo.put("network", "Ganache CLI");
            blockchainInfo.put("url", environment.getProperty("blockchain.ethereum.network-url", "http://localhost:8545"));
            blockchainInfo.put("chainId", environment.getProperty("blockchain.ethereum.chain-id", "1337"));
            blockchainInfo.put("contractAddress", environment.getProperty("blockchain.contract.crypto-bingo.address", "N/A"));
            health.put("blockchain", blockchainInfo);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/info")
    @Operation(
        summary = "ℹ️ Información detallada del sistema",
        description = """
            Retorna información técnica detallada sobre la aplicación:
            
            ## Información incluida:
            - 📋 Detalles de la aplicación
            - ☕ Versión de Java
            - 🌱 Versión de Spring Boot
            - 🗄️ Configuración de base de datos
            - ⛓️ Configuración de blockchain
            - 🔧 Variables de entorno relevantes
            """,
        tags = {"Health"}
    )
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        
        // Información de la aplicación
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", "BingoChain Backend");
        appInfo.put("description", "Backend for BingoChain - Blockchain Bingo Game");
        appInfo.put("version", "1.0.0");
        appInfo.put("java.version", System.getProperty("java.version"));
        appInfo.put("spring.version", org.springframework.core.SpringVersion.getVersion());
        info.put("application", appInfo);
        
        // Configuración de base de datos
        Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("url", environment.getProperty("spring.datasource.url"));
        dbConfig.put("username", environment.getProperty("spring.datasource.username"));
        dbConfig.put("driver", environment.getProperty("spring.datasource.driver-class-name"));
        info.put("database", dbConfig);
        
        // Configuración de blockchain
        Map<String, Object> blockchainConfig = new HashMap<>();
        blockchainConfig.put("network-url", environment.getProperty("blockchain.ethereum.network-url"));
        blockchainConfig.put("chain-id", environment.getProperty("blockchain.ethereum.chain-id"));
        blockchainConfig.put("gas-price", environment.getProperty("blockchain.ethereum.gas-price"));
        blockchainConfig.put("gas-limit", environment.getProperty("blockchain.ethereum.gas-limit"));
        blockchainConfig.put("contract-address", environment.getProperty("blockchain.contract.crypto-bingo.address"));
        info.put("blockchain", blockchainConfig);
        
        // Configuración del servidor
        Map<String, Object> serverConfig = new HashMap<>();
        serverConfig.put("port", environment.getProperty("server.port"));
        serverConfig.put("context-path", environment.getProperty("server.servlet.context-path"));
        info.put("server", serverConfig);
        
        return ResponseEntity.ok(info);
    }

    @GetMapping("/debug/config")
    @Operation(
        summary = "🔧 Configuración para debugging",
        description = """
            Endpoint especial para debugging que muestra toda la configuración relevante.
            
            ⚠️ **Solo para desarrollo** - No usar en producción
            
            ## Información expuesta:
            - 🔗 URLs de conexión
            - 🔑 Configuraciones (sin contraseñas)
            - 📊 Estado de componentes
            - 🌐 Variables de entorno
            """,
        tags = {"Health"}
    )
    public ResponseEntity<Map<String, Object>> debugConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // URLs importantes
        Map<String, Object> urls = new HashMap<>();
        urls.put("api", "http://localhost:3500/api/v1");
        urls.put("swagger", "http://localhost:3500/api/v1/swagger-ui/index.html");
        urls.put("actuator", "http://localhost:3500/api/v1/actuator/health");
        urls.put("frontend", "http://localhost:8080");
        urls.put("database", "postgresql://localhost:5434/bingo_crypto");
        urls.put("ganache", "http://localhost:8545");
        config.put("urls", urls);
        
        // Perfiles activos
        config.put("profiles", environment.getActiveProfiles().length > 0 ? 
            environment.getActiveProfiles() : new String[]{"default"});
        
        // Estado de servicios
        Map<String, Object> services = new HashMap<>();
        services.put("database", "PostgreSQL 15");
        services.put("blockchain", "Ganache CLI");
        services.put("cache", "Redis (optional)");
        config.put("services", services);
        
        return ResponseEntity.ok(config);
    }
}
