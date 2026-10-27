package com.dormsupport.issues.api;

import com.dormsupport.issues.client.AuthServiceUnavailableException;
import com.dormsupport.issues.client.UserProfileNotFoundException;
import com.dormsupport.issues.issue.IssueCategoryNotFoundException;
import com.dormsupport.issues.issue.IssueNotFoundException;
import com.dormsupport.issues.issue.IssueStatusChangeNotAllowedException;
import com.dormsupport.issues.issue.InvalidIssueStatusTransitionException;
import com.dormsupport.issues.issue.ResidentProfileIncompleteException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void validation_shouldReturnBadRequest() {
        assertError(
                handler.validation(mock(MethodArgumentNotValidException.class)),
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Request data is invalid"
        );
    }

    @Test
    void constraintViolation_shouldReturnBadRequest() {
        assertError(
                handler.constraintViolation(mock(ConstraintViolationException.class)),
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Request data is invalid"
        );
    }

    @Test
    void methodValidation_shouldReturnBadRequest() {
        assertError(
                handler.methodValidation(mock(HandlerMethodValidationException.class)),
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Request parameters are invalid"
        );
    }

    @Test
    void argumentTypeMismatch_shouldReturnBadRequest() {
        assertError(
                handler.argumentTypeMismatch(mock(MethodArgumentTypeMismatchException.class)),
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Request parameters are invalid"
        );
    }

    @Test
    void categoryNotFound_shouldReturnNotFound() {
        assertError(
                handler.categoryNotFound(new IssueCategoryNotFoundException()),
                HttpStatus.NOT_FOUND,
                "CATEGORY_NOT_FOUND",
                "Issue category was not found"
        );
    }

    @Test
    void issueNotFound_shouldReturnNotFound() {
        assertError(
                handler.issueNotFound(new IssueNotFoundException()),
                HttpStatus.NOT_FOUND,
                "ISSUE_NOT_FOUND",
                "Issue was not found"
        );
    }

    @Test
    void userNotFound_shouldReturnUnauthorized() {
        assertError(
                handler.userNotFound(new UserProfileNotFoundException()),
                HttpStatus.UNAUTHORIZED,
                "USER_NOT_FOUND",
                "Authenticated user was not found"
        );
    }

    @Test
    void authUnavailable_shouldReturnServiceUnavailable() {
        assertError(
                handler.authUnavailable(
                        new AuthServiceUnavailableException(new RuntimeException("Auth unavailable"))
                ),
                HttpStatus.SERVICE_UNAVAILABLE,
                "AUTH_SERVICE_UNAVAILABLE",
                "Auth service is temporarily unavailable"
        );
    }

    @Test
    void incompleteProfile_shouldReturnBadRequest() {
        assertError(
                handler.incompleteProfile(new ResidentProfileIncompleteException()),
                HttpStatus.BAD_REQUEST,
                "PROFILE_INCOMPLETE",
                "Resident profile must contain block and room"
        );
    }

    @Test
    void statusChangeNotAllowed_shouldReturnForbidden() {
        assertError(
                handler.statusChangeNotAllowed(new IssueStatusChangeNotAllowedException()),
                HttpStatus.FORBIDDEN,
                "STATUS_CHANGE_NOT_ALLOWED",
                "You are not allowed to perform this status change"
        );
    }

    @Test
    void invalidStatusTransition_shouldReturnConflict() {
        assertError(
                handler.invalidStatusTransition(new InvalidIssueStatusTransitionException()),
                HttpStatus.CONFLICT,
                "INVALID_STATUS_TRANSITION",
                "Issue status can only change from NEW to IN_PROGRESS and from IN_PROGRESS to CLOSED"
        );
    }

    private void assertError(
            ResponseEntity<ApiError> response,
            HttpStatus expectedStatus,
            String expectedCode,
            String expectedMessage
    ) {
        assertEquals(expectedStatus, response.getStatusCode());

        ApiError body = response.getBody();
        assertNotNull(body);
        assertEquals(expectedCode, body.code());
        assertEquals(expectedMessage, body.message());
    }
}