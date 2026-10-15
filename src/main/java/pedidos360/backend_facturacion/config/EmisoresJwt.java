package pedidos360.backend_facturacion.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// Valida tokens de Entra ID (Microsoft) y de Cognito (AWS)
@Component
public class EmisoresJwt {

    private final Map<String, AuthenticationManager> managers;

    public EmisoresJwt(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String entraIssuer,
            @Value("${spring.security.oauth2.resourceserver.jwt.audiences}") String entraAudience,
            @Value("${app.security.cognito.issuer-uri}") String cognitoIssuer,
            @Value("${app.security.cognito.client-id}") String cognitoClientId) {
        managers = Map.of(
                entraIssuer, manager(entraIssuer, entraAudience, "roles", "preferred_username"),
                cognitoIssuer, manager(cognitoIssuer, cognitoClientId, "cognito:groups", "email"));
    }

    // Elige el validador según el emisor (iss) del token
    public AuthenticationManagerResolver<HttpServletRequest> resolver() {
        return new JwtIssuerAuthenticationManagerResolver(managers::get);
    }

    private static AuthenticationManager manager(String issuer, String audience, String claimRoles, String claimCorreo) {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(audience))));

        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(claimRoles);
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        converter.setPrincipalClaimName(claimCorreo);

        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(converter);
        return provider::authenticate;
    }
}
