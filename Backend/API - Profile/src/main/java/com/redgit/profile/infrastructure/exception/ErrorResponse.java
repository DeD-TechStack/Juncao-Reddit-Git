package com.redgit.profile.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private Object details;
    private LocalDateTime timestamp;
}
