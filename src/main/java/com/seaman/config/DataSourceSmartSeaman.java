package com.seaman.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import javax.sql.DataSource;

@Configuration
public class DataSourceSmartSeaman {

    @Value("${smart.seaman.datasource.url}")
    private String url;

    @Value("${smart.seaman.datasource.driver}")
    private String driver;

    @Value("${smart.seaman.datasource.username}")
    private String dbUsr;

    @Value("${smart.seaman.datasource.password}")
    private String dbPwd;

    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource() {

        DataSource dataSource =  DataSourceBuilder
                .create()
                .username(dbUsr)
                .password(dbPwd)
                .url(url)
                .driverClassName(driver)
                .build();

        return dataSource;
    }

    @Bean("template")
    public NamedParameterJdbcTemplate template () {
        return new NamedParameterJdbcTemplate(this.dataSource());
    }
}
