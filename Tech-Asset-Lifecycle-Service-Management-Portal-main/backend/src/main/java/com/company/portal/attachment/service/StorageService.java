package com.company.portal.attachment.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {

    String store(MultipartFile file, String entityType, String entityId) throws IOException;

    Resource load(String storageKey) throws IOException;

    void delete(String storageKey) throws IOException;
}
