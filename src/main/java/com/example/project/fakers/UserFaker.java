package com.example.project.fakers;

import java.util.List;
import java.util.stream.IntStream;
import com.github.javafaker.Faker;
import java.util.stream.Collectors;
import com.example.project.entity.User;

public class UserFaker 
{
	public static List<User> getFakeUsers()
	{
        List<User> users = getFakeUsers(5);
        return users;
	}
	
	public static List<User> getFakeUsers(int count)
	{
        Faker faker = new Faker();
        List<User> users = IntStream.range(0, count).mapToObj(i -> 
        {
            User user = User
	            		.builder()
	            		.email(faker.internet().emailAddress())
	            		.build();
            return user;
        }).collect(Collectors.toList());
        
        return users;
	}

	public static User getFakeUser()
	{
        Faker faker = new Faker();
        User user = User
        		.builder()
        		.email(faker.internet().emailAddress())
        		.build();
        
        return user;
	}
}
