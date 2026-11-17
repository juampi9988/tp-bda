package ar.edu.utn.frc.tup.bda.premios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "ar.edu.utn.frc.tup.bda.premios",
        "ar.edu.utn.frc.tup.bda.commons"
})
public class MsPremiosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsPremiosApplication.class, args);
    }
}

