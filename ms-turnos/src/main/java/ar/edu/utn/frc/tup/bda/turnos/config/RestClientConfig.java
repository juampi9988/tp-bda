package ar.edu.utn.frc.tup.bda.turnos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuración de clientes HTTP con timeouts explícitos de conexión y lectura.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient premiosRestClient(
            @Value("${integraciones.ms-premios.base-url:http://localhost:8082}") String baseUrl,
            @Value("${integraciones.http.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${integraciones.http.read-timeout-ms:4000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }


    // ==== RN-02 -> Cliente HTTP para conectarse con la API oficial de Google Calendar v3  ====

    @Bean
    public RestClient googleCalendarRestClient(
            @Value("${integraciones.google-calendar.base-url:https://www.googleapis.com/calendar/v3}") String baseUrl,
           
            // Tiempo límite (3 segs) para conectar con el servidor de Google antes de cancelar
            @Value("${integraciones.http.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${integraciones.http.read-timeout-ms:4000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}

