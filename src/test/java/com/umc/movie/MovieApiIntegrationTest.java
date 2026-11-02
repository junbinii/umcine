package com.umc.movie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Runs against the local MySQL seed data; no replacement in-memory database.
@SpringBootTest
@AutoConfigureMockMvc
class MovieApiIntegrationTest {
    @Autowired MockMvc mvc;

    @Test void nicknameAvailability() throws Exception {
        mvc.perform(get("/members/nickname/{nickname}", "수현"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/members/nickname/{nickname}", "unused_week5_member"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
    }
    @Test void emailAvailability() throws Exception {
        mvc.perform(get("/members/email/{email}", "noco@umc.com"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/members/email/{email}", "unused_week5@umc.com"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
    }
    @Test void ratingsAreOrderedAndExposeOnlyDto() throws Exception {
        mvc.perform(get("/members/1/ratings"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ratingId").value(2))
                .andExpect(jsonPath("$[0].movieId").value(155))
                .andExpect(jsonPath("$[1].ratingId").value(1))
                .andExpect(jsonPath("$[0].createdAt", not(emptyString())))
                .andExpect(jsonPath("$[0]", aMapWithSize(6)))
                .andExpect(jsonPath("$[0].member").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }
    @Test void otherMembersRatingsAreIsolated() throws Exception {
        mvc.perform(get("/members/2/ratings"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].score").value(3));
    }
    @Test void missingMemberReturns404() throws Exception {
        mvc.perform(get("/members/999999/ratings")).andExpect(status().isNotFound());
    }
    @Test void allowedOriginGetsCorsHeader() throws Exception {
        mvc.perform(get("/members/1/ratings").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
    @Test void unapprovedOriginIsRejected() throws Exception {
        mvc.perform(get("/members/1/ratings").header("Origin", "http://localhost:9999"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
    @Test void preflightIsHandled() throws Exception {
        mvc.perform(options("/members/1/ratings").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")));
    }
}
