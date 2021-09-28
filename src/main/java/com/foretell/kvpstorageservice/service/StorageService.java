package com.foretell.kvpstorageservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface StorageService {
    Object read(String key);

    boolean write(String key, Object value, Long ttlInMs);

    Object remove(String key);

    File dump();

    boolean load(MultipartFile file);
}
