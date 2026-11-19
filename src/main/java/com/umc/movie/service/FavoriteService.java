package com.umc.movie.service;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.entity.Favorite;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {
    private final FavoriteRepository favorites;
    private final MemberRepository members;
    public List<FavoriteResponse> list(Long memberId) {
        return favorites.findByMember_MemberIdOrderByFavoriteIdDesc(memberId).stream()
                .map(FavoriteResponse::from).toList();
    }
    public boolean contains(Long memberId, Long movieId) {
        return favorites.existsByMember_MemberIdAndMovieId(memberId, movieId);
    }
    @Transactional
    public FavoriteResponse create(Long memberId, CreateFavoriteRequest request) {
        Long movieId = request.movieId();
        if (contains(memberId, movieId)) {
            throw new ApiException(HttpStatus.CONFLICT, "FAVORITE409_1", "이미 즐겨찾기한 영화입니다.");
        }
        var member = members.findById(memberId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "MEMBER404_1", "회원을 찾을 수 없습니다."));
        return FavoriteResponse.from(favorites.saveAndFlush(new Favorite(member, movieId)));
    }
    @Transactional
    public void delete(Long memberId, Long movieId) {
        if (!contains(memberId, movieId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "FAVORITE404_1", "즐겨찾기를 찾을 수 없습니다.");
        }
        favorites.deleteByMember_MemberIdAndMovieId(memberId, movieId);
    }
}
