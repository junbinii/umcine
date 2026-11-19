package com.umc.movie.dto;
import com.umc.movie.entity.Favorite;
import java.time.LocalDateTime;
public record FavoriteResponse(Long favoriteId, Long movieId, LocalDateTime createdAt) {
    public static FavoriteResponse from(Favorite item) {
        return new FavoriteResponse(item.getFavoriteId(), item.getMovieId(), item.getCreatedAt());
    }
}
