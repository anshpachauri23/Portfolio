package com.company.portal.attachment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;

    public LocalStorageService(@Value("${app.storage.local-dir:./uploads}") String localDir) {
        this.rootLocation = Paths.get(localDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String entityType, String entityId) throws IOException {
        Path dir = rootLocation.resolve(entityType).resolve(entityId);
        Files.createDirectories(dir);

        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = dir.resolve(key);
        file.transferTo(target.toFile());
        return entityType + "/" + entityId + "/" + key;
    }

    @Override
    public Resource load(String storageKey) throws IOException {
        Path file = rootLocation.resolve(storageKey);
        if (!Files.exists(file)) {
            throw new IOException("File not found: " + storageKey);
        }
        return new FileSystemResource(file);
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Path file = rootLocation.resolve(storageKey);
        Files.deleteIfExists(file);
    }
}
