package com.timeoff.assignment.leavetype;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timeoff.assignment.leaverequest.LeaveRequestRepository;
import com.timeoff.assignment.leavetype.dto.CreateLeaveTypeRequest;
import com.timeoff.assignment.leavetype.dto.UpdateLeaveTypeStatusRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LeaveTypeControllerIT  extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        leaveRequestRepository.deleteAll();
        leaveTypeRepository.deleteAll();
    }


    @Test
    void create() throws Exception {
        CreateLeaveTypeRequest request =
                new CreateLeaveTypeRequest("Annual Leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Annual Leave"))
                .andExpect(jsonPath("$.code").value("ANNUAL_LEAVE"))
                .andExpect(jsonPath("$.deductsFromAnnualBalance").value(true))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void create_codeAlreadyExists_returnConflictError() throws Exception {
        CreateLeaveTypeRequest request =
                new CreateLeaveTypeRequest("Annual Leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LEAVE_TYPE_ALREADY_EXISTS"));
    }


    @Test
    void create_nullName_returnBadRequestError() throws Exception {
        CreateLeaveTypeRequest request =
                new CreateLeaveTypeRequest(null, LeaveTypeCode.ANNUAL_LEAVE, true);


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void create_nullCode_returnBadRequestError() throws Exception {
        CreateLeaveTypeRequest request =
                new CreateLeaveTypeRequest("Annual leave", null, true);


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations").isArray());
    }


    @Test
    void list_noLeaveTypes_returnEmptyPageResult() throws Exception {
        mockMvc.perform(get("/api/v1/leave-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void list_noParams_returnPaginatedResult() throws Exception {
        CreateLeaveTypeRequest request1 =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        CreateLeaveTypeRequest request2 =
                new CreateLeaveTypeRequest("Sick leave", LeaveTypeCode.SICK_LEAVE, false);

        CreateLeaveTypeRequest request3 =
                new CreateLeaveTypeRequest("Unpaid leave", LeaveTypeCode.UNPAID_LEAVE, true);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request1))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request2))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request3))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/leave-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }


    @Test
    void list_invalidSortParam_returnInternalServerError() throws Exception {
        CreateLeaveTypeRequest request1 =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        CreateLeaveTypeRequest request2 =
                new CreateLeaveTypeRequest("Sick leave", LeaveTypeCode.SICK_LEAVE, false);

        CreateLeaveTypeRequest request3 =
                new CreateLeaveTypeRequest("Unpaid leave", LeaveTypeCode.UNPAID_LEAVE, true);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request1))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request2))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request3))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/leave-types").param("sort","random"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void list_noFilter_returnPaginatedResultFirstPage() throws Exception {

        CreateLeaveTypeRequest request1 =
                new CreateLeaveTypeRequest("Sick leave", LeaveTypeCode.SICK_LEAVE, false);

        CreateLeaveTypeRequest request2 =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);


        CreateLeaveTypeRequest request3 =
                new CreateLeaveTypeRequest("Unpaid leave", LeaveTypeCode.UNPAID_LEAVE, true);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request1))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request2))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request3))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/leave-types")
                        .param("page", "0").param("size", "2").param("sort", "code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void list_noFilter_returnPaginatedResultSecondPage() throws Exception {

        CreateLeaveTypeRequest request1 =
                new CreateLeaveTypeRequest("Sick leave", LeaveTypeCode.SICK_LEAVE, false);

        CreateLeaveTypeRequest request2 =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);


        CreateLeaveTypeRequest request3 =
                new CreateLeaveTypeRequest("Unpaid leave", LeaveTypeCode.UNPAID_LEAVE, true);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request1))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request2))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request3))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/leave-types")
                        .param("page", "1").param("size", "2").param("sort", "code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void list_noFilter_returnSortedResult() throws Exception {

        CreateLeaveTypeRequest request1 =
                new CreateLeaveTypeRequest("Sick leave", LeaveTypeCode.SICK_LEAVE, false);

        CreateLeaveTypeRequest request2 =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);


        CreateLeaveTypeRequest request3 =
                new CreateLeaveTypeRequest("Unpaid leave", LeaveTypeCode.UNPAID_LEAVE, true);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request1))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request2))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request3))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/leave-types")
                        .param("page", "0").param("size", "3").param("sort", "code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].code").value("ANNUAL_LEAVE"))
                .andExpect(jsonPath("$.content[1].code").value("SICK_LEAVE"))
                .andExpect(jsonPath("$.content[2].code").value("UNPAID_LEAVE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void list_noFilter_returnSortedResultDesc() throws Exception {

        CreateLeaveTypeRequest request1 =
                new CreateLeaveTypeRequest("Sick leave", LeaveTypeCode.SICK_LEAVE, false);

        CreateLeaveTypeRequest request2 =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);


        CreateLeaveTypeRequest request3 =
                new CreateLeaveTypeRequest("Unpaid leave", LeaveTypeCode.UNPAID_LEAVE, true);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request1))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request2))))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request3))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/leave-types")
                        .param("page", "0").param("size", "3").param("sort", "code,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].code").value("UNPAID_LEAVE"))
                .andExpect(jsonPath("$.content[1].code").value("SICK_LEAVE"))
                .andExpect(jsonPath("$.content[2].code").value("ANNUAL_LEAVE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }


    @Test
    void list_activeFilterFalse_returnPaginatedResult() throws Exception {
        CreateLeaveTypeRequest request1 =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        CreateLeaveTypeRequest request2 =
                new CreateLeaveTypeRequest("Sick leave", LeaveTypeCode.SICK_LEAVE, false);

        CreateLeaveTypeRequest request3 =
                new CreateLeaveTypeRequest("Unpaid leave", LeaveTypeCode.UNPAID_LEAVE, true);


        UpdateLeaveTypeStatusRequest updateRequest2StatusBody =
                new UpdateLeaveTypeStatusRequest(false);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request1))))
                .andExpect(status().isCreated());


        String request2ResponseBody = mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request2))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request3))))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/leave-types/" + objectMapper.readTree(request2ResponseBody).get("id") + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((updateRequest2StatusBody))))
                .andExpect(status().isOk());


        mockMvc.perform(get("/api/v1/leave-types")
                        .param("page", "0").param("size", "2").param("sort", "code").param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].code").value("SICK_LEAVE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void list_activeFilterTrue_returnPaginatedResult() throws Exception {
        CreateLeaveTypeRequest request1 =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        CreateLeaveTypeRequest request2 =
                new CreateLeaveTypeRequest("Sick leave", LeaveTypeCode.SICK_LEAVE, false);

        CreateLeaveTypeRequest request3 =
                new CreateLeaveTypeRequest("Unpaid leave", LeaveTypeCode.UNPAID_LEAVE, true);


        UpdateLeaveTypeStatusRequest updateRequest2StatusBody =
                new UpdateLeaveTypeStatusRequest(false);

        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request1))))
                .andExpect(status().isCreated());


        String request2ResponseBody = mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request2))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();


        mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request3))))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/leave-types/" + objectMapper.readTree(request2ResponseBody).get("id") + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((updateRequest2StatusBody))))
                .andExpect(status().isOk());


        mockMvc.perform(get("/api/v1/leave-types")
                        .param("page", "0").param("size", "2").param("sort", "code").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].code").value("ANNUAL_LEAVE"))
                .andExpect(jsonPath("$.content[1].code").value("UNPAID_LEAVE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void list_activeFilterInvalidValue_returnValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/leave-types").param("active", "random"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void get_leaveTypeExists_returnTheLeaveType() throws Exception {
        CreateLeaveTypeRequest request =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        String responseBody = mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/v1/leave-types/" + objectMapper.readTree(responseBody).get("id")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Annual leave"))
                .andExpect(jsonPath("$.code").value("ANNUAL_LEAVE"))
                .andExpect(jsonPath("$.deductsFromAnnualBalance").value(true))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void get_leaveTypeDoesNotExist_returnNotFoundError() throws Exception {

        mockMvc.perform(get("/api/v1/leave-types/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("LEAVE_TYPE_NOT_FOUND"));
    }

    @Test
    void updateStatus_invalidPayload_throwValidationError() throws Exception {

        CreateLeaveTypeRequest request =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        String responseBody = mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UpdateLeaveTypeStatusRequest updateStatusBody =
                new UpdateLeaveTypeStatusRequest(null);

        mockMvc.perform(patch("/api/v1/leave-types/" + objectMapper.readTree(responseBody).get("id") + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString((updateStatusBody))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void updateStatus_leaveTypeDoesNotExist_throwNotFoundError() throws Exception {

        UpdateLeaveTypeStatusRequest updateStatusBody =
                new UpdateLeaveTypeStatusRequest(false);

        mockMvc.perform(patch("/api/v1/leave-types/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((updateStatusBody))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("LEAVE_TYPE_NOT_FOUND"));
    }

    @Test
    void updateStatus() throws Exception {

        CreateLeaveTypeRequest request =
                new CreateLeaveTypeRequest("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        String responseBody = mockMvc.perform(post("/api/v1/leave-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((request))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UpdateLeaveTypeStatusRequest updateStatusBody =
                new UpdateLeaveTypeStatusRequest(false);

        mockMvc.perform(patch("/api/v1/leave-types/" + objectMapper.readTree(responseBody).get("id") + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString((updateStatusBody))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Annual leave"))
                .andExpect(jsonPath("$.code").value("ANNUAL_LEAVE"))
                .andExpect(jsonPath("$.deductsFromAnnualBalance").value(true))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }


}
