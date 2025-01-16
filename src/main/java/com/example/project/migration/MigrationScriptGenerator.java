package com.example.project.migration;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.util.Objects;
import java.util.HashMap;
import java.util.HashSet;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.nio.file.Files;
import java.io.IOException;
import java.util.LinkedHashSet;
import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.Column;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import java.time.format.DateTimeFormatter;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
public class MigrationScriptGenerator 
{   
    @Value("${app.database-migrations-locations}")
    private String migrationLocation;
    
    @Autowired
    private final JdbcTemplate jdbcTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;

    private static final String FLYWAY_VERSION_QUERY = """
        SELECT version FROM flyway_schema_history 
        ORDER BY installed_rank DESC LIMIT 1
    """;

    public void startMigrationAutomation() {
        log.info("Starting migration script automation...");
        List<String> migrationStatements = compareSchemaWithEntities();
        if (!migrationStatements.isEmpty()) {
            generateMigrationScript(migrationStatements);
        } else {
            log.info("No schema changes detected.");
        }
    }

    private Map<String, TableSchema> fetchCurrentDatabaseSchema() {
        log.info("Fetching current database schema...");
        
        String query = """
            SELECT table_name, column_name, data_type, is_nullable, character_maximum_length
            FROM information_schema.columns
            WHERE table_schema = 'public'
            ORDER BY table_name, ordinal_position;
        """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
        Map<String, TableSchema> schema = new HashMap<>();
        
        for (Map<String, Object> row : rows) {
            String tableName = (String) row.get("table_name");
            String columnName = (String) row.get("column_name");
            String dataType = (String) row.get("data_type");
            String isNullable = (String) row.get("is_nullable");
            Integer maxLength = (Integer) row.get("character_maximum_length");

            TableSchema tableSchema = schema.computeIfAbsent(tableName, TableSchema::new);
            tableSchema.addColumn(new ColumnSchema(
                columnName, 
                dataType, 
                "YES".equalsIgnoreCase(isNullable), 
                maxLength
            ));
        }

        schema.remove("flyway_schema_history");
        return schema;
    }

    private Map<String, TableSchema> getEntitySchema() {
        Map<String, TableSchema> entitySchema = new HashMap<>();
        var metamodel = entityManager.getMetamodel();
        Set<EntityType<?>> entities = metamodel.getEntities();

        for (EntityType<?> entity : entities) {
            Class<?> entityClass = entity.getJavaType();
            String tableName = getTableName(entityClass);
            TableSchema tableSchema = new TableSchema(tableName);

            Set<Field> allFields = getAllFields(entityClass);
            for (Field field : allFields) {
                ColumnSchema columnSchema = processField(field);
                if (columnSchema != null) {
                    tableSchema.addColumn(columnSchema);
                }
            }

            entitySchema.put(tableName, tableSchema);
        }

        return entitySchema;
    }

    private Set<Field> getAllFields(Class<?> clazz) {
        Set<Field> fields = new LinkedHashSet<Field>();  
        
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        
        return fields;
    }

    private String getTableName(Class<?> entityClass) {
        Table tableAnn = entityClass.getAnnotation(Table.class);
        if (tableAnn != null && !tableAnn.name().isEmpty()) {
            return tableAnn.name();
        }
        return entityClass.getSimpleName().toLowerCase();
    }

    private ColumnSchema processField(Field field) {
        Column columnAnn = field.getAnnotation(Column.class);
        if (columnAnn == null) {
            // Check if it's an ID field without @Column
            if (field.isAnnotationPresent(Id.class)) {
                return new ColumnSchema(field.getName(), mapJavaTypeToSqlType(field), false, null);
            }
            return null;
        }

        String columnName = columnAnn.name().isEmpty() ? field.getName() : columnAnn.name();
        String sqlType = mapJavaTypeToSqlType(field);
        boolean isNullable = isNullable(field, columnAnn);
        Integer maxLength = getMaxLength(field, columnAnn);

        return new ColumnSchema(columnName, sqlType, isNullable, maxLength);
    }

