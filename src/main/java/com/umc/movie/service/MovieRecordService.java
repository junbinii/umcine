package com.umc.movie.service;

import com.umc.movie.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovieRecordService {
    private final RatingService ratingService;
    private final FavoriteService favoriteService;

    @Transactional
    public MovieRecordResponse create(Long memberId, CreateMovieRecordRequest request) {
        // 별도 Spring Bean 호출이므로 두 서비스의 쓰기 작업은 이 트랜잭션에 참여합니다.
        RatingResponse rating = ratingService.create(memberId,
                new CreateRatingRequest(request.movieId(), request.score(), request.comment()));
        FavoriteResponse favorite = null;
        if (request.addToFavorites()) {
            favorite = favoriteService.create(memberId, new CreateFavoriteRequest(request.movieId()));
        }
        return new MovieRecordResponse(rating, favorite);
    }
}
