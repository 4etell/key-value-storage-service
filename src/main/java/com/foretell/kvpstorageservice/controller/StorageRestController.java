package com.foretell.kvpstorageservice.controller;

import com.foretell.kvpstorageservice.dto.req.StorageDtoReq;
import com.foretell.kvpstorageservice.service.StorageService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("api/storage")
public class StorageRestController {

    private final StorageService storageService;

    @Autowired
    public StorageRestController(StorageService storageService) {
        this.storageService = storageService;
    }


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

    @PostMapping
    public void setValue(@RequestBody StorageDtoReq storageDtoReq,
                         HttpServletResponse response) {

        if (storageService.write(storageDtoReq.getKey(), storageDtoReq.getValue(), storageDtoReq.getTtlInMs())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

    }


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

    @PostMapping("load")
    public void load(@RequestParam("file") MultipartFile file,
                     HttpServletResponse response) {
        if (storageService.load(file)) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @GetMapping("dump")
    public ResponseEntity<InputStreamResource> dump() throws IOException {

        File file = storageService.dump();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
