package com.example.project;

import com.example.project.seeder.DatabaseSeeder;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import com.example.project.migration.MigrationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

	 @Bean
	 ApplicationRunner init(MigrationRunner migrationRunner, DatabaseSeeder databaseSeeder) {
		 return args -> {
			 migrationRunner.run();
			 
			 databaseSeeder.seedDatabase(); 
		 };
	 }
}
