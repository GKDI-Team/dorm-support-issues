package com.dormsupport.issues.api;

import com.dormsupport.issues.client.AuthServiceUnavailableException;
import com.dormsupport.issues.client.UserProfileNotFoundException;
import com.dormsupport.issues.issue.IssueCategoryNotFoundException;
import com.dormsupport.issues.issue.IssueNotFoundException;
import com.dormsupport.issues.issue.IssueStatusChangeNotAllowedException;
import com.dormsupport.issues.issue.InvalidIssueStatusTransitionException;
import com.dormsupport.issues.issue.ResidentProfileIncompleteException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Данные запроса заполнены некорректно");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraintViolation(ConstraintViolationException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Данные запроса заполнены некорректно");
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiError> methodValidation(HandlerMethodValidationException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Параметры запроса указаны некорректно");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> argumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Параметры запроса указаны некорректно");
    }

    @ExceptionHandler(IssueCategoryNotFoundException.class)
    ResponseEntity<ApiError> categoryNotFound(IssueCategoryNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "Категория заявки не найдена");
    }

    @ExceptionHandler(IssueNotFoundException.class)
    ResponseEntity<ApiError> issueNotFound(IssueNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "ISSUE_NOT_FOUND", "Заявка не найдена");
    }

    @ExceptionHandler(UserProfileNotFoundException.class)
    ResponseEntity<ApiError> userNotFound(UserProfileNotFoundException exception) {
        return error(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Пользователь не найден. Войдите в систему ещё раз");
    }

    @ExceptionHandler(AuthServiceUnavailableException.class)
    ResponseEntity<ApiError> authUnavailable(AuthServiceUnavailableException exception) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_SERVICE_UNAVAILABLE",
                "Сервис авторизации временно недоступен. Попробуйте позже");
    }

    @ExceptionHandler(ResidentProfileIncompleteException.class)
    ResponseEntity<ApiError> incompleteProfile(ResidentProfileIncompleteException exception) {
        return error(HttpStatus.BAD_REQUEST, "PROFILE_INCOMPLETE",
                "В профиле жильца должны быть указаны блок и комната");
    }

    @ExceptionHandler(IssueStatusChangeNotAllowedException.class)
    ResponseEntity<ApiError> statusChangeNotAllowed(IssueStatusChangeNotAllowedException exception) {
        return error(HttpStatus.FORBIDDEN, "STATUS_CHANGE_NOT_ALLOWED",
                "У вас нет прав на изменение статуса этой заявки");
    }

    @ExceptionHandler(InvalidIssueStatusTransitionException.class)
    ResponseEntity<ApiError> invalidStatusTransition(InvalidIssueStatusTransitionException exception) {
        return error(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION",
                "Нельзя пропускать этапы: меняйте статус последовательно — «Новая» → «В работе» → «Закрыта».");
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(code, message));
    }
}
