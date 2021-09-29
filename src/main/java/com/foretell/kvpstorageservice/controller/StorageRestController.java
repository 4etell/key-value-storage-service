package com.foretell.kvpstorageservice.controller;

import com.foretell.kvpstorageservice.data.StoringData;
import com.foretell.kvpstorageservice.dto.req.StorageDtoReq;
import com.foretell.kvpstorageservice.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("api/storage")
public class StorageRestController {

    private final StorageService storageService;

    @Autowired
    public StorageRestController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Operation(summary = "Get storage", description = "This operation returns storage map in json",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Operation succeeded, you got storage map in json")
            }
    )
    @GetMapping
    public Map<String, StoringData> getStorageMap() {
        return storageService.getStorageMap();
    }


    @Operation(summary = "Set value in storage map",
            description = "This operation takes these parameters: key, value, ttlInMs(time to live in milliseconds," +
                    " if ttl parameter is not specified or ttl parameter is null, default ttl will be used)",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Operation succeeded, new value set in storage"),
                    @ApiResponse(responseCode = "400", description = "Bad request, new value was not set")
            })
    @PostMapping
    public void setValue(@RequestBody StorageDtoReq storageDtoReq, HttpServletResponse response) {

        if (storageService.write(storageDtoReq.getKey(), storageDtoReq.getValue(), storageDtoReq.getTtlInMs())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    @Operation(summary = "Get value, which contains key from storage",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation succeeded, you got value, which contains key from storage"),
                    @ApiResponse(responseCode = "404", description = "Could not find value by this key")
            }
    )
    @GetMapping("{key}")
    public Object getValue(@PathVariable String key,
                           HttpServletResponse response) {

        Object value = storageService.read(key);

        if (value != null) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return value;
    }

    @Operation(summary = "Delete value, which contains key from storage",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Kvp just removed"),
                    @ApiResponse(responseCode = "404", description = "Could not find value by this key")
            }
    )

    @DeleteMapping("{key}")
    public Object removeValue(@PathVariable String key,
                              HttpServletResponse response) {

        Object value = storageService.remove(key);

        if (value != null) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return value;

    }

    @Operation(summary = "Load file, which dumped by /api/dump method")
    @PostMapping("load")
    public void load(@RequestPart MultipartFile file,
                     HttpServletResponse response) {
        if (storageService.load(file)) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Operation(summary = "Dump storage in json file")
    @GetMapping("dump")
    public ResponseEntity<InputStreamResource> dump() throws IOException {

        File file = storageService.dump();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
