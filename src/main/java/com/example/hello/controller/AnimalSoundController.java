package com.example.hello.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AnimalSoundController {

    @GetMapping(value = "/api/animals/{animal}/sound", produces = "text/plain;charset=UTF-8")
    public String sound(@PathVariable String animal) {
        return switch (animal) {
            case "dog" -> "멍멍";
            case "cat" -> "야옹";
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
    }
}
