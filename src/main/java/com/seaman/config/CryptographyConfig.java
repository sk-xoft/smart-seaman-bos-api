package com.seaman.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class CryptographyConfig {

    @Value("${encrypt.cert.key}")
    private String encryptCertSecretKey;

    @Value("${jwt.secret}")
    private String encryptJwtSecretKey;

    @Bean
    public SecretKey certSecretKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(encryptCertSecretKey), "AES");
    }

    @Bean(name = "jwtSecretKey")
    public SecretKey jwtSecretKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(encryptJwtSecretKey), "AES");
    }

}
