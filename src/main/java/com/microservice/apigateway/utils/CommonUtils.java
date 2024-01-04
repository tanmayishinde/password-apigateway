package com.microservice.apigateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Component
public class CommonUtils {
    public static Jws<Claims> getJwtClaims(String jwtString, String secret){
        try{
            // Decode the secret from Base64 and create a SecretKeySpec
            Key hmacKey = new SecretKeySpec(Base64.getDecoder().decode(secret),
                    SignatureAlgorithm.HS256.getJcaName());
            // Decode the secret from Base64 and create a SecretKeySpec

            Jws<Claims> jwt = Jwts.parserBuilder()
                    .setSigningKey(hmacKey)
                    .build()
                    .parseClaimsJws(jwtString);
            return jwt;
        }
        catch (Exception e){
            return null;
        }
    }
    public String createJWTToken(String secret, int timeout, Jws<Claims> claims){
        Key hmacKey  = new SecretKeySpec(Base64.getDecoder().decode(secret),
                SignatureAlgorithm.HS256.getJcaName());

        Instant now = Instant.now();
        return Jwts.builder()
                .claim("name", claims.getBody().get("fullname"))
                .claim("email", claims.getBody().get("email"))
                .setSubject("login")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(timeout, ChronoUnit.MINUTES)))
                .signWith(hmacKey)
                .compact();

    }
    public static boolean isValidJwt(Jws<Claims> jwt) {
        Date expiryDate = jwt.getBody().getExpiration();
        return   Date.from(Instant.now()).before(expiryDate)
                && "login".equalsIgnoreCase(jwt.getBody().getSubject());
    }
    public static HttpCookie getJwtCookie(ServerHttpRequest serverHttpRequest) throws Exception {
//        the keys are cookie names (strings) and the values are HttpCookie objects.
        MultiValueMap<String, HttpCookie> header =serverHttpRequest.getCookies();
        List<HttpCookie> cookie= header.get("auth");
        if(cookie != null){
//            find the first cookie in a stream of cookies and return it,
//            or return null if no cookie is found.
            return cookie.stream()
                    .findFirst()
                    .orElse(null);
        }
        throw new Exception("Invalid session Cookie");

    }
    public static HttpCookie getSessionCookie(ServerHttpRequest serverHttpRequest) throws Exception {
//        the keys are cookie names (strings) and the values are HttpCookie objects.
        MultiValueMap<String, HttpCookie> header = serverHttpRequest.getCookies();
        List<HttpCookie> cookie = header.get("session-ID");
        if (cookie != null) {
//            find the first cookie in a stream of cookies and return it,
//            or return null if no cookie is found.
            return cookie.stream()
                    .findFirst()
                    .orElse(null);
        }
        throw new Exception("Invalid session Cookie");

    }

    public static ResponseCookie getJwtCookie(String jwt, int cookieTimeout){
        return ResponseCookie.from("auth",
                jwt).domain("localhost").httpOnly(true).maxAge(cookieTimeout).path("/").build();
    }
    public static ResponseCookie getSessionCookie(String sessionId, int cookieTimeout){
        return ResponseCookie.from("session-id",
                sessionId).domain("localhost").httpOnly(true).maxAge(cookieTimeout).path("/").build();
    }

}
