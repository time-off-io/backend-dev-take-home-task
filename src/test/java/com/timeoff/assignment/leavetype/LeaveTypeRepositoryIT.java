package com.timeoff.assignment.leavetype;

import com.timeoff.assignment.leaverequest.LeaveRequestRepository;
import com.timeoff.assignment.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LeaveTypeRepositoryIT  extends AbstractIntegrationTest {

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        leaveRequestRepository.deleteAll();
        leaveTypeRepository.deleteAll();
        leaveTypeRepository.flush();
    }

    @Test
    void save_validLeaveType_persistEntity() {
        LeaveType leaveType = new LeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        LeaveType saved =
                leaveTypeRepository.saveAndFlush(leaveType);

        entityManager.clear();

        Optional<LeaveType> actualLeaveType =
                leaveTypeRepository.findById(saved.getId());

        assertThat(actualLeaveType).isPresent();

        assertThat(actualLeaveType.get().getId()).isNotNull();
        assertThat(actualLeaveType.get().getId()).isPositive();
        assertThat(actualLeaveType.get().getName()).isEqualTo("Annual Leave");
        assertThat(actualLeaveType.get().getCode()).isEqualTo(LeaveTypeCode.ANNUAL_LEAVE);
        assertTrue(actualLeaveType.get().isDeductsFromAnnualBalance());
        assertTrue(actualLeaveType.get().isActive());
    }

    @Test
    void save_nullName_throwsDataIntegrityViolationException() {
        LeaveType leaveType = new LeaveType(
                null,
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        assertThrows(DataIntegrityViolationException.class, () -> {
            leaveTypeRepository.saveAndFlush(leaveType);
        });
    }

    @Test
    void save_nullCode_throwsDataIntegrityViolationException() {
        LeaveType leaveType = new LeaveType(
                "Annual Leave",
                null,
                true
        );

        assertThrows(DataIntegrityViolationException.class, () -> {
            leaveTypeRepository.saveAndFlush(leaveType);
        });
    }

    @Test
    void save_aLeaveTypeWithTheSameCodeAlreadyExists_throwDataIntegrityViolationException() {
        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        LeaveType duplicate = new LeaveType(
                "Another Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                false
        );

        assertThrows(DataIntegrityViolationException.class, () -> {
            leaveTypeRepository.saveAndFlush(duplicate);
        });
    }

    @Test
    void findById_leaveTypeExists_returnLeaveType() {
        LeaveType saved = saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        entityManager.clear();

        Optional<LeaveType> result = leaveTypeRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo(LeaveTypeCode.ANNUAL_LEAVE);
    }

    @Test
    void findById_leaveTypeDoesNotExist_returnEmptyOptional() {
        Optional<LeaveType> result = leaveTypeRepository.findById(999999L);
        assertThat(result).isEmpty();
    }

    @Test
    void existsLeaveTypeByCode_leaveTypeExists_returnTrue() {
        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        boolean exists =
                leaveTypeRepository.existsLeaveTypeByCode(
                        LeaveTypeCode.ANNUAL_LEAVE
                );

        assertThat(exists).isTrue();
    }

    @Test
    void existsLeaveTypeByCode_leaveTypeDoesNotExist_returnFalse() {
        boolean exists =
                leaveTypeRepository.existsLeaveTypeByCode(
                        LeaveTypeCode.ANNUAL_LEAVE
                );

        assertThat(exists).isFalse();
    }

    @Test
    void deactivateLeaveType_managedEntity_persistChange() {
        LeaveType saved = saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        assertThat(saved.isActive()).isTrue();

        saved.deactivate();

        entityManager.flush();
        entityManager.clear();

        LeaveType updated = leaveTypeRepository
                .findById(saved.getId())
                .orElseThrow();

        assertThat(updated.isActive()).isFalse();
    }

    @Test
    void activateLeaveType_managedEntity_persistChange() {
        LeaveType leaveType = new LeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        leaveType.deactivate();

        LeaveType saved =
                leaveTypeRepository.saveAndFlush(leaveType);

        assertThat(saved.isActive()).isFalse();

        saved.activate();

        entityManager.flush();
        entityManager.clear();

        LeaveType updated = leaveTypeRepository
                .findById(saved.getId())
                .orElseThrow();

        assertThat(updated.isActive()).isTrue();
    }

    @Test
    void findAll_activeSpecificationIsTrue_returnOnlyActiveLeaveTypes() {
        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        LeaveType inactive = new LeaveType(
                "Sick Leave",
                LeaveTypeCode.SICK_LEAVE,
                false
        );

        inactive.deactivate();

        leaveTypeRepository.saveAndFlush(inactive);

        Specification<LeaveType> specification =   Specification.allOf(
                LeaveTypeSpecifications.isActive(true)
        );

        List<LeaveType> result =
                leaveTypeRepository.findAll(specification);

        assertThat(result).hasSize(1);
        assertThat(result.stream().map(LeaveType::getCode).toList()).containsExactly(LeaveTypeCode.ANNUAL_LEAVE);
        assertTrue(result.stream().allMatch(LeaveType::isActive));

    }

    @Test
    void findAll_activeSpecificationIsFalse_returnOnlyInactiveLeaveTypes() {
        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        LeaveType inactive = new LeaveType(
                "Sick Leave",
                LeaveTypeCode.SICK_LEAVE,
                false
        );

        inactive.deactivate();

        leaveTypeRepository.saveAndFlush(inactive);

        Specification<LeaveType> specification =  Specification.allOf(
                LeaveTypeSpecifications.isActive(false)
        );

        List<LeaveType> result =
                leaveTypeRepository.findAll(specification);

        assertThat(result).hasSize(1);
        assertThat(result.stream().map(LeaveType::getCode).toList()).containsExactly(LeaveTypeCode.SICK_LEAVE);
        assertTrue(result.stream().noneMatch(LeaveType::isActive));

    }

    @Test
    void findAll_activeSpecIsNull_returnAllInactiveLeaveTypes() {
        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        LeaveType inactive = new LeaveType(
                "Sick Leave",
                LeaveTypeCode.SICK_LEAVE,
                false
        );

        inactive.deactivate();

        leaveTypeRepository.saveAndFlush(inactive);

        Specification<LeaveType> specification =  Specification.allOf(LeaveTypeSpecifications.isActive(null));

        List<LeaveType> result =
                leaveTypeRepository.findAll(specification);

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(LeaveType::getCode).toList()).containsExactlyInAnyOrder(LeaveTypeCode.SICK_LEAVE, LeaveTypeCode.ANNUAL_LEAVE);

    }

    @Test
    void findAll_withPaginationAndSortingAndRequestForFirstPageAndNoSpec_returnSortedResultsAndPagination() {
        saveLeaveType(
                "Sick Leave",
                LeaveTypeCode.SICK_LEAVE,
                false
        );

        saveLeaveType(
                "Unpaid Leave",
                LeaveTypeCode.UNPAID_LEAVE,
                true
        );

        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );
        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by("code").ascending()
        );

        Page<LeaveType> result =
                leaveTypeRepository.findAll(
                        Specification.allOf(LeaveTypeSpecifications.isActive(null)),
                        pageable
                );

        assertThat(result.getContent().stream().map(LeaveType::getCode).toList()).containsExactly(LeaveTypeCode.ANNUAL_LEAVE, LeaveTypeCode.SICK_LEAVE);

        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
        assertTrue(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    @Test
    void findAll_withPaginationAndSortingAndRequestForSecondPageAndNoSpec_returnSortedResultsAndPagination() {
        saveLeaveType(
                "Sick Leave",
                LeaveTypeCode.SICK_LEAVE,
                false
        );

        saveLeaveType(
                "Unpaid Leave",
                LeaveTypeCode.UNPAID_LEAVE,
                true
        );

        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );
        Pageable pageable = PageRequest.of(
                1,
                2,
                Sort.by("code").ascending()
        );


        Page<LeaveType> result =
                leaveTypeRepository.findAll(
                        Specification.allOf(LeaveTypeSpecifications.isActive(null)),
                        pageable
                );

        assertThat(result.getContent().stream().map(LeaveType::getCode).toList()).containsExactly(LeaveTypeCode.UNPAID_LEAVE);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertFalse(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.hasNext());
        assertTrue(result.hasPrevious());
    }

    @Test
    void findAll_withPaginationAndSortingDescAndNoSpec_returnSortedResultsAndPagination() {
        saveLeaveType(
                "Sick Leave",
                LeaveTypeCode.SICK_LEAVE,
                false
        );

        saveLeaveType(
                "Unpaid Leave",
                LeaveTypeCode.UNPAID_LEAVE,
                true
        );

        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );
        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("code").descending()
        );


        Page<LeaveType> result =
                leaveTypeRepository.findAll(
                        Specification.allOf(LeaveTypeSpecifications.isActive(null)),
                        pageable
                );

        assertThat(result.getContent().stream().map(LeaveType::getCode).toList()).containsExactly(LeaveTypeCode.UNPAID_LEAVE,
                LeaveTypeCode.SICK_LEAVE,
                LeaveTypeCode.ANNUAL_LEAVE);
    }

    @Test
    void findAll_withPaginationAndSortingAndActiveSpec_returnSortedAndFilteredResultsAndPagination() {
        LeaveType sickLeaveType = saveLeaveType(
                "Sick Leave",
                LeaveTypeCode.SICK_LEAVE,
                false
        );

        saveLeaveType(
                "Unpaid Leave",
                LeaveTypeCode.UNPAID_LEAVE,
                true
        );

        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        sickLeaveType.deactivate();

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("code").ascending()
        );


        Page<LeaveType> result =
                leaveTypeRepository.findAll(
                        Specification.allOf(LeaveTypeSpecifications.isActive(true)),
                        pageable
                );

        assertThat(result.getContent().stream().map(LeaveType::getCode).toList()).containsExactly(LeaveTypeCode.ANNUAL_LEAVE, LeaveTypeCode.UNPAID_LEAVE);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertTrue(result.isLast());
    }

    @Test
    void findAll_noLeaveTypesExist_returnEmptyList() {
        List<LeaveType> result =
                leaveTypeRepository.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findAll() {
        saveLeaveType(
                "Annual Leave",
                LeaveTypeCode.ANNUAL_LEAVE,
                true
        );

        saveLeaveType(
                "Sick Leave",
                LeaveTypeCode.SICK_LEAVE,
                false
        );

        List<LeaveType> result = leaveTypeRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(LeaveType::getCode).toList()).containsExactlyInAnyOrder(LeaveTypeCode.SICK_LEAVE, LeaveTypeCode.ANNUAL_LEAVE);

    }


    private LeaveType saveLeaveType(
            String name,
            LeaveTypeCode code,
            boolean deductsFromAnnualBalance
    ) {
        LeaveType leaveType = new LeaveType(
                name,
                code,
                deductsFromAnnualBalance
        );

        return leaveTypeRepository.saveAndFlush(leaveType);
    }
}
