package com.foretell.kvpstorageservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foretell.kvpstorageservice.dto.req.StorageDtoReq;
import com.foretell.kvpstorageservice.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StorageRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageService storageService;

    private final String URL_TEMPLATE = "/api/storage";

    private String key;
    private String value;

    @BeforeEach
    void setRandomKeyValue() {
        key = UUID.randomUUID().toString();
        value = UUID.randomUUID().toString();
    }

    @Test
    void getValueWhenKeyValuePairExistThenStatusIsOkAndThenReturnValue() throws Exception {

        storageService.write(key, value, 10000000L);

        this.mockMvc.perform(MockMvcRequestBuilders
                .get(URL_TEMPLATE + "/{key}", key))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(value));
    }

    @Test
    void getValueWhenKeyNotExistsThenStatusIsNotFound() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .get(URL_TEMPLATE + "/{key}", key))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void setValueWhenKeyValueTtlExistAndTtlPositiveThenStatusIsOK() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL_TEMPLATE)
                .content(asJsonString(new StorageDtoReq(key, value, 10000000L)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void setValueWhenKeyValueTtlExistAndTtlNegativeThenStatusIsBadRequest() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL_TEMPLATE)
                .content(asJsonString(new StorageDtoReq(key, value, -10000000L)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void setValueWhenKeyValueTtlExistAndTtlZeroThenStatusIsBadRequest() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL_TEMPLATE)
                .content(asJsonString(new StorageDtoReq(key, value, 0L)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void setValueWhenKeyValueExistAndTtlNotExistsThenStatusIsOK() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL_TEMPLATE)
                .content(asJsonString(new StorageDtoReq(key, value)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void setValueWhenKeyValueTtlExistAndTtlNullThenStatusIsOk() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL_TEMPLATE)
                .content(asJsonString(new StorageDtoReq(key, value, null)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void setValueWhenKeyValueNotExistThenStatusIsBadRequest() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .post(URL_TEMPLATE)
                .content(asJsonString(new StorageDtoReq()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeValueWhenKeyExistsThenStatusIsOkAndThenReturnValue() throws Exception {

        storageService.write(key, value, null);

        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(URL_TEMPLATE + "/{key}", key))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(value));
    }

    @Test
    void removeValueWhenKeyNotExistsThenStatusIsNotFound() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete(URL_TEMPLATE + "/{key}", key))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
