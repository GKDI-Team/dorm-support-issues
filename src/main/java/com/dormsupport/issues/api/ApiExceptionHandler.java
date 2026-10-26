package com.dormsupport.issues.api;

import com.dormsupport.issues.client.AuthServiceUnavailableException;
import com.dormsupport.issues.client.UserProfileNotFoundException;
import com.dormsupport.issues.issue.IssueCategoryNotFoundException;
import com.dormsupport.issues.issue.IssueNotFoundException;
import com.dormsupport.issues.issue.IssueStatusChangeNotAllowedException;
import com.dormsupport.issues.issue.ResidentProfileIncompleteException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request data is invalid");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraintViolation(ConstraintViolationException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request data is invalid");
    }

    @ExceptionHandler(IssueCategoryNotFoundException.class)
    ResponseEntity<ApiError> categoryNotFound(IssueCategoryNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "Issue category was not found");
    }

    @ExceptionHandler(IssueNotFoundException.class)
    ResponseEntity<ApiError> issueNotFound(IssueNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "ISSUE_NOT_FOUND", "Issue was not found");
    }

    @ExceptionHandler(UserProfileNotFoundException.class)
    ResponseEntity<ApiError> userNotFound(UserProfileNotFoundException exception) {
        return error(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Authenticated user was not found");
    }

    @ExceptionHandler(AuthServiceUnavailableException.class)
    ResponseEntity<ApiError> authUnavailable(AuthServiceUnavailableException exception) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_SERVICE_UNAVAILABLE",
                "Auth service is temporarily unavailable");
    }

    @ExceptionHandler(ResidentProfileIncompleteException.class)
    ResponseEntity<ApiError> incompleteProfile(ResidentProfileIncompleteException exception) {
        return error(HttpStatus.BAD_REQUEST, "PROFILE_INCOMPLETE",
                "Resident profile must contain block and room");
    }

    @ExceptionHandler(IssueStatusChangeNotAllowedException.class)
    ResponseEntity<ApiError> statusChangeNotAllowed(IssueStatusChangeNotAllowedException exception) {
        return error(HttpStatus.FORBIDDEN, "STATUS_CHANGE_NOT_ALLOWED",
                "You are not allowed to perform this status change");
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(code, message));
    }
}
