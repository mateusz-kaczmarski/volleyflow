package pl.volleyflow.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.volleyflow.club.model.ClubAlreadyExists;
import pl.volleyflow.club.model.ClubBasicDto;
import pl.volleyflow.club.model.ClubRequest;
import pl.volleyflow.club.service.ClubService;
import pl.volleyflow.config.GlobalErrorHandler;
import pl.volleyflow.security.JwtAuthenticationFilter;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClubController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalErrorHandler.class)
class ClubControllerTest {

    @MockitoBean
    private ClubService clubService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createClub() throws Exception {
        UUID clubExternalId = UUID.randomUUID();

        ClubRequest request = new ClubRequest(
                "Demo Club",
                "demo.png",
                "Demo club description"
        );

        ClubBasicDto response = ClubBasicDto.builder()
                .externalId(clubExternalId)
                .name("Demo Club")
                .avatar("demo.png")
                .description("Demo club description")
                .userRole("OWNER")
                .build();

        when(clubService.createClub(eq(request), eq("owner@example.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/clubs")
                        .principal(() -> "owner@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value(clubExternalId.toString()))
                .andExpect(jsonPath("$.name").value("Demo Club"))
                .andExpect(jsonPath("$.avatar").value("demo.png"))
                .andExpect(jsonPath("$.description").value("Demo club description"))
                .andExpect(jsonPath("$.userRole").value("OWNER"));

        verify(clubService).createClub(eq(request), eq("owner@example.com"));
    }

    @Test
    void createClubReturnsBadRequestWhenNameIsBlank() throws Exception {
        ClubRequest request = new ClubRequest(
                " ",
                "demo.png",
                "Demo club description"
        );

        mockMvc.perform(post("/api/clubs")
                        .principal(() -> "owner@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("club name cannot be blank"));
    }

    @Test
    void createClubReturnsConflictWhenClubAlreadyExists() throws Exception {
        ClubRequest request = new ClubRequest(
                "Demo Club",
                "demo.png",
                "Demo club description"
        );

        when(clubService.createClub(eq(request), eq("owner@example.com")))
                .thenThrow(new ClubAlreadyExists("Club already exists"));

        mockMvc.perform(post("/api/clubs")
                        .principal(() -> "owner@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.httpStatus").value(409))
                .andExpect(jsonPath("$.message").value("Club already exists"));
    }
}
