package ex.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import ex.model.Perfil;
import ex.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    private static final String ISSUER = "auth-api";

    public String generateToken(Usuario usuario){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var jwtBuilder = JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getEmail())
                    .withClaim("perfil", usuario.getPerfil().name())
                    .withExpiresAt(genExpirationDate(2));

            if (usuario.getPerfil() != Perfil.SUPER_ADMIN && usuario.getCongregacao() != null) {
                jwtBuilder.withClaim("idCongregacao", usuario.getCongregacao().getIdCongregacao());
            }

            return jwtBuilder.sign(algorithm);
        } catch(JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token", exception);
        }
    }

    public String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch(JWTVerificationException exception){
            return "";
        }
    }

    private Instant genExpirationDate(int hours){
        return LocalDateTime.now().plusHours(hours).toInstant(ZoneOffset.of("-03:00"));
    }

}
