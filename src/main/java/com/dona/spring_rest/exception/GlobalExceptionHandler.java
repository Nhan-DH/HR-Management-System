package com.dona.spring_rest.exception;

import com.dona.spring_rest.helper.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        LOGGER.error("ResourceNotFoundException: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.ofError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                "Not Found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException ex) {
        LOGGER.error("DuplicateResourceException: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.ofError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                "Conflict");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        LOGGER.error("MethodArgumentTypeMismatchException: {}", ex.getMessage(), ex);

        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "đúng kiểu dữ liệu";
        String message = String.format("Giá trị truyền vào cho tham số '%s' không hợp lệ. Vui lòng nhập kiểu %s.",
                paramName, requiredType);

        ApiResponse<Object> response = ApiResponse.ofError(
                HttpStatus.BAD_REQUEST.value(),
                message,
                "Bad Request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        LOGGER.error("HttpMessageNotReadableException: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.ofError(
                HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu gửi lên không hợp lệ hoặc sai định dạng JSON.",
                "Bad Request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        LOGGER.error("IllegalArgumentException: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.ofError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "Bad Request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        LOGGER.error("HttpRequestMethodNotSupportedException: {}", ex.getMessage(), ex);

        String supportedMethods = ex.getSupportedMethods() != null
                ? Arrays.stream(ex.getSupportedMethods()).collect(Collectors.joining(", "))
                : "không xác định";

        String message = String.format(
                "Phương thức %s không được hỗ trợ cho endpoint này. Các phương thức được hỗ trợ: %s.",
                ex.getMethod(),
                supportedMethods);

        ApiResponse<Object> response = ApiResponse.ofError(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                message,
                "Method Not Allowed");

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFound(NoHandlerFoundException ex) {
        LOGGER.error("NoHandlerFoundException: {}", ex.getMessage(), ex);

        String message = String.format("Không tìm thấy endpoint: %s %s", ex.getHttpMethod(), ex.getRequestURL());

        ApiResponse<Object> response = ApiResponse.ofError(
                HttpStatus.NOT_FOUND.value(),
                message,
                "Not Found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
        LOGGER.error("Unhandled Exception: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.ofError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
                "Internal Server Error");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.badRequest("Dữ liệu không hợp lệ", errors));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

}
