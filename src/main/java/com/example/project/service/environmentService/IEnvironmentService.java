package com.example.project.service.environmentService;

import org.springframework.core.env.Environment;

public interface IEnvironmentService 
{
	Environment getEnv();
    boolean isProdEnv();
    boolean isDevEnv();
    boolean isDockerEnv(); 
    boolean isTestEnv();
}
