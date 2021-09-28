package com.foretell.kvpstorageservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foretell.kvpstorageservice.data.StoringData;
import com.foretell.kvpstorageservice.service.impl.StorageServiceImpl;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
public class StorageServiceImplTest {
    @Autowired
    private StorageServiceImpl storageService;

    private String key;
    private String value;

    @BeforeEach
    void setRandomKeyValue() {
        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();
    }

    @Test
    void writeKeyValueTtlAfterTtlKeyValueRemovedThenResultNull() throws InterruptedException {
        storageService.write(key, value, 1000L);
        Thread.sleep(1100);
        Assertions.assertNull(storageService.read(key));
    }

    @Test
    void writeKeyValueWhenThisKeyExistsThenTtlAndValueUpdated() throws InterruptedException {
        storageService.write(key, value, 2000L);
        String anotherValue = UUID.randomUUID().toString();
        storageService.write(key, anotherValue, 30000L);
        Thread.sleep(2100);
        Assertions.assertEquals(anotherValue, storageService.read(key));
    }

    @Test
    void dumpCheckDumpEqualsToStorageMap() throws IOException {

        storageService.write(key, value, 200000L);
        setRandomKeyValue();
        storageService.write(key, value, 200000L);
        setRandomKeyValue();
        storageService.write(key, value, 200000L);

        Map<String, StoringData> storageMapBeforeDump = storageService.getStorageMap();
        File file = storageService.dump();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, StoringData> storageMapAfterDump = objectMapper.readValue(
                file,
                new TypeReference<Map<String, StoringData>>() {
                }
        );
        Assertions.assertTrue(equalStorageMaps(storageMapBeforeDump, storageMapAfterDump));
    }

    @Test
    void loadCheckStorageMapBeforeDumpEqualsStorageMapAfterLoad() throws IOException {
        storageService.write(key, value, 200000L);
        setRandomKeyValue();
        storageService.write(key, value, 200000L);
        setRandomKeyValue();
        storageService.write(key, value, 200000L);

        Map<String, StoringData> storageMapBeforeDump = storageService.getStorageMap();
        File file = storageService.dump();
        Assertions.assertTrue(storageService.load(convertFileToMultiPartFile(file)));
        Map<String, StoringData> storageMapAfterLoad = storageService.getStorageMap();
        Assertions.assertTrue(equalStorageMaps(storageMapBeforeDump, storageMapAfterLoad));

    }

    @Test
    void dumpAndLoadKvpThenAfterTtlKvpRemoved() throws IOException, InterruptedException {
        storageService.write(key, value, 1000L);
        File file1 = storageService.dump();
        Assertions.assertTrue(storageService.load(convertFileToMultiPartFile(file1)));
        Thread.sleep(1100L);
        Assertions.assertNull(storageService.read(key));

        setRandomKeyValue();
        storageService.write(key, value, 1000L);
        File file2 = storageService.dump();
        Thread.sleep(1100L);
        Assertions.assertTrue(storageService.load(convertFileToMultiPartFile(file2)));
        Assertions.assertNull(storageService.read(key));
    }

    private boolean equalStorageMaps(Map<String, StoringData> m1, Map<String, StoringData> m2) {
        for (String key : m1.keySet()) {
            if (!m1.get(key).getValue().equals(m2.get(key).getValue()) ||
                    m1.get(key).getStampTtl() != m2.get(key).getStampTtl())
                return false;
        }
        return true;
    }

    private MultipartFile convertFileToMultiPartFile(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file",
                file.getName(), "text/plain", IOUtils.toByteArray(input));
    }
}
