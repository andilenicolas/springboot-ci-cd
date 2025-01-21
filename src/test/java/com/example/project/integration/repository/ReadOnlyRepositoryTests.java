package com.example.project.integration.repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import com.example.project.entity.User;
import org.junit.jupiter.api.BeforeEach;
import jakarta.persistence.EntityManager;
import com.example.project.fakers.UserFaker;
import jakarta.persistence.EntityTransaction;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.project.repository.readonly.IReadOnlyRepository;
import com.example.project.repository.writeonly.IWriteOnlyRepository;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
public class ReadOnlyRepositoryTests 
{	
	@Autowired
	private LocalContainerEntityManagerFactoryBean entityManagerFactory;
	
	@Autowired
	private IReadOnlyRepository readOnlyRepository;
	
	@Autowired
	private IWriteOnlyRepository writeOnlyRepository;
	
	@BeforeEach
	void setupEachTest()
	{
		// Clear Database
		EntityManager entityManager = entityManagerFactory.getObject().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try 
        {
            transaction.begin();
            
            // Get all entity tables using native query
            Query query = entityManager.createNativeQuery(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = 'PUBLIC'");
            
            @SuppressWarnings("unchecked")
            List<String> tableNames = query.getResultList();
            tableNames.remove("flyway_schema_history");
            
            // Disable foreign key constraints
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
            
            // Truncate all tables
            for (String tableName : tableNames) {
                entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            }
  
            // Re-enable foreign key constraints
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
	}
    
	@Test
	void shouldReturnEmptyListWhenFindAll() 
	{ 
		 // When
		 List<User> dbUsers = readOnlyRepository.findAll(User.class);
		 
		 // Then
		 assertTrue(dbUsers.isEmpty(), "Retrieved an empty list of users");
	}
	
	@Test
	void shouldReturnListOfEntitiesWhenFindAll() 
	{
		 // Given
		 List<User> fakeUsers = UserFaker.getFakeUsers(5);
		 writeOnlyRepository.saveAll(fakeUsers);
		 
		 // When
		 List<User> dbUsers = readOnlyRepository.findAll(User.class);
		 
		 // Then
		 assertAll(
	            () -> assertEquals(fakeUsers.size(), dbUsers.size(), "Not all users were saved"),
	            () -> assertTrue(dbUsers.containsAll(fakeUsers), "Retrieved users don't match saved users")
	     );
	}
	 
	@Test
	void shouldReturnEntityWhenFindById() 
	 {
		// Given
		 User fakeUser = UserFaker.getFakeUser();
		 writeOnlyRepository.save(fakeUser);
		 
		 // When
		 Optional<User> dbUser = readOnlyRepository.findById(User.class, fakeUser.getId());
		 
		 // Then
		 assertAll(
	            () -> assertTrue(dbUser.isPresent(), "User should exist"),
	            () -> assertEquals(fakeUser, dbUser.get(), "Retrieved user should match saved user")
	     );
	 }
	 
	@Test
	void shouldReturnEmptyOptionalForNonExistentIdWhenFindById() 
	{
		// When
		Optional<User> dbUser = readOnlyRepository.findById(User.class, UUID.fromString("26614f26-8e0c-46ea-af11-095aa02bc098"));
		 
		 // Then
		 assertTrue(dbUser.isEmpty(), "Should return empty Optional for non-existent ID");
	}
	
}
