package com.umc.movie.controller;
import com.umc.movie.dto.*;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteService service;
    @GetMapping
    public ApiResponse<List<FavoriteResponse>> list(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId) {
        return ApiResponse.ok("즐겨찾기를 조회했습니다.", service.list(memberId));
    }
    @GetMapping("/{movieId}")
    public ApiResponse<FavoriteStatusResponse> get(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId, @PathVariable Long movieId) {
        return ApiResponse.ok("즐겨찾기 상태를 조회했습니다.", new FavoriteStatusResponse(service.contains(memberId, movieId)));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> add(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId, @Valid @RequestBody CreateFavoriteRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created("즐겨찾기를 저장했습니다.", service.create(memberId, request)));
    }
    @DeleteMapping("/{movieId}")
    public ApiResponse<Void> delete(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId, @PathVariable Long movieId) {
        service.delete(memberId, movieId);
        return ApiResponse.ok("즐겨찾기를 해제했습니다.");
    }
}
