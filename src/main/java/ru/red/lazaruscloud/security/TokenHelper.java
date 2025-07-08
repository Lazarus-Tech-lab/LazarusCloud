package ru.red.lazaruscloud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.red.lazaruscloud.dto.authDtos.JwtResponseDto;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class TokenHelper {

    @Value("${application.jwt.secret}")
    private String SECRET_KEY;

    @Value("${application.jwt.access.expire}")
    private long EXPIRE_ACCESS;

    @Value("${application.jwt.refresh.expire}")
    private long EXPIRE_REFRESH;

    public JwtResponseDto generateTokens(UserDetails userDetails) {
        String access = generateAccessToken(userDetails);
        String refresh = generateRefreshToken(userDetails);
        return new JwtResponseDto(access, refresh);
    }

    public boolean validateTokenOfType(String token, UserDetails userDetails) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);

            Claims claims = claimsJws.getPayload();
            return claims.getSubject().equals(userDetails.getUsername()) &&
                    claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token).getPayload();
    }

    private String generateAccessToken(UserDetails userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "access")
                .claim("roles", userDetails.getAuthorities())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRE_ACCESS))
                .signWith(getSecretKey())
                .compact();
    }

    private String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(now.getTime() + EXPIRE_REFRESH))
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        byte[] encodedKey = Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(encodedKey);
    }
}