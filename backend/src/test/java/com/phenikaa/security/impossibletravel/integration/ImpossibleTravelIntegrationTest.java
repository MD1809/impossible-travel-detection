package com.phenikaa.security.impossibletravel.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Kiểm thử Tích hợp Toàn diện (End-to-End Integration Tests)")
class ImpossibleTravelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USER = "e2e_user_" + System.currentTimeMillis();
    private static final String TEST_PASS = "Password@123";
    private String capturedSessionToken;

    @Test
    @Order(1)
    @DisplayName("E2E-1: Kiểm tra trạng thái hoạt động của hệ thống (Health Check)")
    void testSystemHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("impossible-travel-detection")));
    }

    @Test
    @Order(2)
    @DisplayName("E2E-2: Đăng ký tài khoản người dùng thử nghiệm mới")
    void testRegisterUser() throws Exception {
        String registerJson = String.format("""
                {
                    "username": "%s",
                    "password": "%s",
                    "fullName": "Sinh Vien E2E"
                }
                """, TEST_USER, TEST_PASS);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.username", is(TEST_USER)));
    }

    @Test
    @Order(3)
    @DisplayName("E2E-3: Đăng nhập hợp lệ lần đầu tại Việt Nam (113.160.224.1) -> 200 OK")
    void testFirstLoginHanoiAllowed() throws Exception {
        String loginJson = String.format("""
                {
                    "username": "%s",
                    "password": "%s",
                    "ipAddress": "113.160.224.1"
                }
                """, TEST_USER, TEST_PASS);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.sessionToken", notNullValue()))
                .andExpect(jsonPath("$.location.country", is("Vietnam")))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        capturedSessionToken = jsonNode.get("sessionToken").asText();
        assertNotNull(capturedSessionToken);
    }

    @Test
    @Order(4)
    @DisplayName("E2E-4: Kịch bản 1 - Đăng nhập đồng thời tức thời từ New York (161.185.160.93) -> 403 Forbidden")
    void testConcurrentLoginFromNewYorkBlocked() throws Exception {
        String loginJson = String.format("""
                {
                    "username": "%s",
                    "password": "%s",
                    "ipAddress": "161.185.160.93"
                }
                """, TEST_USER, TEST_PASS);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.detectionDetails.ruleTriggered", is("CONCURRENT_IMPOSSIBLE_TRAVEL")))
                .andExpect(jsonPath("$.detectionDetails.distanceKm", greaterThan(13000.0)))
                .andExpect(jsonPath("$.detectionDetails.action", is("BLOCKED")));
    }

    @Test
    @Order(5)
    @DisplayName("E2E-5: Kiểm tra chỉ số thống kê Dashboard ghi nhận đúng các lần đăng nhập")
    void testDashboardStatsReflected() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLogins", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.allowedLogins", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.blockedLogins", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.securityAlertsCount", greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(6)
    @DisplayName("E2E-6: Lấy danh sách sự kiện an ninh xác nhận bản ghi cảnh báo vừa phát sinh")
    void testSecurityEventsList() throws Exception {
        mockMvc.perform(get("/api/security-events?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].eventType", is("CONCURRENT_IMPOSSIBLE_TRAVEL")))
                .andExpect(jsonPath("$.content[0].actionTaken", is("BLOCKED")));
    }

    @Test
    @Order(7)
    @DisplayName("E2E-7: Kiểm tra xử lý trường hợp sai mật khẩu -> 401 Unauthorized")
    void testLoginWithWrongPassword() throws Exception {
        String wrongPassJson = String.format("""
                {
                    "username": "%s",
                    "password": "WrongPassword123",
                    "ipAddress": "113.160.224.1"
                }
                """, TEST_USER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPassJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @Order(8)
    @DisplayName("E2E-8: Đăng xuất phiên làm việc hợp lệ đã cấp")
    void testLogoutSession() throws Exception {
        assertNotNull(capturedSessionToken, "Cần có sessionToken từ bước E2E-3");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + capturedSessionToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("thành công")));
    }
}
