package com.foretell.kvpstorageservice.service;

import com.foretell.kvpstorageservice.data.StoringData;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

public interface StorageService {

    Map<String, StoringData> getStorageMap();

    Object read(String key);

    boolean write(String key, Object value, Long ttlInMs);

    Object remove(String key);

    File dump();

    boolean load(MultipartFile file);
}
