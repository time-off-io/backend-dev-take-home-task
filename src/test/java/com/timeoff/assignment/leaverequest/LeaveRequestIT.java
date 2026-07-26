package com.timeoff.assignment.leaverequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timeoff.assignment.employee.EmployeeRepository;
import com.timeoff.assignment.employee.dto.CreateEmployeeRequest;
import com.timeoff.assignment.employee.dto.UpdateEmployeeStatusRequest;
import com.timeoff.assignment.leaverequest.dto.CreateLeaveRequest;
import com.timeoff.assignment.leaverequest.dto.RejectLeaveRequest;
import com.timeoff.assignment.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LeaveRequestIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // A Monday comfortably in the future, so the "not in the past" rule never breaks over time.
    private final LocalDate weekStart =
            LocalDate.now().plusWeeks(6).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

    @BeforeEach
    void cleanDatabase() {
        leaveRequestRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    void createsLeaveRequestAndComputesWorkingDays() throws Exception {
        Long employeeId = createEmployee("worker@example.com", 25);

        createLeaveRequest(employeeId, weekStart, weekStart.plusDays(4))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requestedDays").value(5))
                .andExpect(jsonPath("$.employeeId").value(employeeId));
    }

    @Test
    void rejectsCreationForInactiveEmployee() throws Exception {
        Long employeeId = createEmployee("inactive@example.com", 25);
        mockMvc.perform(patch("/api/v1/employees/{id}/status", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeStatusRequest(false))))
                .andExpect(status().isOk());

        createLeaveRequest(employeeId, weekStart, weekStart.plusDays(4))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMPLOYEE_INACTIVE"));
    }

    @Test
    void approvalFlowDeductsAndPersistsBalance() throws Exception {
        Long employeeId = createEmployee("approve@example.com", 25);
        Long requestId = createLeaveRequestId(employeeId, weekStart, weekStart.plusDays(4));

        mockMvc.perform(post("/api/v1/leave-requests/{id}/approve", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewComment\":\"Approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewedAt").exists());

        // Balance persisted: 25 - 5 = 20
        mockMvc.perform(get("/api/v1/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingLeaveDays").value(20));
    }

    @Test
    void rejectionFlowKeepsBalance() throws Exception {
        Long employeeId = createEmployee("reject@example.com", 25);
        Long requestId = createLeaveRequestId(employeeId, weekStart, weekStart.plusDays(4));

        mockMvc.perform(post("/api/v1/leave-requests/{id}/reject", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RejectLeaveRequest("No coverage"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(get("/api/v1/employees/{id}", employeeId))
                .andExpect(jsonPath("$.remainingLeaveDays").value(25));
    }

    @Test
    void rejectionRequiresComment() throws Exception {
        Long employeeId = createEmployee("nocomment@example.com", 25);
        Long requestId = createLeaveRequestId(employeeId, weekStart, weekStart.plusDays(4));

        mockMvc.perform(post("/api/v1/leave-requests/{id}/reject", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void cancellationFlow() throws Exception {
        Long employeeId = createEmployee("cancel@example.com", 25);
        Long requestId = createLeaveRequestId(employeeId, weekStart, weekStart.plusDays(4));

        mockMvc.perform(post("/api/v1/leave-requests/{id}/cancel", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cannotCancelApprovedRequest() throws Exception {
        Long employeeId = createEmployee("noca@example.com", 25);
        Long requestId = createLeaveRequestId(employeeId, weekStart, weekStart.plusDays(4));
        mockMvc.perform(post("/api/v1/leave-requests/{id}/approve", requestId)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/leave-requests/{id}/cancel", requestId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_LEAVE_REQUEST_STATUS"));
    }

    @Test
    void rejectsOverlappingRequest() throws Exception {
        Long employeeId = createEmployee("overlap@example.com", 25);
        createLeaveRequestId(employeeId, weekStart, weekStart.plusDays(4));

        // Overlaps the existing PENDING request (shares the same week).
        createLeaveRequest(employeeId, weekStart.plusDays(2), weekStart.plusDays(6))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LEAVE_REQUEST_OVERLAP"));
    }

    @Test
    void rejectsRequestExceedingBalance() throws Exception {
        Long employeeId = createEmployee("small@example.com", 3);

        createLeaveRequest(employeeId, weekStart, weekStart.plusDays(4)) // 5 working days > 3
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_LEAVE_BALANCE"));
    }

    @Test
    void rejectsMissingEmployeeIdWithValidationError() throws Exception {
        String payload = objectMapper.writeValueAsString(
                new CreateLeaveRequest(null, weekStart, weekStart.plusDays(4), "Vacation", 1L));

        mockMvc.perform(post("/api/v1/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void filtersByStatusAndEmployee() throws Exception {
        Long first = createEmployee("f1@example.com", 25);
        Long second = createEmployee("f2@example.com", 25);
        Long toReject = createLeaveRequestId(first, weekStart, weekStart.plusDays(4));
        createLeaveRequestId(second, weekStart, weekStart.plusDays(4));

        mockMvc.perform(post("/api/v1/leave-requests/{id}/reject", toReject)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RejectLeaveRequest("nope"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/leave-requests").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].employeeId").value(second));

        mockMvc.perform(get("/api/v1/leave-requests").param("employeeId", String.valueOf(first)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("REJECTED"));
    }

    // --- helpers ---

    private Long createEmployee(String email, int allowance) throws Exception {
        String body = mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEmployeeRequest("Test", "User", email, allowance))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions createLeaveRequest(
            Long employeeId, LocalDate start, LocalDate end) throws Exception {
        String payload = objectMapper.writeValueAsString(
                new CreateLeaveRequest(employeeId, start, end, "Vacation", 1L));
        return mockMvc.perform(post("/api/v1/leave-requests")
                .contentType(MediaType.APPLICATION_JSON).content(payload));
    }

    private Long createLeaveRequestId(Long employeeId, LocalDate start, LocalDate end) throws Exception {
        String body = createLeaveRequest(employeeId, start, end)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}
