package com.bingochain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI para BingoChain
 * 
 * Proporciona documentación interactiva de la API REST
 * Accesible en: http://localhost:3500/api/v1/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI bingoChainOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🎰 BingoChain API")
                        .description("""
                                API REST para BingoChain - Lotería Descentralizada con NFTs
                                
                                ## Funcionalidades Principales:
                                - 🎲 Gestión de sorteos semanales
                                - 🎫 Compra y gestión de boletos NFT
                                - 🏆 Sistema de premios y ganadores
                                - ⛓️ Integración con blockchain Ethereum
                                - 📊 Estadísticas y reportes
                                
                                ## Tecnologías:
                                - Spring Boot 3.2.0
                                - PostgreSQL 15
                                - Web3j para Ethereum
                                - Ganache para desarrollo local
                                
                                ## Red Blockchain:
                                - **Desarrollo**: Ganache CLI (localhost:8545)
                                - **Chain ID**: 1337
                                - **Contrato**: CryptoBingo.sol
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BingoChain Development Team")
                                .email("dev@bingochain.com")
                                .url("https://github.com/bingochain"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:3500/api/v1")
                                .description("Servidor de Desarrollo Local"),
                        new Server()
                                .url("https://api.bingochain.com/v1")
                                .description("Servidor de Producción (Futuro)")))
                .tags(List.of(
                        new Tag()
                                .name("Lotteries")
                                .description("🎲 Gestión de sorteos y loterías"),
                        new Tag()
                                .name("Tickets")
                                .description("🎫 Compra y gestión de boletos NFT"),
                        new Tag()
                                .name("Blockchain")
                                .description("⛓️ Operaciones de blockchain y contratos"),
                        new Tag()
                                .name("Health")
                                .description("🏥 Monitoreo y salud del sistema")));
    }
}


