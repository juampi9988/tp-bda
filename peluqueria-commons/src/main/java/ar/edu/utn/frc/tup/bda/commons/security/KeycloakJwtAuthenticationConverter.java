package ar.edu.utn.frc.tup.bda.commons.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convierte los roles dentro del claim "realm_access.roles" de Keycloak
 * en GrantedAuthorities de Spring Security (ej. ROLE_CLIENTE, ROLE_ADMIN).
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    @SuppressWarnings("unchecked")
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Extraemos del token JWT la sección "realm_access", donde Keycloak guarda los roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Collection<GrantedAuthority> authorities = Collections.emptyList();

        // 2. Si existe esa sección y contiene una lista de roles...
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
            authorities = roles.stream()
                    // Filtramos para asegurarnos de que cada elemento sea texto (String)
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    // Si el rol no empieza con "ROLE_", se lo agregamos (ej: "ADMIN" -> "ROLE_ADMIN")
                    // Esto permite que funcionen anotaciones como @PreAuthorize("hasRole('ADMIN')")
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    // Convertimos cada rol al formato estándar que maneja Spring (SimpleGrantedAuthority)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }

        // 3. Devolvemos el token oficial de autenticación para Spring Security con el usuario y sus roles listos
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}

