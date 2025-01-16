package com.example.project.config;

import org.flywaydb.core.Flyway;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
 
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FlyWayConfig 
{
    @Autowired
    private final Environment env;

    @Bean
    public Flyway flyway() {
        String dbUrl = env.getProperty("spring.datasource.url");
        String dbUsername = env.getProperty("spring.datasource.username");
        String dbPassword = env.getProperty("spring.datasource.password");
        
        Boolean baselineOnMigrate = env.getProperty("spring.flyway.baseline-on-migrate", Boolean.class, true);
        String locations = env.getProperty("spring.flyway.locations", "classpath:db/migration");
        
        return Flyway.configure()
                .dataSource(dbUrl, dbUsername, dbPassword)
                .validateOnMigrate(false)
                .baselineOnMigrate(baselineOnMigrate)
                .locations(locations)
                .load();
    }

}
