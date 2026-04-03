package com.costrip.costrip_backend.exception.exception.advice;

import com.costrip.costrip_backend.exception.ResourceNotFoundException;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class DefaultExceptionAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorObject> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(ex.getHttpStatus().value());
        errorObject.setMessage(ex.getMessage());

        log.error(ex.getMessage(), ex);

        return new ResponseEntity<>(errorObject, HttpStatusCode.valueOf(ex.getHttpStatus().value()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<Object> handleException(HttpMessageNotReadableException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", e.getMessage());
        result.put("httpStatus", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ErrorObject> handleAuthenticationException(AuthenticationException e) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage(e.getMessage());

        log.warn("Authentication failed: {}", e.getMessage());

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    // JWT 만료/위변조 같은 인증 실패는 서버 오류가 아니라 401로 응답한다.
    @ExceptionHandler({JwtException.class, AuthenticationCredentialsNotFoundException.class})
    protected ResponseEntity<ErrorObject> handleJwtException(RuntimeException e) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage(e.getMessage());

        log.warn("JWT authentication failed: {}", e.getMessage());

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorObject> handleAccessDeniedException(AccessDeniedException e) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.FORBIDDEN.value());
        errorObject.setMessage(e.getMessage());

        log.warn("Access denied: {}", e.getMessage());

        return new ResponseEntity<>(errorObject, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorObject> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorObject.setMessage(e.getMessage());

        log.warn("Invalid request: {}", e.getMessage());

        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    // 업로드 파일이 제한 크기를 넘으면 413으로 응답한다.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ErrorObject> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e
    ) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE.value());
        errorObject.setMessage("업로드 가능한 파일 크기를 초과했습니다.");

        log.warn("Upload size exceeded: {}", e.getMessage());

        return new ResponseEntity<>(errorObject, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ErrorObject> handleException(RuntimeException e) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorObject.setMessage(e.getMessage());

        log.error(e.getMessage(), e);

        return new ResponseEntity<>(errorObject, HttpStatusCode.valueOf(500));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        log.error(ex.getMessage(), ex);

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        400,
                        "입력값 검증 오류",
                        LocalDateTime.now(),
                        errors
                );

        return ResponseEntity.badRequest().body(response);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ValidationErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, String> errors;
    }
}
