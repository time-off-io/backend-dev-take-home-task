package com.timeoff.assignment.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timeoff.assignment.employee.dto.CreateEmployeeRequest;
import com.timeoff.assignment.employee.dto.UpdateEmployeeStatusRequest;
import com.timeoff.assignment.leaverequest.LeaveRequestRepository;
import com.timeoff.assignment.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @BeforeEach
    void cleanDatabase() {
        leaveRequestRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void createsEmployee() throws Exception {
        CreateEmployeeRequest request =
                new CreateEmployeeRequest("George", "Papadopoulos", "george@example.com", 25);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("george@example.com"))
                .andExpect(jsonPath("$.remainingLeaveDays").value(25))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        CreateEmployeeRequest request =
                new CreateEmployeeRequest("George", "Papadopoulos", "george@example.com", 25);
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMPLOYEE_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void rejectsInvalidPayloadWithValidationError() throws Exception {
        CreateEmployeeRequest request =
                new CreateEmployeeRequest("", "Doe", "not-an-email", -5);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void returnsNotFoundForUnknownEmployee() throws Exception {
        mockMvc.perform(get("/api/v1/employees/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EMPLOYEE_NOT_FOUND"));
    }

    @Test
    void updatesEmployeeStatus() throws Exception {
        Long id = createEmployee("switch@example.com");

        mockMvc.perform(patch("/api/v1/employees/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UpdateEmployeeStatusRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void listsEmployeesWithPagination() throws Exception {
        for (int i = 0; i < 3; i++) {
            createEmployee("emp" + i + "@example.com");
        }

        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0").param("size", "2").param("sort", "email,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));
    }

    private Long createEmployee(String email) throws Exception {
        String body = mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CreateEmployeeRequest("Test", "User", email, 25))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}
