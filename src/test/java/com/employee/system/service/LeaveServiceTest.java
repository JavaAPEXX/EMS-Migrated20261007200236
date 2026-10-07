package com.employee.system.service;

import com.employee.system.dto.LeaveBalanceDTO;
import com.employee.system.dto.LeaveDTO;
import com.employee.system.entity.Employee;
import com.employee.system.entity.Holiday;
import com.employee.system.entity.Leave;
import com.employee.system.repository.EmployeeRepository;
import com.employee.system.repository.HolidayRepository;
import com.employee.system.repository.LeaveRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private LeaveService leaveService;

    /* ---------- applyLeave ---------- */

    @Test
    @DisplayName("Given valid LeaveDTO when applyLeave then leave is saved and returned")
    void givenValidLeaveDTO_whenApplyLeave_thenLeaveSavedAndReturned() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");

        LeaveDTO request = new LeaveDTO();
        request.setEmployeeId(employee.getId());
        request.setStartDate(LocalDate.of(2023, 1, 2));
        request.setEndDate(LocalDate.of(2023, 1, 4));
        request.setLeaveType("CASUAL");
        request.setReason("Family event");
        request.setStatus(null); // let service set default

        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(leaveRepository.hasOverlappingLeave(employee, request.getStartDate(), request.getEndDate()))
                .thenReturn(false);
        when(holidayRepository.findBetween(request.getStartDate(), request.getEndDate()))
                .thenReturn(Collections.emptyList());
        when(leaveRepository.countTakenDaysByTypeAndYear(employee, "CASUAL", 2023)).thenReturn(0L);

        Leave savedLeave = new Leave();
        savedLeave.setId(100L);
        savedLeave.setEmployee(employee);
        savedLeave.setStartDate(request.getStartDate());
        savedLeave.setEndDate(request.getEndDate());
        savedLeave.setLeaveType(request.getLeaveType());
        savedLeave.setReason(request.getReason());
        savedLeave.setDays(3); // 3 working days, no holidays
        savedLeave.setStatus("PENDING");
        when(leaveRepository.save(any(Leave.class))).thenReturn(savedLeave);

        // Act
        LeaveDTO result = leaveService.applyLeave(request);

        // Assert
        assertNotNull(result);
        assertEquals(savedLeave.getId(), result.getId());
        assertEquals(employee.getId(), result.getEmployeeId());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals(request.getStartDate(), result.getStartDate());
        assertEquals(request.getEndDate(), result.getEndDate());
        assertEquals("CASUAL", result.getLeaveType());
        assertEquals("PENDING", result.getStatus());
        assertEquals(3, result.getDays());

        ArgumentCaptor<Leave> captor = ArgumentCaptor.forClass(Leave.class);
        verify(leaveRepository, times(1)).save(captor.capture());
        Leave captured = captor.getValue();
        assertEquals(employee, captured.getEmployee());
        assertEquals("CASUAL", captured.getLeaveType());
        assertEquals(3, captured.getDays());
    }

    @Test
    @DisplayName("Given LeaveDTO with null start date when applyLeave then throws RuntimeException")
    void givenLeaveDTOWithNullStartDate_whenApplyLeave_thenThrowRuntimeException() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(2L);
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        LeaveDTO request = new LeaveDTO();
        request.setEmployeeId(employee.getId());
        request.setStartDate(null);
        request.setEndDate(LocalDate.now());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(request));
        assertEquals("Invalid leave date range", ex.getMessage());
    }

    @Test
    @DisplayName("Given LeaveDTO with end date before start date when applyLeave then throws RuntimeException")
    void givenLeaveDTOWithEndDateBeforeStart_whenApplyLeave_thenThrowRuntimeException() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(3L);
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        LeaveDTO request = new LeaveDTO();
        request.setEmployeeId(employee.getId());
        request.setStartDate(LocalDate.of(2023, 5, 10));
        request.setEndDate(LocalDate.of(2023, 5, 5));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(request));
        assertEquals("Invalid leave date range", ex.getMessage());
    }

    @Test
    @DisplayName("Given overlapping leave when applyLeave then throws RuntimeException")
    void givenLeaveDTOWithOverlappingLeave_whenApplyLeave_thenThrowRuntimeException() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(4L);
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        LeaveDTO request = new LeaveDTO();
        request.setEmployeeId(employee.getId());
        request.setStartDate(LocalDate.of(2023, 6, 1));
        request.setEndDate(LocalDate.of(2023, 6, 3));
        request.setLeaveType("SICK");

        when(leaveRepository.hasOverlappingLeave(employee, request.getStartDate(), request.getEndDate()))
                .thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(request));
        assertEquals("Leave conflict detected for the requested period", ex.getMessage());
    }

    @Test
    @DisplayName("Given leave request exceeding entitlement when applyLeave then throws RuntimeException")
    void givenLeaveDTOExceedingEntitlement_whenApplyLeave_thenThrowRuntimeException() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(5L);
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        LeaveDTO request = new LeaveDTO();
        request.setEmployeeId(employee.getId());
        request.setStartDate(LocalDate.of(2023, 7, 1));
        request.setEndDate(LocalDate.of(2023, 7, 5)); // 5 days
        request.setLeaveType("SICK");

        when(leaveRepository.hasOverlappingLeave(employee, request.getStartDate(), request.getEndDate()))
                .thenReturn(false);
        when(holidayRepository.findBetween(request.getStartDate(), request.getEndDate()))
                .thenReturn(Collections.emptyList());
        // Already taken 8 days, entitlement is 10
        when(leaveRepository.countTakenDaysByTypeAndYear(employee, "SICK", 2023)).thenReturn(8L);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(request));
        assertTrue(ex.getMessage().contains("Insufficient leave balance"));
    }

    @Test
    @DisplayName("Given non‑existing employee when applyLeave then throws RuntimeException")
    void givenNonExistingEmployee_whenApplyLeave_thenThrowRuntimeException() {
        // Arrange
        LeaveDTO request = new LeaveDTO();
        request.setEmployeeId(999L);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setLeaveType("EARNED");

        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.applyLeave(request));
        assertEquals("Employee not found with id: 999", ex.getMessage());
    }

    @Test
    @DisplayName("Given leave request with holidays when applyLeave then working days exclude holidays")
    void givenLeaveDTOWithHolidays_whenApplyLeave_thenWorkingDaysReduced() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(6L);
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        LeaveDTO request = new LeaveDTO();
        request.setEmployeeId(employee.getId());
        request.setStartDate(LocalDate.of(2023, 12, 20));
        request.setEndDate(LocalDate.of(2023, 12, 24)); // 5 calendar days
        request.setLeaveType("EARNED");

        when(leaveRepository.hasOverlappingLeave(employee, request.getStartDate(), request.getEndDate()))
                .thenReturn(false);
        // Assume two holidays within the range
        Holiday h1 = new Holiday(); Holiday h2 = new Holiday();
        when(holidayRepository.findBetween(request.getStartDate(), request.getEndDate()))
                .thenReturn(Arrays.asList(h1, h2));
        when(leaveRepository.countTakenDaysByTypeAndYear(employee, "EARNED", 2023)).thenReturn(0L);

        Leave savedLeave = new Leave();
        savedLeave.setId(200L);
        savedLeave.setEmployee(employee);
        savedLeave.setStartDate(request.getStartDate());
        savedLeave.setEndDate(request.getEndDate());
        savedLeave.setLeaveType("EARNED");
        savedLeave.setReason("Travel");
        savedLeave.setDays(3); // 5 - 2 holidays
        savedLeave.setStatus("PENDING");
        when(leaveRepository.save(any(Leave.class))).thenReturn(savedLeave);

        // Act
        LeaveDTO result = leaveService.applyLeave(request);

        // Assert
        assertEquals(3, result.getDays());
        verify(holidayRepository, times(1))
                .findBetween(request.getStartDate(), request.getEndDate());
    }

    /* ---------- approveLeave ---------- */

    @Test
    @DisplayName("Given existing leave id and approve true when approveLeave then status becomes APPROVED")
    void givenValidLeaveId_whenApproveLeave_thenStatusUpdatedToApproved() {
        // Arrange
        Long leaveId = 10L;
        Leave existing = new Leave();
        existing.setId(leaveId);
        existing.setStatus("PENDING");

        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(existing));
        when(leaveRepository.save(existing)).thenReturn(existing);

        // Act
        LeaveDTO result = leaveService.approveLeave(leaveId, true);

        // Assert
        assertEquals("APPROVED", result.getStatus());
        verify(leaveRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Given existing leave id and approve false when approveLeave then status becomes REJECTED")
    void givenValidLeaveId_whenApproveLeave_thenStatusUpdatedToRejected() {
        // Arrange
        Long leaveId = 11L;
        Leave existing = new Leave();
        existing.setId(leaveId);
        existing.setStatus("PENDING");

        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(existing));
        when(leaveRepository.save(existing)).thenReturn(existing);

        // Act
        LeaveDTO result = leaveService.approveLeave(leaveId, false);

        // Assert
        assertEquals("REJECTED", result.getStatus());
        verify(leaveRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Given non‑existing leave id when approveLeave then throws RuntimeException")
    void givenNonExistingLeaveId_whenApproveLeave_thenThrowRuntimeException() {
        // Arrange
        Long leaveId = 999L;
        when(leaveRepository.findById(leaveId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.approveLeave(leaveId, true));
        assertEquals("Leave not found with id: 999", ex.getMessage());
    }

    /* ---------- getLeaveById ---------- */

    @Test
    @DisplayName("Given existing leave id when getLeaveById then returns corresponding DTO")
    void givenValidLeaveId_whenGetLeaveById_thenReturnDTO() {
        // Arrange
        Long leaveId = 20L;
        Employee emp = new Employee();
        emp.setId(2L);
        emp.setFirstName("Alice");
        emp.setLastName("Smith");

        Leave leave = new Leave();
        leave.setId(leaveId);
        leave.setEmployee(emp);
        leave.setStartDate(LocalDate.of(2023, 3, 1));
        leave.setEndDate(LocalDate.of(2023, 3, 2));
        leave.setLeaveType("SICK");
        leave.setStatus("PENDING");
        leave.setDays(2);

        when(leaveRepository.findById(leaveId)).thenReturn(Optional.of(leave));

        // Act
        LeaveDTO result = leaveService.getLeaveById(leaveId);

        // Assert
        assertEquals(leaveId, result.getId());
        assertEquals(emp.getId(), result.getEmployeeId());
        assertEquals("Alice Smith", result.getEmployeeName());
        assertEquals("SICK", result.getLeaveType());
        assertEquals(2, result.getDays());
    }

    @Test
    @DisplayName("Given non‑existing leave id when getLeaveById then throws RuntimeException")
    void givenNonExistingLeaveId_whenGetLeaveById_thenThrowRuntimeException() {
        // Arrange
        Long leaveId = 888L;
        when(leaveRepository.findById(leaveId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.getLeaveById(leaveId));
        assertEquals("Leave not found with id: 888", ex.getMessage());
    }

    /* ---------- getLeavesByEmployee ---------- */

    @Test
    @DisplayName("Given existing employee id when getLeavesByEmployee then returns list of DTOs")
    void givenValidEmployeeId_whenGetLeavesByEmployee_thenReturnList() {
        // Arrange
        Long empId = 30L;
        Employee emp = new Employee();
        emp.setId(empId);
        emp.setFirstName("Bob");
        emp.setLastName("Brown");

        Leave l1 = new Leave();
        l1.setId(1L);
        l1.setEmployee(emp);
        l1.setLeaveType("CASUAL");
        l1.setStatus("APPROVED");
        l1.setDays(1);

        Leave l2 = new Leave();
        l2.setId(2L);
        l2.setEmployee(emp);
        l2.setLeaveType("SICK");
        l2.setStatus("PENDING");
        l2.setDays(2);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));
        when(leaveRepository.findByEmployee(emp)).thenReturn(Arrays.asList(l1, l2));

        // Act
        List<LeaveDTO> result = leaveService.getLeavesByEmployee(empId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(1L) && dto.getLeaveType().equals("CASUAL")));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(2L) && dto.getLeaveType().equals("SICK")));
    }

    @Test
    @DisplayName("Given non‑existing employee id when getLeavesByEmployee then throws RuntimeException")
    void givenNonExistingEmployeeId_whenGetLeavesByEmployee_thenThrowRuntimeException() {
        // Arrange
        Long empId = 777L;
        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.getLeavesByEmployee(empId));
        assertEquals("Employee not found with id: 777", ex.getMessage());
    }

    /* ---------- getLeavesByStatus ---------- */

    @Test
    @DisplayName("Given status when getLeavesByStatus then returns matching leaves")
    void givenStatus_whenGetLeavesByStatus_thenReturnList() {
        // Arrange
        String status = "APPROVED";
        Leave l = new Leave();
        l.setId(5L);
        l.setStatus(status);
        l.setLeaveType("EARNED");
        l.setDays(4);
        when(leaveRepository.findByStatus(status)).thenReturn(Collections.singletonList(l));

        // Act
        List<LeaveDTO> result = leaveService.getLeavesByStatus(status);

        // Assert
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());
        assertEquals(5L, result.get(0).getId());
    }

    /* ---------- getLeaveBalance ---------- */

    @Test
    @DisplayName("Given employee id and year when getLeaveBalance then returns correct balances")
    void givenEmployeeIdAndYear_whenGetLeaveBalance_thenReturnCorrectBalances() {
        // Arrange
        Long empId = 40L;
        int year = 2023;
        Employee emp = new Employee();
        emp.setId(empId);
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));

        // Mock taken days for each entitlement type
        when(leaveRepository.countTakenDaysByTypeAndYear(emp, "SICK", year)).thenReturn(3L);
        when(leaveRepository.countTakenDaysByTypeAndYear(emp, "CASUAL", year)).thenReturn(0L);
        when(leaveRepository.countTakenDaysByTypeAndYear(emp, "EARNED", year)).thenReturn(10L);

        // Act
        LeaveBalanceDTO result = leaveService.getLeaveBalance(empId, year);

        // Assert
        assertNotNull(result);
        assertEquals(empId, result.getEmployeeId());
        Map<String, Integer> balances = result.getBalances();
        assertEquals(7, balances.get("SICK"));      // 10 - 3
        assertEquals(7, balances.get("CASUAL"));   // 7 - 0
        assertEquals(5, balances.get("EARNED"));   // 15 - 10
    }

    @Test
    @DisplayName("Given employee with no taken leaves when getLeaveBalance then returns full entitlements")
    void givenEmployeeId_whenGetLeaveBalanceAndNoLeaves_thenReturnFullEntitlements() {
        // Arrange
        Long empId = 41L;
        int year = 2024;
        Employee emp = new Employee();
        emp.setId(empId);
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));

        when(leaveRepository.countTakenDaysByTypeAndYear(emp, "SICK", year)).thenReturn(0L);
        when(leaveRepository.countTakenDaysByTypeAndYear(emp, "CASUAL", year)).thenReturn(0L);
        when(leaveRepository.countTakenDaysByTypeAndYear(emp, "EARNED", year)).thenReturn(0L);

        // Act
        LeaveBalanceDTO result = leaveService.getLeaveBalance(empId, year);

        // Assert
        assertEquals(10, result.getBalances().get("SICK"));
        assertEquals(7, result.getBalances().get("CASUAL"));
        assertEquals(15, result.getBalances().get("EARNED"));
    }

    @Test
    @DisplayName("Given non‑existing employee id when getLeaveBalance then throws RuntimeException")
    void givenNonExistingEmployeeId_whenGetLeaveBalance_thenThrowRuntimeException() {
        // Arrange
        Long empId = 999L;
        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.getLeaveBalance(empId, 2023));
        assertEquals("Employee not found with id: 999", ex.getMessage());
    }
}