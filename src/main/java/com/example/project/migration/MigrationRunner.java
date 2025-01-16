package com.example.project.migration;

import java.util.Arrays;
import org.flywaydb.core.Flyway;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
public class MigrationRunner 
{
    @Value("${app.run-migrations}")
    private String appRunMigrations;
    
    @Autowired
    private Flyway flyway;
    
    public void run()
	{
    	run(false);
	}
    
	public void run(boolean override)
	{	  
        boolean shouldRunMigrations = Boolean.parseBoolean(appRunMigrations) || override;
        if(!shouldRunMigrations) return;
        
        try {
        	var migrations = flyway.info().all();
            log.info("Starting Flyway migrations, Available migrations {Total}: {MigrationsArray} ", 
            		migrations.length,
                    Arrays.toString(migrations));
            flyway.migrate();
            flyway.validate();
            log.info("Flyway migrations completed successfully.");
        } catch (Exception e) {
            log.error("Flyway migrations failed: {Exception}", e.getMessage(), e);
            throw e;
        }
	}

}
