package com.timeoff.assignment.leavetype;

import com.timeoff.assignment.common.exception.BusinessException;
import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.leavetype.dto.CreateLeaveTypeRequest;
import com.timeoff.assignment.leavetype.dto.LeaveTypeResponse;
import com.timeoff.assignment.leavetype.dto.UpdateLeaveTypeStatusRequest;
import com.timeoff.assignment.leavetype.exception.LeaveTypeAlreadyExistsException;
import com.timeoff.assignment.leavetype.exception.LeaveTypeNotFoundException;
import com.timeoff.assignment.leavetype.mapper.LeaveTypeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveTypeServiceTest {

    @InjectMocks
    private LeaveTypeService service;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeaveTypeMapper leaveTypeMapper =
            Mappers.getMapper(LeaveTypeMapper.class);


    @Test
    void create() {

        CreateLeaveTypeRequest request =
                new CreateLeaveTypeRequest("Annual Leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        LeaveTypeResponse expected = new LeaveTypeResponse(1L,
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true,
                true, Instant.now(), Instant.now());

        when(leaveTypeRepository.existsLeaveTypeByCode(LeaveTypeCode.ANNUAL_LEAVE)).thenReturn(false);
        when(leaveTypeRepository.save(any(LeaveType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(leaveTypeMapper.toResponse(any(LeaveType.class))).thenReturn(expected);

        ArgumentCaptor<LeaveType> leaveTypeCaptor =
                ArgumentCaptor.forClass(LeaveType.class);

        LeaveTypeResponse actual = service.create(request);

        assertEquals(expected, actual);
        verify(leaveTypeRepository, times(1)).existsLeaveTypeByCode(LeaveTypeCode.ANNUAL_LEAVE);
        verify(leaveTypeRepository, times(1)).save(leaveTypeCaptor.capture());

        LeaveType savedLeaveType = leaveTypeCaptor.getValue();

        assertEquals("Annual Leave", savedLeaveType.getName());
        assertEquals(
                LeaveTypeCode.ANNUAL_LEAVE,
                savedLeaveType.getCode()
        );
        assertTrue(savedLeaveType.isActive());
        assertTrue(savedLeaveType.isDeductsFromAnnualBalance());
        verify(leaveTypeMapper, times(1)).toResponse(savedLeaveType);
    }

    @Test
    void create_leaveTypeAlreadyExists_throwLeaveTypeAlreadyExistsException() {

        CreateLeaveTypeRequest request =
                new CreateLeaveTypeRequest("Annual Leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        when(leaveTypeRepository.existsLeaveTypeByCode(LeaveTypeCode.ANNUAL_LEAVE)).thenReturn(true);

        BusinessException exception = assertThrows(LeaveTypeAlreadyExistsException.class, () -> {
            service.create(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LEAVE_TYPE_ALREADY_EXISTS);
        verify(leaveTypeRepository, times(1)).existsLeaveTypeByCode(LeaveTypeCode.ANNUAL_LEAVE);
        verifyNoMoreInteractions(leaveTypeRepository);
        verifyNoInteractions(leaveTypeMapper);
    }

    @Test
    void list() {
        Pageable p = PageRequest.of(0, 20, Sort.by("code").ascending());
        LeaveTypeFilter filter = new LeaveTypeFilter(null);

        LeaveType lt1 = new LeaveType("Annual Leave", LeaveTypeCode.ANNUAL_LEAVE, true);
        LeaveType lt2 = new LeaveType("Sick Leave", LeaveTypeCode.SICK_LEAVE, false);
        LeaveType lt3 = new LeaveType("Unpaid Leave", LeaveTypeCode.UNPAID_LEAVE, true);

        LeaveTypeResponse ltr1 = new LeaveTypeResponse(1L,
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true,
                true, Instant.now(), Instant.now());

        LeaveTypeResponse ltr2 = new LeaveTypeResponse(2L,
                "Sick Leave",
                LeaveTypeCode.SICK_LEAVE,
                false,
                true, Instant.now(), Instant.now());

        LeaveTypeResponse ltr3 = new LeaveTypeResponse(3L,
                "Unpaid Leave",
                LeaveTypeCode.UNPAID_LEAVE,
                true,
                true, Instant.now(), Instant.now());

        Page<LeaveType> page = new PageImpl<>(List.of(lt1, lt2, lt3), p, 3);

        when(leaveTypeRepository.findAll(any(Specification.class), eq(p))).thenReturn(page);
        when(leaveTypeMapper.toResponse(lt1)).thenReturn(ltr1);
        when(leaveTypeMapper.toResponse(lt2)).thenReturn(ltr2);
        when(leaveTypeMapper.toResponse(lt3)).thenReturn(ltr3);

        Page<LeaveTypeResponse> response = service.list(filter, p);

        assertThat(response.getContent()).containsExactly(ltr1,ltr2,ltr3);
        assertThat(response.getNumber()).isZero();
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getSort())
                .isEqualTo(Sort.by("code").ascending());
        verify(leaveTypeRepository, times(1)).findAll(any(Specification.class), eq(p));
        verify(leaveTypeMapper).toResponse(lt1);
        verify(leaveTypeMapper).toResponse(lt2);
        verify(leaveTypeMapper).toResponse(lt3);
        verifyNoMoreInteractions(leaveTypeMapper);
    }

    @Test
    void list_noLeaveTypesExist_returnsEmptyPage() {
        Pageable pageable =
                PageRequest.of(0, 20, Sort.by("code").ascending());

        LeaveTypeFilter filter = new LeaveTypeFilter(null);

        Page<LeaveType> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(leaveTypeRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(emptyPage);

        Page<LeaveTypeResponse> result =
                service.list(filter, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();

        verify(leaveTypeRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verifyNoInteractions(leaveTypeMapper);
    }

    @Test
    void get() {
        LeaveType lt = new LeaveType("Annual Leave", LeaveTypeCode.ANNUAL_LEAVE, true);
        LeaveTypeResponse expected = new LeaveTypeResponse(1L,
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true,
                true, Instant.now(), Instant.now());
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(lt));
        when(leaveTypeMapper.toResponse(lt)).thenReturn(expected);

        LeaveTypeResponse actual = service.get(1L);

        assertEquals(expected, actual);
        verify(leaveTypeRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(leaveTypeRepository);
        verify(leaveTypeMapper, times(1)).toResponse(lt);
        verifyNoMoreInteractions(leaveTypeMapper);
    }

    @Test
    void get_leaveTypeNotFound_throwLeaveTypeNotFoundException() {

        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(LeaveTypeNotFoundException.class, () -> {
            service.get(1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LEAVE_TYPE_NOT_FOUND);

        verify(leaveTypeRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(leaveTypeRepository);
        verifyNoInteractions(leaveTypeMapper);
    }

    @Test
    void getEntity() {
        LeaveType expected = new LeaveType("Annual Leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(expected));

        LeaveType actual = service.getEntity(1L);

        assertEquals(expected, actual);
        verify(leaveTypeRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(leaveTypeRepository);
    }

    @Test
    void getEntity_leaveTypeNotFound_throwLeaveTypeNotFoundException(){

        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(LeaveTypeNotFoundException.class, () -> {
            service.getEntity(1L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LEAVE_TYPE_NOT_FOUND);

        verify(leaveTypeRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(leaveTypeRepository);
    }

    @Test
    void updateStatus_leaveTypeIsActiveAndRequestIsToDeactivate_deactivateTheLeaveType() {

        LeaveType leaveType = new LeaveType("Annual Leave", LeaveTypeCode.ANNUAL_LEAVE, true);

        UpdateLeaveTypeStatusRequest request = new UpdateLeaveTypeStatusRequest(false);

        LeaveTypeResponse expected = new LeaveTypeResponse(1L,
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true,
                false, Instant.now(), Instant.now());



        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeMapper.toResponse(leaveType)).thenReturn(expected);

        LeaveTypeResponse actual =
                service.updateStatus(1L, request);

        assertEquals(expected, actual);
        assertFalse(leaveType.isActive());

        verify(leaveTypeRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(leaveTypeRepository);

        verify(leaveTypeMapper, times(1)).toResponse(leaveType);
        verifyNoMoreInteractions(leaveTypeMapper);
    }

    @Test
    void updateStatus_leaveTypeIsInactiveAndRequestIsToActivate_activateTheLeaveType() {

        LeaveType leaveType = new LeaveType("Annual Leave", LeaveTypeCode.ANNUAL_LEAVE, true);
        leaveType.deactivate();

        UpdateLeaveTypeStatusRequest request = new UpdateLeaveTypeStatusRequest(true);

        LeaveTypeResponse expected = new LeaveTypeResponse(1L,
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true,
                true, Instant.now(), Instant.now());


        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeMapper.toResponse(leaveType)).thenReturn(expected);

        LeaveTypeResponse actual =
                service.updateStatus(1L, request);

        assertEquals(expected, actual);
        assertTrue(leaveType.isActive());

        verify(leaveTypeRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(leaveTypeRepository);

        verify(leaveTypeMapper, times(1)).toResponse(leaveType);
        verifyNoMoreInteractions(leaveTypeMapper);
    }

    @Test
    void updateStatus_leaveTypeNotFound_throwLeaveTypeNotFoundException() {

        UpdateLeaveTypeStatusRequest request = new UpdateLeaveTypeStatusRequest(true);

        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(LeaveTypeNotFoundException.class, () -> {
            service.updateStatus(1L, request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LEAVE_TYPE_NOT_FOUND);

        verify(leaveTypeRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(leaveTypeRepository);
        verifyNoInteractions(leaveTypeMapper);

    }
}
