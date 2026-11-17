package com.example.hello.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthcheckController.class)
class HealthcheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsExactPlainTextHealthcheckWithoutParametersOrBody() throws Exception {
        mockMvc.perform(get("/api/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().bytes("healthcheck".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
