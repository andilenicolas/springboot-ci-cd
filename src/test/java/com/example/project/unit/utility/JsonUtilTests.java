package com.example.project.unit.utility;

import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
public class JsonUtilTests 
{
	@Test
	void test() 
	{
		String expected = "Hello, World!";
		String actual = "Hello, World!";
		assertEquals(expected, actual, "Strings should match");
	 }
}
