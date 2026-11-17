package ar.edu.utn.frc.tup.bda.turnos.client;

import ar.edu.utn.frc.tup.bda.commons.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Cliente HTTP hacia ms-premios con manejo controlado de errores de negocio (422/404)
 * y fallas de disponibilidad/timeouts (503 Service Unavailable).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PremiosClient {

    private final RestClient premiosRestClient;


    // ==== Cuando el ms-turnos quiere solicitar un CANJE le hace un POST al endpoint de ms-premios ====

    public CanjePuntosResponseDTO solicitarCanjePremio(CanjePuntosRequestDTO request, String bearerToken) {
        try {
            return premiosRestClient.post()
                    .uri("/api/premios/canjes")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(CanjePuntosResponseDTO.class); // esto le manda en el cuerpo de la solicitud
        } catch (HttpClientErrorException ex) {
            log.warn("Rechazo de negocio desde ms-premios al canjear premio ({}): {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            ErrorResponseDTO errorRemoto = ex.getResponseBodyAs(ErrorResponseDTO.class);
            String mensaje = (errorRemoto != null && errorRemoto.getMessage() != null)
                    ? errorRemoto.getMessage()
                    : "No fue posible canjear el premio solicitado en ms-premios";
            throw new ResponseStatusException(ex.getStatusCode(), mensaje);
        } catch (RestClientException ex) {
            log.error("Error de comunicación o timeout hacia ms-premios al solicitar canje: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de fidelización (ms-premios) no se encuentra disponible para procesar el canje. " +
                    "Se abortó la reserva para resguardar la consistencia.");
        }
    }

    // ==== Cuando el ms-turnos quiere solicitar una ACREDITACION le hace un POST al endpoint de ms-premios ====

    public AcreditacionPuntosResponseDTO solicitarAcreditacionPuntos(AcreditacionPuntosRequestDTO request, String bearerToken) {
        try {
            return premiosRestClient.post()
                    .uri("/api/premios/acreditaciones")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AcreditacionPuntosResponseDTO.class);  
        } catch (HttpClientErrorException ex) {  // ms-premios respondió pero con un error de negocio (ej: 422 "puntos insuficientes"). Se reenvía el mismo código y mensaje al cliente.
            ErrorResponseDTO errorRemoto = ex.getResponseBodyAs(ErrorResponseDTO.class);
            String mensaje = (errorRemoto != null && errorRemoto.getMessage() != null)
                    ? errorRemoto.getMessage()
                    : "Error al acreditar puntos en ms-premios";
            throw new ResponseStatusException(ex.getStatusCode(), mensaje);
        } catch (RestClientException ex) { // ms-premios no respondió (caído o tardó demasiado). Se responde 503 Service Unavailable y se aborta la operación.
            log.error("Error de comunicación o timeout hacia ms-premios al acreditar puntos: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de fidelización (ms-premios) no responde. " +
                    "Se abortó el cierre del turno para mantener la consistencia entre servicios.");
        }
    }

    // ==== Cuando el ms-turnos quiere solicitar un REINTEGRO le hace un POST al endpoint de ms-premios ====

    public ReintegroPuntosResponseDTO solicitarReintegroPuntos(ReintegroPuntosRequestDTO request, String bearerToken) {
        try {
            return premiosRestClient.post()
                    .uri("/api/premios/reintegros")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ReintegroPuntosResponseDTO.class);
        } catch (HttpClientErrorException ex) {
            ErrorResponseDTO errorRemoto = ex.getResponseBodyAs(ErrorResponseDTO.class);
            String mensaje = (errorRemoto != null && errorRemoto.getMessage() != null)
                    ? errorRemoto.getMessage()
                    : "Error al reintegrar puntos en ms-premios";
            throw new ResponseStatusException(ex.getStatusCode(), mensaje);
        } catch (RestClientException ex) {
            log.error("Error de comunicación o timeout hacia ms-premios al reintegrar puntos: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de fidelización (ms-premios) no responde para restituir los puntos. " +
                    "Se abortó la cancelación para evitar pérdida de saldo del cliente.");
        }
    }
}

