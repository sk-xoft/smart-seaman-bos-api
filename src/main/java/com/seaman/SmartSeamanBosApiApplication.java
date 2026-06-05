package com.seaman;

import com.seaman.constant.AppSys;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.TimeZone;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = AppSys.APPLICATION_NAME, version = AppSys.APPLICATION_VERSION, description = AppSys.APPLICATION_DESC))
@EnableCaching
public class SmartSeamanBosApiApplication {

	private final Logger logger = LoggerFactory.getLogger(SmartSeamanBosApiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SmartSeamanBosApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(Environment env) {
		return args -> {
			StringBuilder sb = new StringBuilder();
			sb
					.append(System.lineSeparator())
					.append("|------------Application Details ------------")
					.append(System.lineSeparator())
					.append("|--- Application name    : ").append(AppSys.APPLICATION_NAME)
					.append(System.lineSeparator())
					.append("|--- Environment         : ").append(env.getProperty("spring.profiles.active"))
					.append(System.lineSeparator())
					.append("|--- Default Timezone    : ").append(TimeZone.getDefault().getID())
					.append(" at Date : ").append(new Date())
					.append(System.lineSeparator())
					.append("|--- Application Charset : ").append(Charset.defaultCharset().displayName())
					.append(System.lineSeparator())
					.append("|------------Config Details ------------")
					.append(System.lineSeparator())
					.append("|--- DB URL              : ").append(env.getProperty("smart.seaman.datasource.url"))
					.append(System.lineSeparator())
					.append("|--- DB Username         : ").append(env.getProperty("smart.seaman.datasource.username"))
					.append(System.lineSeparator())
					.append("|--- DB Password         : ").append(mask(env.getProperty("smart.seaman.datasource.password", "")))
					.append(System.lineSeparator())
					.append("|--- Object Store URL    : ").append(env.getProperty("object.store.endpoint"))
					.append(System.lineSeparator())
					.append("|--- Object Store Bucket : ").append(env.getProperty("object.store.bucket"))
					.append(System.lineSeparator())
					.append("|--- Object Store Key    : ").append(mask(env.getProperty("object.store.key", "")))
					.append(System.lineSeparator())
					.append("|--- FCM Credential File : ").append(env.getProperty("fcm.firebase.credential.file"))
					.append(System.lineSeparator())
					.append("|--- Mail Host           : ").append(env.getProperty("spring.mail.host"))
					.append(System.lineSeparator())
					.append("|--- Mail Username       : ").append(env.getProperty("spring.mail.username"))
					.append(System.lineSeparator())
					.append("|--- JWT Secret          : ").append(mask(env.getProperty("jwt.secret", "")))
					.append(System.lineSeparator())
					.append("|--- Encrypt Key         : ").append(mask(env.getProperty("encrypt.cert.key", "")))
					.append(System.lineSeparator())
					.append("|---------------------------------");

			logger.info("{}", sb);
		};
	}

	private static String mask(String value) {
		return value.isEmpty() ? "(empty)" : value.substring(0, Math.min(4, value.length())) + "****";
	}
}
