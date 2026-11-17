package com.example.hello.controller;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class HelloController {

    @GetMapping(value = "/api/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String hello() {
        return "Hello World";
    }

    @PostMapping(value = "/api/hello", consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String hello(@RequestBody String greeting) {
        if (!"안녕".equals(greeting)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return "hello";
    }
}
