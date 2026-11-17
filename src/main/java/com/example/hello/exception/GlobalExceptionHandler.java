package com.example.hello.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (ex instanceof ResponseStatusException || ex instanceof HttpMessageNotReadableException) {
            return errorResponse(status, headers, request);
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception ex, WebRequest request) throws Exception {
        // Leave exceptions with a declared HTTP status to Spring's existing resolvers.
        if (AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class) != null) {
            throw ex;
        }
        if (ex instanceof ErrorResponse error) {
            return ResponseEntity.status(error.getStatusCode()).headers(error.getHeaders()).build();
        }
        log.error("Unhandled application exception", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY, request);
    }

    private ResponseEntity<Object> errorResponse(HttpStatusCode status, HttpHeaders headers, WebRequest request) {
        var response = ResponseEntity.status(status).headers(headers);
        if (!acceptsJson(request)) {
            return response.build();
        }
        String message = switch (status.value()) {
            case 400 -> "잘못된 요청입니다.";
            case 404 -> "요청한 대상을 찾을 수 없습니다.";
            default -> status.is5xxServerError() ? "서버 오류가 발생했습니다." : "요청을 처리할 수 없습니다.";
        };
        return response.contentType(MediaType.APPLICATION_JSON).body(Map.of("message", message));
    }

    private boolean acceptsJson(WebRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept == null) {
            return true;
        }
        try {
            MediaType bestMatch = null;
            for (MediaType type : MediaType.parseMediaTypes(accept)) {
                if (type.isCompatibleWith(MediaType.APPLICATION_JSON)
                        && (bestMatch == null || specificity(type) > specificity(bestMatch)
                            || (specificity(type) == specificity(bestMatch)
                                && type.getQualityValue() > bestMatch.getQualityValue()))) {
                    bestMatch = type;
                }
            }
            return bestMatch != null && bestMatch.getQualityValue() > 0;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private int specificity(MediaType type) {
        return type.isWildcardType() ? 0 : type.isWildcardSubtype() ? 1 : 2;
    }
}
