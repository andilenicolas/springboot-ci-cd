package com.example.project.seeder;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import com.github.javafaker.Faker;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import com.example.project.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.example.project.repository.readonly.IReadOnlyRepository;
import com.example.project.repository.writeonly.IWriteOnlyRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder 
{
    @Value("${app.seed-database}")
    private String appSeedDatabase;
    
    @Autowired
    private final IReadOnlyRepository readOnlyRepository;
    
    @Autowired
    private final IWriteOnlyRepository writeOnlyRepository;
  
    @Transactional
    public void seedDatabase() {
        seedDatabase(false);
    }
    
    @Transactional
    public void seedDatabase(boolean override) {
		boolean shouldSeed = Boolean.parseBoolean(appSeedDatabase) || override;
        if(!shouldSeed) return;
        
    	seedUsers();
    }
    

    private void seedUsers() {
        if (readOnlyRepository.count(User.class) > 0) return;
        
        Faker faker = new Faker();
        List<User> users = IntStream.range(0, 5).mapToObj(i -> 
        {
            User user = User
	            		.builder()
	            		.email(faker.internet().emailAddress())
	            		.build();
            return user;
        }).collect(Collectors.toList());

        writeOnlyRepository.saveAll(users);
        log.info("Seeded {TotalUsers} into the database.", users.size());
    }
}