    private boolean isNullable(Field field, Column columnAnn) {
        // Check @Column nullable
        if (!columnAnn.nullable()) {
            return false;
        }
        
        // Check @NotNull annotation
        if (field.isAnnotationPresent(NotNull.class)) {
            return false;
        }
        
        // Check @Id annotation
        if (field.isAnnotationPresent(Id.class)) {
            return false;
        }

        return true;
    }

    private Integer getMaxLength(Field field, Column columnAnn) {
        if (field.getType() == String.class) {
            return columnAnn.length() != 255 ? columnAnn.length() : null;
        }
        return null;
    }

    private String mapJavaTypeToSqlType(Field field) {
        Class<?> javaType = field.getType();
        
        if (UUID.class.equals(javaType)) {
            return "uuid";
        }
        
        if (LocalDateTime.class.equals(javaType)) {
            return "timestamp";
        }
        
        if (String.class.equals(javaType)) {
            return "character varying";
        } else if (Integer.class.equals(javaType) || int.class.equals(javaType)) {
            return "integer";
        } else if (Long.class.equals(javaType) || long.class.equals(javaType)) {
            return "bigint";
        } else if (Boolean.class.equals(javaType) || boolean.class.equals(javaType)) {
            return "boolean";
        } else if (Double.class.equals(javaType) || double.class.equals(javaType)) {
            return "double precision";
        }
        
        return "unknown";
    }

    private List<String> compareSchemaWithEntities() {
        Map<String, TableSchema> dbSchema = fetchCurrentDatabaseSchema();
        Map<String, TableSchema> entitySchema = getEntitySchema();
        List<String> migrationStatements = new ArrayList<>();
        
        Set<String> allTables = new HashSet<>();
        allTables.addAll(dbSchema.keySet());
        allTables.addAll(entitySchema.keySet());
        
        for (String tableName : allTables) {
            TableSchema dbTable = dbSchema.get(tableName);
            TableSchema entityTable = entitySchema.get(tableName);
            
            if (dbTable == null && entityTable != null) {
                migrationStatements.add(generateCreateTableStatement(tableName, entityTable));
                continue;
            }
            
            if (entityTable == null) {
                // Optionally generate DROP TABLE statement if needed
                // migrationStatements.add("DROP TABLE " + tableName + ";");
                continue;
            }
            
            migrationStatements.addAll(compareColumns(tableName, dbTable, entityTable));
        }
        
        return migrationStatements;
    }

    private String generateCreateTableStatement(String tableName, TableSchema tableSchema) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (\n");
        List<String> columnDefinitions = new ArrayList<>();
        
        for (ColumnSchema column : tableSchema.getColumns()) {
            String columnDef = "    " + column.getColumnName() + " " + column.getDataType();
            
            if (column.getMaxLength() != null) {
                columnDef += "(" + column.getMaxLength() + ")";
            }
            
            if (!column.isNullable()) {
                columnDef += " NOT NULL";
            }
            
            columnDefinitions.add(columnDef);
        }
        
        sql.append(String.join(",\n", columnDefinitions));
        sql.append("\n);");
        
        return sql.toString();
    }

    private List<String> compareColumns(String tableName, TableSchema dbTable, TableSchema entityTable) {
        List<String> alterStatements = new ArrayList<>();
        Map<String, ColumnSchema> dbColumns = dbTable.getColumns().stream()
            .collect(Collectors.toMap(ColumnSchema::getColumnName, c -> c));
        Map<String, ColumnSchema> entityColumns = entityTable.getColumns().stream()
            .collect(Collectors.toMap(ColumnSchema::getColumnName, c -> c));
        
        // Handle new and modified columns
        for (ColumnSchema entityColumn : entityTable.getColumns()) {
            String columnName = entityColumn.getColumnName();
            ColumnSchema dbColumn = dbColumns.get(columnName);
            
            if (dbColumn == null) {
                // New column
                alterStatements.add(generateAddColumnStatement(tableName, entityColumn));
            } else {
                // Check for modifications
                if (!dbColumn.getDataType().equalsIgnoreCase(entityColumn.getDataType())) {
                    alterStatements.add(generateAlterColumnTypeStatement(tableName, entityColumn));
                }
                
                if (dbColumn.isNullable() != entityColumn.isNullable()) {
                    alterStatements.add(generateAlterColumnNullableStatement(
                        tableName, entityColumn.getColumnName(), entityColumn.isNullable()));
                }
                
                if (!Objects.equals(dbColumn.getMaxLength(), entityColumn.getMaxLength())) {
                    alterStatements.add(generateAlterColumnLengthStatement(tableName, entityColumn));
                }
            }
        }
        
        return alterStatements;
    }

