// com/cuisinvoisin/infrastructure/config/StorageConfig.java
package com.cuisinvoisin.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    @Value("${app.storage.upload-dir:uploads}")
    private String uploadDir;

    @Bean
    public Path uploadPath() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        path.toFile().mkdirs();
        return path;
    }
}
