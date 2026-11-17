package ar.edu.utn.frc.tup.bda.commons.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio transversal de envío de correos electrónicos (RN-06).
 * Un fallo en el servidor SMTP se captura y registra en el log sin abortar la transacción principal.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@peluqueria.utn.edu.ar}")
    private String remitente;

    public void enviarNotificacionNoBloqueante(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Correo enviado exitosamente a [{}] - Asunto: {}", destinatario, asunto);
        } catch (Exception ex) {
            // RN-06: No abortar la transacción de negocio si el servidor SMTP falla
            log.warn("RN-06: Fallo al enviar correo electrónico a [{}] (Asunto: '{}'). " +
                     "La transacción principal continúa normalmente. Detalle: {}",
                     destinatario, asunto, ex.getMessage());
        }
    }
}