    private String generateAddColumnStatement(String tableName, ColumnSchema column) {
        StringBuilder sql = new StringBuilder("ALTER TABLE " + tableName + " ADD COLUMN " + 
            column.getColumnName() + " " + column.getDataType());
        
        if (column.getMaxLength() != null) {
            sql.append("(" + column.getMaxLength() + ")");
        }
        
        if (!column.isNullable()) {
            sql.append(" NOT NULL");
        }
        
        return sql.toString() + ";";
    }

    private String generateAlterColumnTypeStatement(String tableName, ColumnSchema column) {
        return String.format("ALTER TABLE %s ALTER COLUMN %s TYPE %s%s;",
            tableName,
            column.getColumnName(),
            column.getDataType(),
            column.getMaxLength() != null ? "(" + column.getMaxLength() + ")" : ""
        );
    }

    private String generateAlterColumnNullableStatement(String tableName, String columnName, boolean nullable) {
        return String.format("ALTER TABLE %s ALTER COLUMN %s %s;",
            tableName,
            columnName,
            nullable ? "DROP NOT NULL" : "SET NOT NULL"
        );
    }

    private String generateAlterColumnLengthStatement(String tableName, ColumnSchema column) {
        return String.format("ALTER TABLE %s ALTER COLUMN %s TYPE %s(%d);",
            tableName,
            column.getColumnName(),
            column.getDataType(),
            column.getMaxLength()
        );
    }

    private void generateMigrationScript(List<String> migrationStatements) {
        if (migrationStatements.isEmpty()) {
            return;
        }

        String filename = generateMigrationFilename();
        
        Path migrationPath = Paths.get(migrationLocation);
        if (!Files.exists(migrationPath)) {
            try {
                Files.createDirectories(migrationPath);
            } catch (IOException e) {
                log.error("Failed to create migration directory", e);
                return;
            }
        }
        
        Path scriptPath = migrationPath.resolve(filename);
        try {
        	 boolean isDuplicate = checkForDuplicateVersion(migrationPath, filename.split("_")[0]);
             if(!isDuplicate && !Files.exists(scriptPath.getFileName()))
             {
            	 String content = String.join("\n\n", migrationStatements);
                 Files.writeString(scriptPath, content);
                 log.info("Generated migration script: {Filename}", filename);
             }
        } catch (IOException e) {
            log.error("Failed to write migration script", e);
        }
    }
    
    private boolean checkForDuplicateVersion(Path migrationPath, String versionPrefix) throws IOException {
        try (Stream<Path> files = Files.list(migrationPath)) {
            return files.anyMatch(file -> file.getFileName().toString().startsWith(versionPrefix));
        }
    }
    
    public String generateMigrationFilename() {
        String nextVersion = getNextVersion();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("V%s__%s.sql", nextVersion, ("schemaupdates"+timestamp));
    }

    private String getNextVersion() {
        try {
            String currentVersion = jdbcTemplate.queryForObject(FLYWAY_VERSION_QUERY, String.class);
            
            if (currentVersion == null) {
                return "1";
            }

            // Extract numeric version from the Flyway version string
            String versionNumber = currentVersion.replaceAll("[^0-9]", "");
            int nextVersionNumber = Integer.parseInt(versionNumber) + 1;

            return String.valueOf(nextVersionNumber);
        } catch (Exception e) {
            log.warn("Could not determine next version from flyway_schema_history, defaulting to V1.");
            return "1"; 
        }
    }
}
