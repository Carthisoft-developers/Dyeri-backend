// com/dyeri/core/infrastructure/storage/FileStorageAdapter.java
package com.dyeri.core.infrastructure.storage;

import com.dyeri.core.domain.exceptions.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
public class FileStorageAdapter {

    private final Path uploadPath;

    public FileStorageAdapter(@Value("${app.storage.upload-dir:uploads}") String dir) {
        this.uploadPath = Paths.get(dir).toAbsolutePath().normalize();
        this.uploadPath.toFile().mkdirs();
    }

    public Mono<String> store(FilePart file, String subDir) {
        if (file == null) return Mono.error(new BusinessRuleException("File is empty"));
        String ext = "";
        String fname = file.filename();
        if (fname.contains(".")) ext = fname.substring(fname.lastIndexOf('.'));
        String filename = UUID.randomUUID() + ext;
        Path targetDir = uploadPath.resolve(subDir);
        Path targetFile = targetDir.resolve(filename);
        try { Files.createDirectories(targetDir); } catch (Exception e) {
            return Mono.error(new BusinessRuleException("Cannot create upload directory"));
        }
        return file.transferTo(targetFile)
                .thenReturn("/uploads/" + subDir + "/" + filename)
                .doOnSuccess(url -> log.info("Stored file: {}", url));
    }
}
