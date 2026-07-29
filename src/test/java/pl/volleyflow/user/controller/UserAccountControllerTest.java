package pl.volleyflow.user.controller;

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
import pl.volleyflow.security.JwtAuthenticationFilter;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountUpdateRequest;
import pl.volleyflow.user.model.UserChangePasswordRequest;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.service.UserAccountService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalErrorHandler.class)
class UserAccountControllerTest {

    private static final UUID EXTERNAL_ID = UUID.fromString("11b8560c-ed43-4122-b6db-46e7698b1b2f");
    private static final String USER_EMAIL = "email@example.pl";
    private static final String PHONE = "123456789";

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnBasicInfo() throws Exception {
        when(userAccountService.getBasicInfoByEmail(USER_EMAIL)).thenReturn(createUserAccountDto());

        mockMvc.perform(get("/api/users/me")
                        .principal(() -> USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(EXTERNAL_ID.toString()))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.phone").value(PHONE));

        verify(userAccountService).getBasicInfoByEmail(USER_EMAIL);
    }

    @Test
    void shouldReturnNotFoundWhenBasicInfoUserDoesNotExist() throws Exception {
        when(userAccountService.getBasicInfoByEmail(USER_EMAIL))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/me")
                        .principal(() -> USER_EMAIL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpStatus").value(404))
                .andExpect(jsonPath("$.message").value("User not exists"));
    }

    @Test
    void shouldChangePassword() throws Exception {
        UserChangePasswordRequest request = new UserChangePasswordRequest("old-password", "new-password");

        mockMvc.perform(put("/api/users/me/password")
                        .principal(() -> USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userAccountService).changePassword(eq(request), eq(USER_EMAIL));
    }

    @Test
    void shouldReturnBadRequestWhenNewPasswordIsTooShort() throws Exception {
        UserChangePasswordRequest request = new UserChangePasswordRequest("old-password", "short");

        mockMvc.perform(put("/api/users/me/password")
                        .principal(() -> USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("newPassword"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("new password must be at least 6 characters"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserAccountUpdateRequest request = new UserAccountUpdateRequest("updated@example.pl", "987654321");
        UserAccountDto response = UserAccountDto.builder()
                .externalId(EXTERNAL_ID)
                .email("updated@example.pl")
                .phone("987654321")
                .build();

        when(userAccountService.updateUserAccount(eq(request), eq(USER_EMAIL))).thenReturn(response);

        mockMvc.perform(put("/api/users/me")
                        .principal(() -> USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(EXTERNAL_ID.toString()))
                .andExpect(jsonPath("$.email").value("updated@example.pl"))
                .andExpect(jsonPath("$.phone").value("987654321"));

        verify(userAccountService).updateUserAccount(eq(request), eq(USER_EMAIL));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatedEmailHasInvalidFormat() throws Exception {
        UserAccountUpdateRequest request = new UserAccountUpdateRequest("invalid-email", PHONE);

        mockMvc.perform(put("/api/users/me")
                        .principal(() -> USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("invalid email format"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .principal(() -> USER_EMAIL))
                .andExpect(status().isNoContent());

        verify(userAccountService).deleteUserAccount(USER_EMAIL);
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.builder()
                .externalId(EXTERNAL_ID)
                .email(USER_EMAIL)
                .phone(PHONE)
                .build();
    }

}
