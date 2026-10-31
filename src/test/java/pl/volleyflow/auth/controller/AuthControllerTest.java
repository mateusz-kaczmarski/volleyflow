package pl.volleyflow.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.auth.model.ConfirmPasswordRequest;
import pl.volleyflow.auth.model.UserLoginRequest;
import pl.volleyflow.auth.model.exception.InvalidCredentialsException;
import pl.volleyflow.auth.service.AuthService;
import pl.volleyflow.config.GlobalErrorHandler;
import pl.volleyflow.security.JwtAuthenticationFilter;
import pl.volleyflow.user.model.UserRegisterRequest;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalErrorHandler.class)
class AuthControllerTest {

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper objectMapper;

    @Test
    void shouldRegister() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("user@example.com", "password123",
                null, "Jan", "Kowalski", null, null);
        UUID userId = UUID.randomUUID();
        when(authService.register(request)).thenReturn(AuthResponse.registerSuccess(userId));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value(userId.toString()));

        verify(authService).register(request);
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        UserLoginRequest request = new UserLoginRequest("not-an-email", "password");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));

        verify(authService, never()).login(any());
    }

    @Test
    void shouldLogin() throws Exception {
        UserLoginRequest request = new UserLoginRequest("user@example.com", "password");
        when(authService.login(request)).thenReturn(AuthResponse.loginSuccess("jwt", UUID.randomUUID()));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"));

        verify(authService).login(request);
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        UserLoginRequest request = new UserLoginRequest("user@example.com", "wrong");
        when(authService.login(request)).thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldActivate() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.active(userId, "token")).thenReturn(AuthResponse.activeSuccessfully(null, userId));

        mockMvc.perform(post("/api/auth/activate")
                        .param("userExternalId", userId.toString()).param("token", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(userId.toString()));

        verify(authService).active(userId, "token");
    }

    @Test
    void shouldRequestPasswordReset() throws Exception {
        when(authService.restartPassword("user@example.com"))
                .thenReturn(AuthResponse.withMessage("Send email for restart password"));

        mockMvc.perform(post("/api/auth/password-reset").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Send email for restart password"));

        verify(authService).restartPassword("user@example.com");
    }

    @Test
    void shouldConfirmPasswordReset() throws Exception {
        ConfirmPasswordRequest request = new ConfirmPasswordRequest("token", "newPassword");

        mockMvc.perform(post("/api/auth/password-reset/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).confirmRestartPassword(request);
    }
}
