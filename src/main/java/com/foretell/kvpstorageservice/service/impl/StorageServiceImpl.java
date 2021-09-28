package com.foretell.kvpstorageservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foretell.kvpstorageservice.data.StoringData;
import com.foretell.kvpstorageservice.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Service
public class StorageServiceImpl implements StorageService {
    private Map<String, StoringData> storageMap = new HashMap<>();
    private Timer timer = new Timer();
    private final Map<String, TimerTask> timerTasks = new HashMap<>();

    @Value("${default.ttl}")
    private Long defaultTtlInMs;

    @Value("${load.path}")
    private String loadPath;

    @Override
    public Object read(String key) {
        if (storageMap.containsKey(key)) {
            return storageMap.get(key).getValue();
        } else {
            return null;
        }
    }

    @Override
    public boolean write(String key, Object value, Long ttlInMs) {

        if (key != null && value != null && (ttlInMs == null || ttlInMs > 0)) {

            if (storageMap.containsKey(key) && timerTasks.containsKey(key)) {
                timerTasks.get(key).cancel();
                timer.purge();
            }

            startRemoveTimer(key, ttlInMs);

            Date dateNow = new Date();
            long stampTtl = dateNow.getTime() + defaultTtlInMs;

            if (ttlInMs != null) {
                stampTtl = dateNow.getTime() + ttlInMs;
            }

            storageMap.put(key, new StoringData(value, stampTtl));
            return true;

        } else {
            return false;
        }
    }

    @Override
    public Object remove(String key) {
        if (storageMap.containsKey(key)) {
            if (timerTasks.containsKey(key)) {
                timerTasks.get(key).cancel();
                timer.purge();
            }
            return storageMap.remove(key).getValue();
        } else {
            return null;
        }
    }

    @Override
    public File dump() {

        createLoadDirIfNotExists();
        String filePathName = loadPath + "/storage.json";

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(new File(filePathName), storageMap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new File(filePathName);
    }

    @Override
    public boolean load(MultipartFile file) {
        if (file != null) {

            createLoadDirIfNotExists();
            String filePathName = loadPath + "/storage.json";

            ObjectMapper objectMapper = new ObjectMapper();

            try {

                file.transferTo(new File(filePathName));

                storageMap = objectMapper.readValue(
                        new File(filePathName),
                        new TypeReference<Map<String, StoringData>>() {
                        }
                );


                timer.cancel();
                timer = new Timer();
                timerTasks.clear();

                storageMap = storageMap.entrySet()
                        .stream()
                        .filter(map -> {
                            long stampTtl = map.getValue().getStampTtl();
                            Date dateNow = new Date();
                            if (dateNow.getTime() >= stampTtl) {
                                return false;
                            } else {
                                startRemoveTimer(map.getKey(), stampTtl - dateNow.getTime());
                                return true;
                            }
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void startRemoveTimer(String key, Long ttlInMs) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                storageMap.remove(key);
                System.out.println("Remove key: " + key);
            }
        };

        timerTasks.put(key, timerTask);

        if (ttlInMs == null) {
            timer.schedule(timerTask, defaultTtlInMs);
        } else {
            timer.schedule(timerTask, ttlInMs);
        }

    }

    private void createLoadDirIfNotExists() {
        File loadDir = new File(loadPath);

        if (!loadDir.exists()) {
            loadDir.mkdir();
        }
    }

    public Map<String, StoringData> getStorageMap() {
        return new HashMap<>(storageMap);
    }
}
