package com.odontologiaintegralfm.configuration.appconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);     // Hilos activos
        executor.setMaxPoolSize(10);     // Máximo de hilos simultáneos
        executor.setQueueCapacity(50);   // Tareas que pueden esperar
        executor.setThreadNamePrefix("AsyncMail-");
        executor.initialize();
        return executor;
    }
}
