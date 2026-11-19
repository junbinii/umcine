package com.umc.movie;

import com.umc.movie.entity.Member;
import com.umc.movie.repository.*;
import com.umc.movie.util.JwtUtil;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Week8IntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired FavoriteRepository favorites;
    @Autowired RatingRepository ratings;
    @Autowired JwtUtil jwt;
    Member member;
    Member other;
    String token;
    String otherToken;

    @BeforeEach
    void setup() {
        String suffix = UUID.randomUUID().toString();
        member = members.save(new Member(suffix + "@test.invalid", "test-hash", suffix));
        other = members.save(new Member("b" + suffix + "@test.invalid", "test-hash", "b" + suffix));
        token = "Bearer " + jwt.createAccessToken(member.getMemberId(), member.getEmail());
        otherToken = "Bearer " + jwt.createAccessToken(other.getMemberId(), other.getEmail());
    }

    @AfterEach
    void cleanup() {
        for (Member owner : new Member[]{member, other}) {
            favorites.deleteAll(favorites.findByMember_MemberIdOrderByFavoriteIdDesc(owner.getMemberId()));
            ratings.deleteAll(ratings.findByMember_MemberIdOrderByRatingIdDesc(owner.getMemberId()));
            members.deleteById(owner.getMemberId());
        }
    }

    @Test
    void favoritesLifecycleAndOwnership() throws Exception {
        mvc.perform(get("/favorites").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").isEmpty());
        mvc.perform(get("/favorites/550").header("Authorization", token))
                .andExpect(jsonPath("$.result.isFavorite").value(false));
        mvc.perform(post("/favorites").header("Authorization", token)
                .contentType("application/json").content("{\"movieId\":550}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result.createdAt").isNotEmpty());
        mvc.perform(post("/favorites").header("Authorization", token)
                .contentType("application/json").content("{\"movieId\":550}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("FAVORITE409_1"));
        mvc.perform(post("/favorites").header("Authorization", token)
                .contentType("application/json").content("{\"movieId\":238}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/favorites").header("Authorization", token))
                .andExpect(jsonPath("$.result[0].movieId").value(238))
                .andExpect(jsonPath("$.result[1].movieId").value(550));
        mvc.perform(get("/favorites/550").header("Authorization", token))
                .andExpect(jsonPath("$.result.isFavorite").value(true));
        mvc.perform(get("/favorites").header("Authorization", otherToken))
                .andExpect(jsonPath("$.result").isEmpty());
        mvc.perform(delete("/favorites/550").header("Authorization", otherToken))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("FAVORITE404_1"));
        mvc.perform(delete("/favorites/550").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(content().json("{\"result\":null}"));
        mvc.perform(get("/favorites/550").header("Authorization", token))
                .andExpect(jsonPath("$.result.isFavorite").value(false));
    }

    @Test
    void recordsCommitAndRollback() throws Exception {
        mvc.perform(post("/movie-records").header("Authorization", token)
                .contentType("application/json")
                .content("{\"movieId\":238,\"score\":5,\"addToFavorites\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.rating.movieId").value(238))
                .andExpect(jsonPath("$.result.favorite.movieId").value(238));
        mvc.perform(post("/movie-records").header("Authorization", token)
                .contentType("application/json")
                .content("{\"movieId\":238,\"score\":5,\"addToFavorites\":true}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RATING409_1"));
        mvc.perform(post("/favorites").header("Authorization", token)
                .contentType("application/json").content("{\"movieId\":550}"))
                .andExpect(status().isCreated());
        // HTTP 요청 종료 후 별도 조회로 먼저 INSERT된 평점의 실제 롤백을 확인합니다.
        mvc.perform(post("/movie-records").header("Authorization", token)
                .contentType("application/json")
                .content("{\"movieId\":550,\"score\":5,\"addToFavorites\":true}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("FAVORITE409_1"));
        assertFalse(ratings.existsByMember_MemberIdAndMovieId(member.getMemberId(), 550L));
        assertTrue(favorites.existsByMember_MemberIdAndMovieId(member.getMemberId(), 550L));
        mvc.perform(post("/movie-records").header("Authorization", token)
                .contentType("application/json")
                .content("{\"movieId\":551,\"score\":4,\"addToFavorites\":false}"))
                .andExpect(status().isCreated()).andExpect(content().json("{\"result\":{\"favorite\":null}}"));
        assertFalse(favorites.existsByMember_MemberIdAndMovieId(member.getMemberId(), 551L));
    }

    @Test
    void authenticationAndValidation() throws Exception {
        mvc.perform(post("/favorites").contentType("application/json").content("{\"movieId\":550}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_2"));
        mvc.perform(post("/movie-records").contentType("application/json")
                .content("{\"movieId\":238,\"score\":5,\"addToFavorites\":true}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/favorites").header("Authorization", token)
                .contentType("application/json").content("{\"movieId\":0}"))
                .andExpect(status().isBadRequest());
        for (String body : new String[]{
                "{\"movieId\":238,\"score\":6,\"addToFavorites\":true}",
                "{\"movieId\":238,\"score\":5}",
                "{\"movieId\":238,\"score\":1.5,\"addToFavorites\":true}"}) {
            mvc.perform(post("/movie-records").header("Authorization", token)
                    .contentType("application/json").content(body)).andExpect(status().isBadRequest());
        }
    }
}
