package com.example.hello.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AnimalJumpSoundController {

    @PostMapping(value = "/api/animals/{animal}/jump-sound", produces = "text/plain;charset=UTF-8")
    public String jumpSound(@PathVariable String animal) {
        return switch (animal) {
            case "turtle" -> "엉금엉금";
            case "rabbit" -> "깡충깡충";
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
    }
}
