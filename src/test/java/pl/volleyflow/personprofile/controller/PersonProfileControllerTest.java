package pl.volleyflow.personprofile.controller;

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
import pl.volleyflow.personprofile.model.PersonProfileCreateRequest;
import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileNotFoundException;
import pl.volleyflow.personprofile.model.PersonProfileUpdateRequest;
import pl.volleyflow.personprofile.service.PersonProfileService;
import pl.volleyflow.security.JwtAuthenticationFilter;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PersonProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalErrorHandler.class)
class PersonProfileControllerTest {

    private static final String EMAIL = "user@example.com";
    private static final UUID PROFILE_ID = UUID.fromString("b3921347-a64e-44a3-8602-d67b7d8b6aec");

    @MockitoBean
    private PersonProfileService personProfileService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateProfile() throws Exception {
        PersonProfileCreateRequest request = new PersonProfileCreateRequest("Jan", "Kowalski", "Jan", 40);
        when(personProfileService.createProfile(request, EMAIL)).thenReturn(profileDto());

        mockMvc.perform(post("/api/profiles")
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(PROFILE_ID.toString()))
                .andExpect(jsonPath("$.firstName").value("Jan"));

        verify(personProfileService).createProfile(request, EMAIL);
    }

    @Test
    void shouldRejectBlankFirstName() throws Exception {
        PersonProfileCreateRequest request = new PersonProfileCreateRequest(" ", "Kowalski", null, null);

        mockMvc.perform(post("/api/profiles")
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("firstName"));

        verify(personProfileService, never()).createProfile(any(PersonProfileCreateRequest.class), anyString());
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        PersonProfileUpdateRequest request = new PersonProfileUpdateRequest(null, null, "Jan", 40);
        when(personProfileService.updateProfile(request, EMAIL)).thenReturn(profileDto());

        mockMvc.perform(put("/api/profiles")
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Jan"));

        verify(personProfileService).updateProfile(request, EMAIL);
    }

    @Test
    void shouldGetProfileByExternalId() throws Exception {
        when(personProfileService.getProfileByExternalId(PROFILE_ID)).thenReturn(profileDto());

        mockMvc.perform(get("/api/profiles/{profileExternalId}", PROFILE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(PROFILE_ID.toString()));

        verify(personProfileService).getProfileByExternalId(PROFILE_ID);
    }

    @Test
    void shouldReturnNotFoundForMissingProfile() throws Exception {
        when(personProfileService.getProfileByExternalId(PROFILE_ID))
                .thenThrow(new PersonProfileNotFoundException("Profile does not exist"));

        mockMvc.perform(get("/api/profiles/{profileExternalId}", PROFILE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpStatus").value(404));
    }

    @Test
    void shouldGetMyProfile() throws Exception {
        when(personProfileService.getMyProfile(EMAIL)).thenReturn(profileDto());

        mockMvc.perform(get("/api/profiles/me").principal(() -> EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"));

        verify(personProfileService).getMyProfile(EMAIL);
    }

    @Test
    void shouldDeleteMyProfile() throws Exception {
        mockMvc.perform(delete("/api/profiles/me").principal(() -> EMAIL))
                .andExpect(status().isNoContent());

        verify(personProfileService).deleteProfile(EMAIL);
    }

    private PersonProfileDto profileDto() {
        return new PersonProfileDto(PROFILE_ID, UUID.randomUUID(), "Jan", "Kowalski", "Jan", 40);
    }
}
