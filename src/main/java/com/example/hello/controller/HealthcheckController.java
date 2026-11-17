package com.example.hello.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthcheckController {

    @GetMapping(value = "/api/healthcheck", produces = MediaType.TEXT_PLAIN_VALUE)
    public String healthcheck() {
        return "healthcheck";
    }
}
