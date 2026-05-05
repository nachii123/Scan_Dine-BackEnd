package com.example.scan_dineCustomer.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private static final long JWT_TOKEN_VALIDITY = 90L * 24 * 60 * 60 * 1000;

    public String generateToken(String customerId, String mobile, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customer_id", customerId);
        claims.put("mobile", mobile);
        claims.put("role", "CUSTOMER");
        return createToken(claims, email);
    }

    public String generateCaptainToken(String captainId, String mobile, String email, String restaurantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("captain_id", captainId);
        claims.put("mobile", mobile);
        claims.put("restaurant_id", restaurantId);
        claims.put("role", "CAPTAIN");
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractCustomerId(String token) {
        return extractClaim(token, claims -> claims.get("customer_id", String.class));
    }

    public String extractCaptainId(String token) {
        return extractClaim(token, claims -> claims.get("captain_id", String.class));
    }

    public String extractRestaurantId(String token) {
        return extractClaim(token, claims -> claims.get("restaurant_id", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
