package ar.edu.utn.frc.tup.bda.turnos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "ar.edu.utn.frc.tup.bda.turnos",
        "ar.edu.utn.frc.tup.bda.commons"
})
public class MsTurnosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsTurnosApplication.class, args);
    }
}

