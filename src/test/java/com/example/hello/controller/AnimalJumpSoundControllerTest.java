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

@WebMvcTest(AnimalJumpSoundController.class)
class AnimalJumpSoundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @CsvSource({"turtle, 엉금엉금", "rabbit, 깡충깡충"})
    void returnsExactUtf8Sound(String animal, String sound) throws Exception {
        mockMvc.perform(post("/api/animals/{animal}/jump-sound", animal))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().encoding("UTF-8"))
                .andExpect(content().bytes(sound.getBytes(UTF_8)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bird", "Turtle", "RABBIT", "거북이", "토끼", " turtle", "rabbit "})
    void rejectsUnsupportedIdentifiers(String animal) throws Exception {
        var uri = UriComponentsBuilder.fromPath("/api/animals/{animal}/jump-sound")
                .buildAndExpand(animal).encode().toUri();
        mockMvc.perform(post(uri))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsMissingAnimal() throws Exception {
        mockMvc.perform(post("/api/animals/jump-sound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsGet() throws Exception {
        mockMvc.perform(get("/api/animals/turtle/jump-sound"))
                .andExpect(status().isMethodNotAllowed());
    }

    @ParameterizedTest
    @CsvSource({"turtle, 엉금엉금", "rabbit, 깡충깡충"})
    void ignoresRequestBodyAndContentType(String animal, String sound) throws Exception {
        mockMvc.perform(post("/api/animals/{animal}/jump-sound", animal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ignored\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().bytes(sound.getBytes(UTF_8)));
    }

    @Test
    void rejectsJsonResponseRequest() throws Exception {
        mockMvc.perform(post("/api/animals/turtle/jump-sound").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }
}
