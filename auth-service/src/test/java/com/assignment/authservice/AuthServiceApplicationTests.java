package com.assignment.authservice;

import com.assignment.authservice.dto.LoginRequest;
import com.assignment.authservice.dto.RegisterRequest;
import com.assignment.authservice.entity.Role;
import com.assignment.authservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AuthServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        // First registration
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    void testRegisterValidationFailures() throws Exception {
        // Invalid email format
        RegisterRequest invalidEmail = RegisterRequest.builder()
                .name("Alice")
                .email("invalid-email")
                .password("password123")
                .role(Role.USER)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.details.email", is("Invalid email format")));

        // Short password
        RegisterRequest shortPassword = RegisterRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("123")
                .role(Role.USER)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortPassword)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.details.password", is("Password must be at least 6 characters")));
    }

    @Test
    void testLoginSuccess() throws Exception {
        // First register the user
        RegisterRequest regRequest = RegisterRequest.builder()
                .name("Bob")
                .email("bob@example.com")
                .password("securePass")
                .role(Role.THEATRE_OWNER)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("bob@example.com")
                .password("securePass")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("bob@example.com")))
                .andExpect(jsonPath("$.role", is("THEATRE_OWNER")));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    void testSecuredEndpointsWithoutToken() throws Exception {
        mockMvc.perform(get("/auth/test/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", containsString("Authentication token is missing")));
    }

    @Test
    void testRoleBasedAccessControl() throws Exception {
        // Register a USER
        RegisterRequest userReg = RegisterRequest.builder()
                .name("User Account")
                .email("user@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        MvcResult userResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReg)))
                .andExpect(status().isCreated())
                .andReturn();

        String userToken = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("token").asText();

        // Register an ADMIN
        RegisterRequest adminReg = RegisterRequest.builder()
                .name("Admin Account")
                .email("admin@example.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        MvcResult adminResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReg)))
                .andExpect(status().isCreated())
                .andReturn();

        String adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).get("token").asText();

        // 1. Test USER accessing User Endpoint (Allowed)
        mockMvc.perform(get("/auth/test/user")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Accessible by USER")));

        // 2. Test USER accessing Admin Endpoint (Forbidden - 403)
        mockMvc.perform(get("/auth/test/admin")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // 3. Test ADMIN accessing Admin Endpoint (Allowed)
        mockMvc.perform(get("/auth/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Restricted to ADMIN")));
    }
}
