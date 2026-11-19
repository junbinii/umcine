package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiResponse<Void>> apiFailure(ApiException error) {
        return ResponseEntity.status(error.getStatus()).body(ApiResponse.failure(error.getCode(), error.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> invalidForm(MethodArgumentNotValidException error) {
        var field = error.getBindingResult().getFieldErrors().getFirst();
        return failure(400, field.getField() + " - " + field.getDefaultMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiResponse<Void>> invalidType() {
        return failure(400, "요청 값의 형식을 확인해주세요.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiResponse<Void>> businessRule(ResponseStatusException error) {
        return failure(error.getStatusCode().value(), error.getReason());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<Void>> constraintViolation(DataIntegrityViolationException error) {
        String detail = error.getMostSpecificCause().getMessage();
        if (detail != null && detail.contains("uk_favorite_member_movie")) {
            return ResponseEntity.status(409).body(ApiResponse.failure(
                    "FAVORITE409_1", "이미 이 영화를 즐겨찾기하셨습니다."));
        }
        if (detail != null && detail.contains("uk_rating_member_movie")) {
            return ResponseEntity.status(409).body(ApiResponse.failure(
                    "RATING409_1", "이미 이 영화에 평점을 남기셨습니다."));
        }
        return failure(409, "데이터베이스 제약 조건과 충돌하여 저장할 수 없습니다.");
    }

    private ResponseEntity<ApiResponse<Void>> failure(int status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.failure(status, message));
    }
}
