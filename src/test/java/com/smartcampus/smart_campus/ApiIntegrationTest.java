package com.smartcampus.smart_campus;
import org.springframework.test.context.ActiveProfiles;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {


    private static final String ADMIN_EMAIL = "admin-it@test.com";
    private static final String ADMIN_PASSWORD = "Admin@12345";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminManagesStudentsAndStudentSeesOnlyOwnArea() throws Exception {
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(get("/api/admin/analytics/overview").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").isNumber());

        mockMvc.perform(post("/api/admin/students")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Riya Sharma","email":"riya-it@test.com","password":"123456",
                                 "rollNo":"IT2023001","course":"B.Tech CSE","semester":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rollNo").value("IT2023001"));

        mockMvc.perform(get("/api/admin/students")
                        .param("search", "riya")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        String studentToken = login("riya-it@test.com", "123456");

        mockMvc.perform(get("/api/student/risk").header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("INSUFFICIENT_DATA"));

        mockMvc.perform(get("/api/admin/students").header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationRulesAreEnforced() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Sneaky","email":"sneaky-it@test.com","password":"123456","role":"ADMIN"}
                                """))
                .andExpect(status().isBadRequest());

        String student = """
                {"fullName":"Reg Student","email":"reg-it@test.com","password":"123456","role":"STUDENT"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(student))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(student))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"reg-it@test.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chatReportsAMissingAiKeyInsteadOfCrashing() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"fullName":"Chat Student","email":"chat-it@test.com","password":"123456","role":"STUDENT"}
                        """));
        String token = login("chat-it@test.com", "123456");

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}