// com/dyeri/core/infrastructure/storage/FileStorageAdapter.java
package com.dyeri.core.infrastructure.storage;

import com.dyeri.core.domain.exceptions.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@Slf4j
@Component
public class FileStorageAdapter {

    private final Path uploadPath;
    private static final int MAX_IMAGE_DIMENSION = 1280;
    private static final float JPEG_QUALITY = 0.75f;

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

    public Mono<String> storeCompressedImage(FilePart file, String subDir) {
        if (file == null) {
            return Mono.error(new BusinessRuleException("File is empty"));
        }

        String inputName = file.filename();
        String inputExt = ".jpg";
        if (inputName != null && inputName.contains(".")) {
            inputExt = inputName.substring(inputName.lastIndexOf('.'));
        }

        String id = UUID.randomUUID().toString();
        String tempName = id + ".tmp" + inputExt;
        String outputName = id + ".jpg";

        Path targetDir = uploadPath.resolve(subDir);
        Path tempFile = targetDir.resolve(tempName);
        Path outputFile = targetDir.resolve(outputName);

        try {
            Files.createDirectories(targetDir);
        } catch (Exception e) {
            return Mono.error(new BusinessRuleException("Cannot create upload directory"));
        }

        return file.transferTo(tempFile)
                .then(Mono.fromCallable(() -> {
                    compressToJpeg(tempFile, outputFile);
                    Files.deleteIfExists(tempFile);
                    return "/uploads/" + subDir + "/" + outputName;
                }).subscribeOn(Schedulers.boundedElastic()))
                .doOnSuccess(url -> log.info("Stored compressed image: {}", url));
    }

    public Mono<byte[]> readBytes(String storedUrl) {
        if (storedUrl == null || storedUrl.isBlank()) {
            return Mono.error(new BusinessRuleException("File url is empty"));
        }

        return Mono.fromCallable(() -> {
            Path filePath = resolveStoredPath(storedUrl);
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                throw new BusinessRuleException("File not found");
            }
            return Files.readAllBytes(filePath);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Path resolveStoredPath(String storedUrl) {
        String relative = storedUrl.startsWith("/") ? storedUrl.substring(1) : storedUrl;
        if (relative.startsWith("uploads/")) {
            relative = relative.substring("uploads/".length());
        }

        Path resolved = uploadPath.resolve(relative).normalize();
        if (!resolved.startsWith(uploadPath)) {
            throw new BusinessRuleException("Invalid file path");
        }
        return resolved;
    }

    private void compressToJpeg(Path source, Path target) throws Exception {
        BufferedImage input = ImageIO.read(source.toFile());
        if (input == null) {
            throw new BusinessRuleException("Unsupported image format");
        }

        BufferedImage resized = resizeIfNeeded(input, MAX_IMAGE_DIMENSION);
        BufferedImage rgbImage = new BufferedImage(
                resized.getWidth(),
                resized.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = rgbImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
        g.drawImage(resized, 0, 0, null);
        g.dispose();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new BusinessRuleException("JPEG writer not available");
        }

        ImageWriter writer = writers.next();
        try (FileImageOutputStream output = new FileImageOutputStream(target.toFile())) {
            writer.setOutput(output);
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(JPEG_QUALITY);
            }
            writer.write(null, new IIOImage(rgbImage, null, null), writeParam);
        } finally {
            writer.dispose();
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage image, int maxDim) {
        int width = image.getWidth();
        int height = image.getHeight();
        int largest = Math.max(width, height);

        if (largest <= maxDim) {
            return image;
        }

        double scale = (double) maxDim / (double) largest;
        int newWidth = Math.max(1, (int) Math.round(width * scale));
        int newHeight = Math.max(1, (int) Math.round(height * scale));

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resized;
    }
}
