package com.example.hello.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnimalSoundController.class)
class AnimalSoundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @CsvSource({"dog, 멍멍", "cat, 야옹"})
    void returnsExactUtf8Sound(String animal, String sound) throws Exception {
        mockMvc.perform(get("/api/animals/{animal}/sound", animal))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().encoding("UTF-8"))
                .andExpect(content().bytes(sound.getBytes(UTF_8)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bird", "Dog", "CAT", "강아지", "고양이", " dog", "cat "})
    void rejectsUnsupportedIdentifiers(String animal) throws Exception {
        var uri = UriComponentsBuilder.fromPath("/api/animals/{animal}/sound")
                .buildAndExpand(animal).encode().toUri();
        mockMvc.perform(get(uri))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsMissingAnimal() throws Exception {
        mockMvc.perform(get("/api/animals/sound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsPost() throws Exception {
        mockMvc.perform(post("/api/animals/dog/sound"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void rejectsJsonResponseRequest() throws Exception {
        mockMvc.perform(get("/api/animals/dog/sound").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }
}
