package com.crqs.command.cqrs_command.exceptions.handler;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ApiErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    public static ApiErrorResponse of(HttpStatus status, String message, String path) {
        return ApiErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
