package com.foretell.kvpstorageservice.controller;

import com.foretell.kvpstorageservice.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("api/record")
public class RecordRestController {

    private final StorageService storageService;

    @Autowired
    public RecordRestController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Operation(summary = "Load file, which dumped by /api/record/dump method")
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
