package pl.volleyflow.clubmembership.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.volleyflow.clubmembership.model.ClubMembershipCreateRequest;
import pl.volleyflow.clubmembership.model.ClubMembershipDto;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.clubmembership.model.ClubMembershipUpdateRequest;
import pl.volleyflow.clubmembership.model.MemberPosition;
import pl.volleyflow.clubmembership.service.ClubMembershipService;
import pl.volleyflow.config.GlobalErrorHandler;
import pl.volleyflow.security.JwtAuthenticationFilter;
import pl.volleyflow.user.model.UserNotFoundException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClubMembershipController.class)
@Import(GlobalErrorHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ClubMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubMembershipService clubMembershipService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void shouldCreateMembership() throws Exception {
        UUID uuid = UUID.randomUUID();
        ClubMembershipCreateRequest clubMembershipCreateRequest = getClubMembershipCreateRequest(uuid);

        ClubMembershipDto clubMembershipDto = ClubMembershipDto.builder()
                .clubExternalId(uuid)
                .firstName("first name")
                .lastName("last name")
                .build();

        when(clubMembershipService.createMembership(eq(clubMembershipCreateRequest), eq("owner@example.pl")))
                .thenReturn(clubMembershipDto);

        mockMvc.perform(post("/api/memberships")
                        .principal(() -> "owner@example.pl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubMembershipCreateRequest))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.clubExternalId").value(uuid.toString()))
                .andExpect(jsonPath("$.firstName").value("first name"))
                .andExpect(jsonPath("$.lastName").value("last name"));

        verify(clubMembershipService).createMembership(eq(clubMembershipCreateRequest), eq("owner@example.pl"));
    }

    @Test
    void shouldReturnNotFoundWhenUserIsNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        ClubMembershipCreateRequest clubMembershipCreateRequest = getClubMembershipCreateRequest(uuid);

        when(clubMembershipService.createMembership(eq(clubMembershipCreateRequest), eq("owner@example.pl")))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/memberships")
                        .principal(() -> "owner@example.pl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubMembershipCreateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpStatus").value(404))
                .andExpect(jsonPath("$.message").value("User not exists"));

        verify(clubMembershipService).createMembership(eq(clubMembershipCreateRequest), eq("owner@example.pl"));
    }

    @Test
    void shouldReturnPlayersByClub() throws Exception {
        UUID uuid = UUID.randomUUID();
        ClubMembershipDto clubMembershipDto = ClubMembershipDto.builder()
                .clubExternalId(uuid)
                .firstName("first name")
                .lastName("last name")
                .build();

        when(clubMembershipService.getPlayersByClub(eq(uuid), eq("owner@example.pl"), eq(null)))
                .thenReturn(List.of(clubMembershipDto));

        mockMvc.perform(get("/api/memberships/{clubExternalId}/players", uuid)
                        .principal(() -> "owner@example.pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("first name"))
                .andExpect(jsonPath("$[0].lastName").value("last name"));

        verify(clubMembershipService).getPlayersByClub(eq(uuid), eq("owner@example.pl"), eq(null));
    }

    @Test
    void shouldReturnActivePlayersByClub() throws Exception {
        UUID uuid = UUID.randomUUID();
        ClubMembershipDto clubMembershipDto = ClubMembershipDto.builder()
                .clubExternalId(uuid)
                .firstName("first name")
                .lastName("last name")
                .active(true)
                .build();

        when(clubMembershipService.getPlayersByClub(eq(uuid), eq("owner@example.pl"), eq(true)))
                .thenReturn(List.of(clubMembershipDto));

        mockMvc.perform(get("/api/memberships/{clubExternalId}/players", uuid)
                        .param("active", "true")
                        .principal(() -> "owner@example.pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(clubMembershipService).getPlayersByClub(eq(uuid), eq("owner@example.pl"), eq(true));
    }

    @Test
    void shouldReturnMembershipDetails() throws Exception {

        UUID clubExternalId = UUID.randomUUID();
        UUID membershipExternalId = UUID.randomUUID();

        ClubMembershipDto clubMembershipDto = ClubMembershipDto.builder()
                .clubExternalId(clubExternalId)
                .externalId(membershipExternalId)
                .firstName("first name")
                .lastName("last name")
                .build();

        when(clubMembershipService.getClubMembershipDetails(
                eq(clubExternalId),
                eq(membershipExternalId),
                eq("owner@example.pl"))
        ).thenReturn(clubMembershipDto);

        mockMvc.perform(get("/api/memberships/{clubExternalId}/{membershipExternalId}", clubExternalId, membershipExternalId)
                        .principal(() -> "owner@example.pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubExternalId").value(clubExternalId.toString()))
                .andExpect(jsonPath("$.externalId").value(membershipExternalId.toString()));

        verify(clubMembershipService).getClubMembershipDetails(
                eq(clubExternalId),
                eq(membershipExternalId),
                eq("owner@example.pl")
        );
    }

    @Test
    void shouldUpdateMembership() throws Exception {
        UUID clubExternalId = UUID.randomUUID();
        UUID membershipExternalId = UUID.randomUUID();
        ClubMembershipUpdateRequest request = getClubMembershipUpdateRequest();

        ClubMembershipDto clubMembershipDto = ClubMembershipDto.builder()
                .clubExternalId(clubExternalId)
                .externalId(membershipExternalId)
                .role(ClubMembershipRole.PLAYER)
                .firstName("updated first name")
                .lastName("updated last name")
                .build();

        when(clubMembershipService.updateMembership(
                eq(clubExternalId),
                eq(membershipExternalId),
                eq(request),
                eq("owner@example.pl")
        )).thenReturn(clubMembershipDto);

        mockMvc.perform(put("/api/memberships/{clubExternalId}/{membershipExternalId}", clubExternalId, membershipExternalId)
                        .principal(() -> "owner@example.pl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubExternalId").value(clubExternalId.toString()))
                .andExpect(jsonPath("$.externalId").value(membershipExternalId.toString()))
                .andExpect(jsonPath("$.firstName").value("updated first name"))
                .andExpect(jsonPath("$.lastName").value("updated last name"));

        verify(clubMembershipService).updateMembership(
                eq(clubExternalId),
                eq(membershipExternalId),
                eq(request),
                eq("owner@example.pl")
        );
    }

    @Test
    void shouldDeleteMembership() throws Exception {
        UUID clubExternalId = UUID.randomUUID();
        UUID membershipExternalId = UUID.randomUUID();

        mockMvc.perform(delete("/api/memberships/{clubExternalId}/{membershipExternalId}", clubExternalId, membershipExternalId)
                        .principal(() -> "owner@example.pl"))
                .andExpect(status().isNoContent());

        verify(clubMembershipService).deleteMembership(
                eq(clubExternalId),
                eq(membershipExternalId),
                eq("owner@example.pl")
        );
    }

    @Test
    void shouldReturnMembersByClub() throws Exception {
        UUID uuid = UUID.randomUUID();
        ClubMembershipDto clubMembershipDto = ClubMembershipDto.builder()
                .clubExternalId(uuid)
                .role(ClubMembershipRole.TRAINER)
                .firstName("first name")
                .lastName("last name")
                .active(true)
                .build();

        when(clubMembershipService.getAllClubMembers(eq(uuid), eq("owner@example.pl"), eq(true)))
                .thenReturn(List.of(clubMembershipDto));

        mockMvc.perform(get("/api/memberships/{clubExternalId}/members", uuid)
                        .param("active", "true")
                        .principal(() -> "owner@example.pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("TRAINER"))
                .andExpect(jsonPath("$[0].firstName").value("first name"))
                .andExpect(jsonPath("$[0].lastName").value("last name"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(clubMembershipService).getAllClubMembers(eq(uuid), eq("owner@example.pl"), eq(true));
    }

    @Test
    void shouldReturnBadRequestWhenFirstNameIsBlank() throws Exception {
        UUID uuid = UUID.randomUUID();
        ClubMembershipCreateRequest request = new ClubMembershipCreateRequest(
                uuid,
                ClubMembershipRole.PLAYER,
                " ",
                "last name",
                "display name",
                1,
                Set.of(MemberPosition.SETTER),
                LocalDate.now(),
                null
        );

        mockMvc.perform(post("/api/memberships")
                        .principal(() -> "owner@example.pl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("firstName"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("first name cannot be blank"));

        verifyNoInteractions(clubMembershipService);
    }

    private static ClubMembershipCreateRequest getClubMembershipCreateRequest(UUID uuid) {
        return new ClubMembershipCreateRequest(
                uuid,
                ClubMembershipRole.OWNER,
                "first name",
                "last name",
                "display name",
                1,
                Set.of(MemberPosition.SETTER),
                LocalDate.now(),
                null
        );
    }

    private static ClubMembershipUpdateRequest getClubMembershipUpdateRequest() {
        return new ClubMembershipUpdateRequest(
                ClubMembershipRole.PLAYER,
                "updated first name",
                "updated last name",
                "updated display name",
                10,
                Set.of(MemberPosition.SETTER),
                LocalDate.now(),
                null
        );
    }

}
