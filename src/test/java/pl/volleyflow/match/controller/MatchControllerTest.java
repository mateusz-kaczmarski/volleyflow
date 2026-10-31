package pl.volleyflow.match.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.volleyflow.config.GlobalErrorHandler;
import pl.volleyflow.match.model.match.MatchCreateRequest;
import pl.volleyflow.match.model.match.MatchDto;
import pl.volleyflow.match.model.match.MatchLocation;
import pl.volleyflow.match.model.set.SetCreateRequest;
import pl.volleyflow.match.model.set.SetDto;
import pl.volleyflow.match.model.set.SetStatisticsDto;
import pl.volleyflow.match.model.set.SetStatisticsUpdateRequest;
import pl.volleyflow.match.service.match.MatchService;
import pl.volleyflow.match.service.set.SetService;
import pl.volleyflow.match.service.setstatistics.SetStatisticsService;
import pl.volleyflow.security.JwtAuthenticationFilter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalErrorHandler.class)
class MatchControllerTest {

    private static final String EMAIL = "user@example.com";
    private final UUID clubId = UUID.randomUUID();
    private final UUID matchId = UUID.randomUUID();
    private final UUID setId = UUID.randomUUID();

    @MockitoBean
    private MatchService matchService;
    @MockitoBean
    private SetService setService;
    @MockitoBean
    private SetStatisticsService setStatisticsService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateMatch() throws Exception {
        MatchCreateRequest request = new MatchCreateRequest(clubId, clubId, UUID.randomUUID(),
                Instant.now().plusSeconds(86400), new MatchLocation("Warszawa", "00-001", "Sportowa", "1"), 2026);
        when(matchService.createMatch(request, EMAIL)).thenReturn(MatchDto.builder().externalId(matchId).build());

        mockMvc.perform(post("/internal/matches")
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value(matchId.toString()));

        verify(matchService).createMatch(request, EMAIL);
    }

    @Test
    void shouldRejectMatchWithoutHomeClub() throws Exception {
        MatchCreateRequest request = new MatchCreateRequest(clubId, null, UUID.randomUUID(),
                Instant.now().plusSeconds(86400), new MatchLocation("Warszawa", "00-001", "Sportowa", "1"), 2026);

        mockMvc.perform(post("/internal/matches")
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(matchJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("homeClubExternalId"));

        verify(matchService, never()).createMatch(any(), any());
    }

    @Test
    void shouldListMatchesForSeason() throws Exception {
        when(matchService.getClubMatches(clubId, EMAIL, 2026)).thenReturn(List.of());

        mockMvc.perform(get("/internal/matches")
                        .param("clubExternalId", clubId.toString())
                        .param("season", "2026")
                        .principal(() -> EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(matchService).getClubMatches(clubId, EMAIL, 2026);
    }

    @Test
    void shouldCreateSet() throws Exception {
        SetCreateRequest request = new SetCreateRequest(25, 20);
        when(setService.createSet(matchId, clubId, request, EMAIL))
                .thenReturn(new SetDto(setId, 1, 25, 20, null));

        mockMvc.perform(post("/internal/matches/{clubId}/{matchId}/set", clubId, matchId)
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homePoints").value(25));

        verify(setService).createSet(matchId, clubId, request, EMAIL);
    }

    @Test
    void shouldDeleteSet() throws Exception {
        mockMvc.perform(delete("/internal/matches/{clubId}/{matchId}/set/{setId}", clubId, matchId, setId)
                        .principal(() -> EMAIL))
                .andExpect(status().isNoContent());

        verify(setService).deleteSet(matchId, clubId, setId, EMAIL);
    }

    @Test
    void shouldGetSetStatistics() throws Exception {
        when(setStatisticsService.getSetStatistics(matchId, clubId, setId, EMAIL))
                .thenReturn(new SetStatisticsDto(List.of()));

        mockMvc.perform(get("/internal/matches/{clubId}/{matchId}/set/{setId}/statistics",
                        clubId, matchId, setId).principal(() -> EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players").isEmpty());

        verify(setStatisticsService).getSetStatistics(matchId, clubId, setId, EMAIL);
    }

    @Test
    void shouldSaveSetStatistics() throws Exception {
        SetStatisticsUpdateRequest request = new SetStatisticsUpdateRequest(List.of());

        mockMvc.perform(put("/internal/matches/{clubId}/{matchId}/set/{setId}/statistics",
                        clubId, matchId, setId)
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(setStatisticsService).saveSetStatistics(matchId, clubId, setId, request, EMAIL);
    }

    @Test
    void shouldFinishMatch() throws Exception {
        mockMvc.perform(post("/internal/matches/{matchId}/finish", matchId).principal(() -> EMAIL))
                .andExpect(status().isOk());

        verify(matchService).finishMatch(matchId, EMAIL);
    }

    private String matchJson(MatchCreateRequest request) {
        return """
                {"createdByClubExternalId":"%s","homeClubExternalId":%s,
                 "awayClubExternalId":"%s","scheduledAt":"%s",
                 "matchLocation":{"city":"Warszawa","zipCode":"00-001",
                                  "street":"Sportowa","buildingNumber":"1"},"season":2026}
                """.formatted(request.createdByClubExternalId(),
                request.homeClubExternalId() == null ? "null" : "\"" + request.homeClubExternalId() + "\"",
                request.awayClubExternalId(), request.scheduledAt());
    }
}
