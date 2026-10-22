package com.microloan.backend.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> validation(
    MethodArgumentNotValidException e,
    HttpServletRequest r
  ) {
    Map<String, String> fields = new LinkedHashMap<>();
    e.getBindingResult()
      .getFieldErrors()
      .forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return response(400, "Please correct the highlighted fields", r, fields);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> status(
    ResponseStatusException e,
    HttpServletRequest r
  ) {
    return response(e.getStatusCode().value(), e.getReason(), r, Map.of());
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class,
    org.springframework.web.multipart.support.MissingServletRequestPartException.class,
  })
  public ResponseEntity<ApiError> malformed(Exception e, HttpServletRequest r) {
    return response(
      400,
      "Invalid request fields, enum value, or JSON body",
      r,
      Map.of()
    );
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiError> size(Exception e, HttpServletRequest r) {
    return response(
      413,
      "Uploaded file exceeds the configured size limit",
      r,
      Map.of()
    );
  }

  @ExceptionHandler(
    org.springframework.web.HttpMediaTypeNotSupportedException.class
  )
  public ResponseEntity<ApiError> media(Exception e, HttpServletRequest r) {
    return response(415, "Unsupported request content type", r, Map.of());
  }

  @ExceptionHandler(
    org.springframework.web.HttpRequestMethodNotSupportedException.class
  )
  public ResponseEntity<ApiError> method(Exception e, HttpServletRequest r) {
    return response(405, "Method not allowed", r, Map.of());
  }

  @ExceptionHandler({
    org.springframework.web.servlet.resource.NoResourceFoundException.class,
    org.springframework.web.servlet.NoHandlerFoundException.class,
  })
  public ResponseEntity<ApiError> missing(Exception e, HttpServletRequest r) {
    return response(404, "Resource not found", r, Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> forbidden(Exception e, HttpServletRequest r) {
    return response(
      403,
      "You do not have permission for this action",
      r,
      Map.of()
    );
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> unauthorized(
    Exception e,
    HttpServletRequest r
  ) {
    return response(401, "Authentication required", r, Map.of());
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    ConcurrencyFailureException.class,
  })
  public ResponseEntity<ApiError> conflict(Exception e, HttpServletRequest r) {
    return response(
      409,
      "The record changed or conflicts with an existing record. Refresh and retry.",
      r,
      Map.of()
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> unexpected(
    Exception e,
    HttpServletRequest r
  ) {
    return response(500, "Unable to complete the request", r, Map.of());
  }

  private ResponseEntity<ApiError> response(
    int s,
    String m,
    HttpServletRequest r,
    Map<String, String> f
  ) {
    return ResponseEntity.status(s).body(
      ApiError.of(s, m, r.getRequestURI(), f)
    );
  }
}
