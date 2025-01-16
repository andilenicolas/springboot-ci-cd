package com.example.project.service.environmentService;

import lombok.Getter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;


@Getter
@Service
@RequiredArgsConstructor
public class EnvironmentService implements IEnvironmentService 
{
	@Autowired
	private final Environment env;

    public boolean isProdEnv() {
        return isActiveProfile("prod");
    }

    public boolean isDevEnv() {
        return isActiveProfile("dev");
    }

    public boolean isDockerEnv() {
        return isActiveProfile("docker");
    }

    public boolean isTestEnv() {
        return isActiveProfile("test");
    }

    private boolean isActiveProfile(String profile) {
        return Arrays.stream(env.getActiveProfiles())
                .anyMatch(activeProfile -> profile.equalsIgnoreCase(activeProfile));
    }
}
