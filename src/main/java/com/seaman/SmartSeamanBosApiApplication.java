package com.seaman;

import com.seaman.constant.AppSys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.TimeZone;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title =  AppSys.APPLICATION_NAME , version = AppSys.APPLICATION_VERSION, description = AppSys.APPLICATION_DESC))
// @SecurityScheme(name = AppSys.APPLICATION_NAME, scheme = "basic", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class SmartSeamanBosApiApplication {

	private final Logger logger = LoggerFactory.getLogger(SmartSeamanBosApiApplication.class);

	@Value("${spring.profiles.active}")
	private String activeProfile;

	public static void main(String[] args) {

//		this test logs.
//		logger.trace("A TRACE Message");
//		logger.debug("A DEBUG Message");
//		logger.info("An INFO Message");
//		logger.warn("A WARN Message");
//		logger.error("An ERROR Message");

		SpringApplication.run(SmartSeamanBosApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			StringBuilder sb = new StringBuilder();
			sb
					.append(System.lineSeparator())
					.append("|------------Application Details ------------")
					.append(System.lineSeparator())
					.append("|--- Application name : ").append(AppSys.APPLICATION_NAME)
					.append(System.lineSeparator())
					.append("|--- Environment : ").append(activeProfile)
					.append(System.lineSeparator())
					.append("|--- Default Timezone : ").append(TimeZone.getDefault().getID())
					.append(" at Date : ").append(new Date())
					.append(System.lineSeparator())
					.append("|--- Application Charset : ").append(Charset.defaultCharset().displayName())
					.append(System.lineSeparator())
					.append("|---------------------------------");

			logger.info("{}", sb);
		};
	}
}
