package com.costuras.agenda.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public Claims getAllClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public <T> T getClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(getAllClaims(token));
    }

    public boolean isTokenExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }

    public UsuarioPrincipal getPrincipalFromToken(String token) {
        Claims claims = getAllClaims(token);
        Integer id = null;
        Object idClaim = claims.get("id");
        if (idClaim instanceof Integer) id = (Integer) idClaim;
        else if (idClaim instanceof Long) id = ((Long) idClaim).intValue();
        return UsuarioPrincipal.builder()
                .id(id)
                .username(claims.getSubject())
                .role(claims.get("role", String.class))
                .build();
    }
}
