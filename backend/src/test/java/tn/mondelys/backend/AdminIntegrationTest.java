package tn.mondelys.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mondelys;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "app.jwt.secret=integration_test_secret_key_at_least_32_chars_long",
        "app.jwt.expiration-ms=86400000"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void anonymousUsersCanUsePublicEndpointsButNotAdminOnes() throws Exception {
        mockMvc.perform(get("/api/settings/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantName").value("Mondélys"));

        mockMvc.perform(get("/api/reviews/public"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Aya Ben Salem",
                                "email", "aya@example.com",
                                "rating", 5,
                                "title", "Magnifique",
                                "message", "Une expérience superbe."
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Aya Ben Salem"));

        mockMvc.perform(post("/api/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Karim",
                                "lastName", "Trabelsi",
                                "email", "karim@example.com",
                                "phone", "+21670000000",
                                "subject", "Réservation de table",
                                "message", "Bonjour, je voudrais réserver pour samedi."
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("karim@example.com"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Lina",
                                "lastName", "Mrad",
                                "email", "lina@example.com",
                                "phone", "+21671000000",
                                "reservationDate", "2099-12-31",
                                "reservationTime", "20:00:00",
                                "guestsCount", 4,
                                "occasion", "Anniversaire",
                                "preorder", "Menu dégustation",
                                "specialRequests", "Table calme"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.referenceCode").value(containsString("MDY-")));

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/DashboradAdmin.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void authenticatedAdminCanManageReservationsReviewsMessagesSettingsAndNotifications() throws Exception {
        Cookie adminCookie = adminCookie(loginAndGetToken());

        JsonNode reservation = readJson(mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Meriem",
                                "lastName", "Gharbi",
                                "email", "meriem@example.com",
                                "phone", "+21671123456",
                                "reservationDate", "2099-11-05",
                                "reservationTime", "19:30:00",
                                "guestsCount", 2,
                                "occasion", "Mariage",
                                "preorder", "Sur place",
                                "specialRequests", "Sans gluten"
                        ))))
                .andExpect(status().isOk())
                .andReturn());

        JsonNode review = readJson(mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Nour Haddad",
                                "email", "nour@example.com",
                                "rating", 4,
                                "title", "Très bon",
                                "message", "Très belle ambiance."
                        ))))
                .andExpect(status().isOk())
                .andReturn());

        JsonNode contactMessage = readJson(mockMvc.perform(post("/api/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Walid",
                                "lastName", "Brahmi",
                                "email", "walid@example.com",
                                "phone", "+21679999999",
                                "subject", "Événement privé / Mariage",
                                "message", "Nous voulons privatiser la salle."
                        ))))
                .andExpect(status().isOk())
                .andReturn());

        long reservationId = reservation.get("id").asLong();
        long reviewId = review.get("id").asLong();
        long messageId = contactMessage.get("id").asLong();

        mockMvc.perform(get("/api/admin/auth/session").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@mondelys.tn"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(get("/api/admin/reservations").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(reservationId));

        mockMvc.perform(patch("/api/admin/reservations/{id}/status", reservationId)
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "CONFIRMED",
                                "adminNote", "Client VIP"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.adminNote").value("Client VIP"));

        mockMvc.perform(get("/api/admin/dashboard").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingReservations").value(0))
                .andExpect(jsonPath("$.confirmedReservations").value(1))
                .andExpect(jsonPath("$.totalReservations").value(1));

        mockMvc.perform(get("/api/admin/dashboard/weekly-reservations").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(7)))
                .andExpect(jsonPath("$.values", hasSize(7)));

        mockMvc.perform(get("/api/admin/reviews").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(reviewId));

        mockMvc.perform(patch("/api/admin/reviews/{id}/respond", reviewId)
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "response", "Merci pour votre retour."
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responded").value(true))
                .andExpect(jsonPath("$.responseMessage").value("Merci pour votre retour."));

        mockMvc.perform(get("/api/admin/contact-messages").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(messageId));

        mockMvc.perform(patch("/api/admin/contact-messages/{id}/read", messageId)
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readByAdmin").value(true));

        mockMvc.perform(post("/api/admin/contact-messages/{id}/respond", messageId)
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "response", "Votre demande a bien été enregistrée."
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responded").value(true))
                .andExpect(jsonPath("$.adminResponse").value("Votre demande a bien été enregistrée."));

        mockMvc.perform(get("/api/admin/notifications").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNew").value(0))
                .andExpect(jsonPath("$.pendingReviews").value(0))
                .andExpect(jsonPath("$.pendingReservations").value(0));

        mockMvc.perform(get("/api/admin/settings").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantName").value("Mondélys"));

        mockMvc.perform(put("/api/admin/settings")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "restaurantName", "Mondelys Test",
                                "publicPhone", "+216 70 111 222",
                                "publicEmail", "hello@mondelys.tn",
                                "address", "1 Rue de Test, Tunis",
                                "description", "Configuration de test",
                                "lunchWeek", "12:00 - 15:00",
                                "dinnerWeek", "19:00 - 23:00",
                                "lunchSunday", "12:00 - 15:30",
                                "dinnerSunday", "Ferme"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantName").value("Mondelys Test"))
                .andExpect(jsonPath("$.publicEmail").value("hello@mondelys.tn"));

        mockMvc.perform(post("/api/admin/settings/reset-operational-data").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Les réservations, messages et avis ont été supprimés."));

        mockMvc.perform(get("/api/admin/reservations").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(get("/api/admin/reviews").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(get("/api/admin/contact-messages").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void authenticatedAdminsCanOpenProtectedAdminPages() throws Exception {
        Cookie adminCookie = adminCookie(loginAndGetToken());

        mockMvc.perform(get("/DashboradAdmin.html").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tableau de Bord")));
    }

    private String loginAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "admin@mondelys.tn",
                                "password", "Admin2026!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("adminToken=")))
                .andReturn();

        return readJson(result).get("token").asText();
    }

    private Cookie adminCookie(String token) {
        Cookie cookie = new Cookie("adminToken", token);
        cookie.setPath("/");
        return cookie;
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
