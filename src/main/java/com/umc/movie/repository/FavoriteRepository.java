package com.umc.movie.repository;
import com.umc.movie.entity.Favorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByMember_MemberIdOrderByFavoriteIdDesc(Long memberId);
    boolean existsByMember_MemberIdAndMovieId(Long memberId, Long movieId);
    void deleteByMember_MemberIdAndMovieId(Long memberId, Long movieId);
}
