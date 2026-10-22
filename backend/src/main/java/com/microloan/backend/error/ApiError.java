package com.microloan.backend.error;

import java.time.Instant;
import java.util.Map;

public record ApiError(
  Instant timestamp,
  int status,
  String error,
  String message,
  String path,
  Map<String, String> validationErrors
) {
  public static ApiError of(
    int status,
    String message,
    String path,
    Map<String, String> fields
  ) {
    return new ApiError(
      Instant.now(),
      status,
      org.springframework.http.HttpStatus.valueOf(status).getReasonPhrase(),
      message,
      path,
      fields
    );
  }
}
