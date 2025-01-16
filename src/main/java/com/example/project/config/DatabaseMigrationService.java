package com.example.project.config;

import java.util.Map;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.sql.ResultSet;
import java.nio.file.Path;
import java.sql.Connection;
import java.io.IOException;
import java.nio.file.Files;
import javax.sql.DataSource;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.sql.DatabaseMetaData;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import jakarta.persistence.EntityManagerFactory;

/*

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseMigrationService 
{    
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;
    private final Environment  env;
    private final Flyway flyway;
    
    private static final String SCHEMA_FILE_PATH = "target/generated-schema.sql";
    
    @PostConstruct
    public void init() {
    
        boolean shouldCheckSchema = Boolean.parseBoolean(
            env.getProperty("app.check-schema", "false")
        );
        
        if (!shouldCheckSchema) {
            log.info("Schema check is disabled. Skipping schema comparison.");
            return;
        }

        try {
        	// Create target directory if it doesn't exist
            Path schemaPath = Path.of(SCHEMA_FILE_PATH);
            Files.createDirectories(schemaPath.getParent());
            
            // Step 1: Generate schema from entities
            generateSchemaFromEntities();
            
            // Step 2: Compare and generate migration if needed
            if (isSchemaUpdateNeeded()) {
                generateMigrationScript();
                
                // Step 3: Set the flag to run migrations if new script was generated
                System.setProperty("app.run-migrations", "true");
                log.info("New migration script generated. Setting app.run-migrations=true");
            } else {
                log.info("No schema changes detected.");
            }
            
        } catch (Exception e) {
            log.error("Schema comparison failed", e);
            throw new RuntimeException("Failed to check database schema", e);
        }
       
    }
    
    private void generateSchemaFromEntities() {
        try {
            // Create schema file if it doesn't exist
            Path schemaFile = Path.of(SCHEMA_FILE_PATH);
            Files.createDirectories(schemaFile.getParent());

            // Create MetadataSources and configure
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, entityManagerFactory.getProperties().get("hibernate.dialect"))
                .build();
                
            MetadataSources metadataSources = new MetadataSources(standardRegistry);
            
            // Add all entity classes from your packages
            // This assumes your entities are annotated with @Entity
            String[] packagesToScan = {
                "com.example.project.domain",
                "com.example.project.entity"
            };
            
            for (String packageName : packagesToScan) {
                ClassPathScanningCandidateComponentProvider scanner = 
                    new ClassPathScanningCandidateComponentProvider(false);
                scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
                
                for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
                    Class<?> entityClass = Class.forName(bd.getBeanClassName());
                    metadataSources.addAnnotatedClass(entityClass);
                }
            }

            // Generate schema
            Metadata metadata = metadataSources.getMetadataBuilder().build();
            SchemaExport schemaExport = new SchemaExport();
            schemaExport.setFormat(true);
            schemaExport.setDelimiter(";");
            schemaExport.setOutputFile(SCHEMA_FILE_PATH);
            
            EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.SCRIPT);
            schemaExport.execute(targetTypes, SchemaExport.Action.CREATE, metadata);

            log.info("Schema generated successfully at: {}", SCHEMA_FILE_PATH);
            
            // Cleanup
            standardRegistry.close();
            
        } catch (Exception e) {
            log.error("Failed to generate schema", e);
            throw new RuntimeException("Failed to generate schema", e);
        }
    }
    
    private boolean isSchemaUpdateNeeded() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            Path schemaFile = Path.of(SCHEMA_FILE_PATH);
            if (!Files.exists(schemaFile)) {
                log.error("Generated schema file not found at: {}", SCHEMA_FILE_PATH);
                return false;
            }
            
            String generatedSchema = Files.readString(schemaFile);
            String currentSchema = extractCurrentSchema(metaData);
            
            return !compareSchemas(currentSchema, generatedSchema);
        } catch (Exception e) {
            log.error("Failed to compare schemas", e);
            return false;
        }
    }
    
    private void generateMigrationScript() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date());
            
            String generatedSchema = Files.readString(Path.of("target/generated-schema.sql"));
            
            // Use Flyway's configured locations
            String migrationLocation = env.getProperty("spring.flyway.locations", "classpath:db/migration")
                .replace("classpath:", "src/main/resources/");
            
            Path migrationPath = Path.of(migrationLocation);
            Files.createDirectories(migrationPath);
            
            String filename = String.format("V%s__Auto_generated_schema_update.sql", timestamp);
            Path migrationFile = migrationPath.resolve(filename);
            
            Files.writeString(migrationFile, generatedSchema);
            
            log.info("Generated migration script: {}", filename);
        } catch (IOException e) {
            log.error("Failed to generate migration script", e);
            throw new RuntimeException("Migration script generation failed", e);
        }
    }
    
    private String extractCurrentSchema(DatabaseMetaData metaData) throws SQLException {
        StringBuilder schema = new StringBuilder();
        
        try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                schema.append(generateTableSQL(metaData, tableName));
            }
        }
        
        return schema.toString();
    }
    
    private String generateTableSQL(DatabaseMetaData metaData, String tableName) 
            throws SQLException {
        StringBuilder tableSQL = new StringBuilder();
        tableSQL.append("CREATE TABLE ").append(tableName).append(" (\n");
        
        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            boolean first = true;
            while (columns.next()) {
                if (!first) {
                    tableSQL.append(",\n");
                }
                first = false;
                
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                boolean nullable = columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                
                tableSQL.append("    ")
                       .append(columnName)
                       .append(" ")
                       .append(columnType)
                       .append("(").append(columnSize).append(")")
                       .append(nullable ? "" : " NOT NULL");
            }
        }
        
        tableSQL.append("\n);\n\n");
        return tableSQL.toString();
    }
    
    private boolean compareSchemas(String currentSchema, String generatedSchema) {
        String normalizedCurrent = normalizeSchema(currentSchema);
        String normalizedGenerated = normalizeSchema(generatedSchema);
        return normalizedCurrent.equals(normalizedGenerated);
    }
    
    private String normalizeSchema(String schema) {
        return schema.replaceAll("\\s+", " ")
                    .replaceAll("\\s*,\\s*", ",")
                    .replaceAll("\\s*\\(\\s*", "(")
                    .replaceAll("\\s*\\)\\s*", ")")
                    .toLowerCase()
                    .trim();
    }
}


 */

