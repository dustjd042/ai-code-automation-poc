package com.example.hello.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsExactPlainTextGreetingWithoutParametersOrBody() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().bytes("Hello World".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void translatesExactUtf8GreetingToPlainText() throws Exception {
        mockMvc.perform(post("/api/hello")
                        .contentType("text/plain; charset=UTF-8")
                        .content("안녕".getBytes(UTF_8)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().bytes("hello".getBytes(UTF_8)));
    }

    @Test
    void rejectsMissingBody() throws Exception {
        mockMvc.perform(post("/api/hello").contentType("text/plain; charset=UTF-8"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "hello", "안녕하세요", " 안녕", "안녕 ", "안녕\n",
            "\n안녕", "\"안녕\"", "안녕"})
    void rejectsAnythingOtherThanExactGreeting(String body) throws Exception {
        mockMvc.perform(post("/api/hello")
                        .contentType("text/plain; charset=UTF-8")
                        .content(body.getBytes(UTF_8)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "application/xml", "application/octet-stream"})
    void rejectsUnsupportedMediaTypes(String contentType) throws Exception {
        mockMvc.perform(post("/api/hello")
                        .contentType(contentType)
                        .content("안녕".getBytes(UTF_8)))
                .andExpect(status().isUnsupportedMediaType());
    }
}
