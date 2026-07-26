package com.timeoff.assignment.leavetype;

import com.timeoff.assignment.leavetype.dto.CreateLeaveTypeRequest;
import com.timeoff.assignment.leavetype.dto.LeaveTypeResponse;
import com.timeoff.assignment.leavetype.dto.UpdateLeaveTypeStatusRequest;
import com.timeoff.assignment.leavetype.exception.LeaveTypeAlreadyExistsException;
import com.timeoff.assignment.leavetype.exception.LeaveTypeNotFoundException;
import com.timeoff.assignment.leavetype.mapper.LeaveTypeMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveTypeMapper leaveTypeMapper;

    public LeaveTypeService(LeaveTypeRepository leaveTypeRepository, LeaveTypeMapper leaveTypeMapper) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveTypeMapper = leaveTypeMapper;
    }

    @Transactional
    public LeaveTypeResponse create(CreateLeaveTypeRequest request) {

        if (leaveTypeRepository.existsLeaveTypeByCode(request.code())) {
            throw new LeaveTypeAlreadyExistsException(request.code());
        }

        LeaveType leaveType =
                new LeaveType(request.name(), request.code(), request.deductsFromAnnualBalance());

        return leaveTypeMapper.toResponse(leaveTypeRepository.save(leaveType));
    }

    public Page<LeaveTypeResponse> list(LeaveTypeFilter filter, Pageable pageable) {
        Specification<LeaveType> spec = Specification
                .allOf(LeaveTypeSpecifications.isActive(filter.active()));
        return leaveTypeRepository.findAll(spec, pageable).map(leaveTypeMapper::toResponse);
    }

    public LeaveTypeResponse get(Long id) {
        return leaveTypeMapper.toResponse(getEntity(id));
    }


    @Transactional
    public LeaveTypeResponse updateStatus(Long leaveTypeId, UpdateLeaveTypeStatusRequest request) {
        LeaveType leaveType = getEntity(leaveTypeId);
        if (request.active()) {
            leaveType.activate();
        } else {
            leaveType.deactivate();
        }
        return leaveTypeMapper.toResponse(leaveType);
    }


    /**
     * Loads a leave type entity or throws a not found error. 
     */
    public LeaveType getEntity(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> new LeaveTypeNotFoundException(id));
    }

}
