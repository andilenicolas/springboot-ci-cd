package com.example.project.config;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.internal.ExceptionHandlerLoggedImpl;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.hibernate.tool.schema.internal.exec.GenerationTargetToScript;
import org.hibernate.tool.schema.spi.ExceptionHandler;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToUrl;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.SourceType;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaCreator;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.ScriptSourceInput;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.hibernate.tool.schema.spi.SourceDescriptor;
import org.hibernate.tool.schema.spi.TargetDescriptor;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Component
public class HibernateSchemaExporter {

	/*
    public void generateSchema() {
        try {
        
        	 
        	
            // Step 1: Configure Hibernate
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect") // Update with your dialect
                    .build();

            MetadataSources metadataSources = new MetadataSources(registry);
            // Add entity classes (or packages) here
            // metadataSources.addAnnotatedClass(YourEntity.class);

            Metadata metadata = metadataSources.buildMetadata();

            
            // Step 2: Define ExecutionOptions
            ExecutionOptions options = new ExecutionOptions() {         	
                @Override
                public boolean shouldManageNamespaces() {
                    return true;
                }

				@Override
				public ExceptionHandler getExceptionHandler() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Map<String, Object> getConfigurationValues() {
					// TODO Auto-generated method stub
					return null;
				}
            };
            
            public class CustomExecutionOptions implements ExecutionOptions {
                private final Map<String, Object> configurationValues;

                public CustomExecutionOptions(Map<String, Object> configurationValues) {
                    this.configurationValues = configurationValues;
                }

                @Override
                public boolean shouldManageNamespaces() {
                    // Return true if Hibernate should manage namespaces
                    return true;
                }

                @Override
                public ExceptionHandler getExceptionHandler() {
                    // Handle exceptions during schema creation
                    return new ExceptionHandler() {
                        @Override
                        public void handleException(Exception exception) {
                            System.err.println("Error during schema creation: " + exception.getMessage());
                            exception.printStackTrace();
                        }
                    };
                }

                @Override
                public Map<String, Object> getConfigurationValues() {
                    // Return the configuration values from the registry
                    return configurationValues;
                }
            }
            
            
            

            // Step 3: Define Source Descriptor
            SourceDescriptor sourceDescriptor = new SourceDescriptor() {
                @Override
                public ScriptSourceInput getScriptSourceInput() {
                    return null; // Not applicable when using METADATA
                }

				@Override
				public SourceType getSourceType() {
					// TODO Auto-generated method stub
					 return SourceType.METADATA;
				}
            };

            // Step 4: Define Target Descriptor
            Path targetFilePath = Path.of("target/generated-schema.sql");
            TargetDescriptor targetDescriptor = new TargetDescriptor() {
                @Override
                public EnumSet<TargetType> getTargetTypes() {
                    return EnumSet.of(TargetType.SCRIPT);
                }

                @Override
                public ScriptTargetOutput getScriptTargetOutput() {
                    try {
                        return new ScriptTargetOutputToUrl(targetFilePath.toUri().toURL(), "UTF-8");
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Invalid file path for schema script", e);
                    }
                }
            };

            // Step 5: Obtain SchemaCreator
            SchemaManagementTool schemaManagementTool = registry.getService(SchemaManagementTool.class);
            SchemaCreator schemaCreator = schemaManagementTool.getSchemaCreator(null);

            // Step 6: Execute schema creation
            schemaCreator.doCreation(
                    metadata,
                    options,
                    (contributable) -> true, // Include all contributables
                    sourceDescriptor,
                    targetDescriptor
            );

            System.out.println("Schema successfully created at: " + targetFilePath.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Schema generation failed", e);
        }
    }
	 */
}
