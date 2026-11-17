package com.example.hello.exception;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.hello.controller.HelloController;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
@Import(GlobalExceptionHandlerTest.FailureController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hidesInternalDetailsInServerError() throws Exception {
        mockMvc.perform(get("/test/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().bytes("{\"message\":\"서버 오류가 발생했습니다.\"}".getBytes(UTF_8)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "*/*", "application/*", "text/plain, application/json"})
    void returnsJsonWhenAccepted(String accept) throws Exception {
        mockMvc.perform(get("/test/bad-request").accept(accept))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().bytes("{\"message\":\"잘못된 요청입니다.\"}".getBytes(UTF_8)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"text/plain", "application/xml", "application/json;q=0", "*/*;q=1, application/json;q=0"})
    void doesNotForceJsonWhenExcluded(String accept) throws Exception {
        for (String path : List.of("/test/bad-request", "/test/failure")) {
            mockMvc.perform(get(path).accept(accept))
                    .andExpect(status().is(path.endsWith("failure") ? 500 : 400))
                    .andExpect(header().doesNotExist(HttpHeaders.CONTENT_TYPE))
                    .andExpect(content().string(""));
        }
    }

    @Test
    void preservesMissingBodyStatusWhenOnlyPlainTextIsAccepted() throws Exception {
        mockMvc.perform(post("/api/hello").contentType(MediaType.TEXT_PLAIN).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void preservesStatusExceptionHeadersWithoutExposingReason() throws Exception {
        mockMvc.perform(get("/test/rate-limit"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "60"))
                .andExpect(content().bytes("{\"message\":\"요청을 처리할 수 없습니다.\"}".getBytes(UTF_8)));
    }

    @Test
    void preservesFrameworkStatusesAndAllowHeader() throws Exception {
        mockMvc.perform(post("/test/failure"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, org.hamcrest.Matchers.containsString("GET")));
        mockMvc.perform(get("/api/hello").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
        mockMvc.perform(post("/api/hello").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnsupportedMediaType());
        mockMvc.perform(get("/not-mapped"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/test/annotated"))
                .andExpect(status().isConflict());
    }

    @RestController
    static class FailureController {
        @GetMapping("/test/failure")
        String failure() {
            throw new IllegalStateException("Internal database failure at /private/server/config");
        }

        @GetMapping("/test/bad-request")
        String badRequest() {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Secret internal reason");
        }

        @GetMapping("/test/rate-limit")
        String rateLimit() {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Secret internal reason") {
                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set(HttpHeaders.RETRY_AFTER, "60");
                    return headers;
                }
            };
        }

        @GetMapping("/test/annotated")
        String annotated() {
            throw new ConflictException();
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    static class ConflictException extends RuntimeException {
    }
}
