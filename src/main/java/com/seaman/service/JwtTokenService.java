package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final SecretKey jwtSecretKey;

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getJti(String token) {
        return getClaimFromToken(token, Claims::getId);
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //for retrieving any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
    }

    public boolean verifyToken(String token) {
        boolean verifySuccess = false;

        try {
            Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token);
            verifySuccess = true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
           // throw new BusinessException("WA00014", ex.getMessage());
//            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
            throw new BusinessException("WA00007", "Incorrect client session id.");
        } catch (ExpiredJwtException ex) {
           // throw new BusinessException("WA00014", ex.getMessage());
//            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
            throw new BusinessException("WA00007", "Incorrect client session id.");
        }

        return verifySuccess;
    }

    //check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //generate token for user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return doGenerateToken(claims, userDetails.getUsername());
    }

    public String generateToken(Map<String, Object> claims, String username) {
        return doGenerateToken(claims, username);
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +  (30 * 60 * 60) * 1000)) // this must set up configuration.
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token, String subject) {
        final String username = getUsernameFromToken(token);
        return (username.equals(subject) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }


}
