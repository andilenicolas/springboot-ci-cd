package com.example.project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProjectApplicationTests {

	@Test
	void contextLoads() {
	    // Verify the application context loads without issues
        assertEquals(1, 1); // Simple assertion to validate the test runs
	}
	
	@Test
	void basicTest() {
		// A simple functional test
		String expected = "Hello, World!";
		String actual = "Hello, World!";
		assertEquals(expected, actual, "Strings should match");
	 }

}
