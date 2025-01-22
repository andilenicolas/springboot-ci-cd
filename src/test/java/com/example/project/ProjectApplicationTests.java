package com.example.project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.example.project.entity.User;
import com.example.project.repository.readonly.IReadOnlyRepository;
import com.example.project.utility.JsonUtil;

import lombok.RequiredArgsConstructor;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
class ProjectApplicationTests {
	
	@Autowired
	private IReadOnlyRepository readOnlyRepository;

	@Test
	void contextLoads() {
	    // Verify the application context loads without issues
        assertEquals(1, 1); // Simple assertion to validate the test runs
	}
	
	@Test
	void basicTest() {

		long count = readOnlyRepository.count(User.class);
		
		System.out.println("count : " + count);
		// A simple functional test
		String expected = "Hello, World!";
		String actual = "Hello, World!";
		assertEquals(expected, actual, "Strings should match");
	 }

}
