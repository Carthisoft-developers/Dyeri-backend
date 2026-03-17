// com/cuisinvoisin/infrastructure/storage/FileStorageAdapter.java
package com.cuisinvoisin.infrastructure.storage;

import com.cuisinvoisin.domain.exceptions.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
public class FileStorageAdapter {

    private final Path uploadPath;

    public FileStorageAdapter(Path uploadPath) {
        this.uploadPath = uploadPath;
    }

    /**
     * Store a multipart file and return a relative URL path.
     */
    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : "";
        String filename = UUID.randomUUID() + extension;

        try {
            Path targetDir = uploadPath.resolve(subDirectory);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {}", targetFile);
            return "/uploads/" + subDirectory + "/" + filename;
        } catch (IOException e) {
            throw new BusinessRuleException("Failed to store file: " + e.getMessage());
        }
    }

    public void delete(String relativePath) {
        try {
            Path target = uploadPath.resolve(relativePath.replaceFirst("^/uploads/", ""));
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", relativePath, e);
        }
    }
}
