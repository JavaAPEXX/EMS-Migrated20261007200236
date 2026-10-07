package com.employee.system.service;

import com.employee.system.dto.PerformanceGoalDTO;
import com.employee.system.entity.Employee;
import com.employee.system.entity.PerformanceGoal;
import com.employee.system.repository.EmployeeRepository;
import com.employee.system.repository.PerformanceGoalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceGoalServiceTest {

    @Mock
    private PerformanceGoalRepository goalRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private PerformanceGoalService performanceGoalService;

    // -------------------------------------------------------------------------
    // createGoal
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Create goal with valid DTO should return saved DTO")
    void givenValidGoalDTO_whenCreateGoal_thenReturnSavedGoalDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");

        PerformanceGoalDTO inputDto = new PerformanceGoalDTO();
        inputDto.setEmployeeId(1L);
        inputDto.setGoalTitle("Increase Sales");
        inputDto.setGoalDescription("Boost Q4 sales by 15%");
        inputDto.setCategory("SALES");
        inputDto.setStartDate(LocalDate.now().minusDays(10));
        inputDto.setTargetDate(LocalDate.now().plusDays(20));
        inputDto.setStatus("ACTIVE");
        inputDto.setPriority("HIGH");
        inputDto.setProgressPercentage(30);
        inputDto.setKeyResults("Result A");
        inputDto.setRemarks("Initial remark");

        PerformanceGoal savedGoal = new PerformanceGoal();
        savedGoal.setId(100L);
        savedGoal.setEmployee(employee);
        savedGoal.setGoalTitle(inputDto.getGoalTitle());
        savedGoal.setGoalDescription(inputDto.getGoalDescription());
        savedGoal.setCategory(inputDto.getCategory());
        savedGoal.setStartDate(inputDto.getStartDate());
        savedGoal.setTargetDate(inputDto.getTargetDate());
        savedGoal.setStatus(inputDto.getStatus());
        savedGoal.setPriority(inputDto.getPriority());
        savedGoal.setProgressPercentage(inputDto.getProgressPercentage());
        savedGoal.setKeyResults(inputDto.getKeyResults());
        savedGoal.setRemarks(inputDto.getRemarks());

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(goalRepository.save(any(PerformanceGoal.class))).thenReturn(savedGoal);

        // Act
        PerformanceGoalDTO resultDto = performanceGoalService.createGoal(inputDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(100L, resultDto.getId());
        assertEquals("John Doe", resultDto.getEmployeeName());
        assertEquals("Increase Sales", resultDto.getGoalTitle());
        verify(employeeRepository, times(1)).findById(1L);
        ArgumentCaptor<PerformanceGoal> captor = ArgumentCaptor.forClass(PerformanceGoal.class);
        verify(goalRepository, times(1)).save(captor.capture());
        assertEquals("ACTIVE", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Create goal with non‑existing employee should throw RuntimeException")
    void givenNonExistingEmployee_whenCreateGoal_thenThrowRuntimeException() {
        // Arrange
        PerformanceGoalDTO inputDto = new PerformanceGoalDTO();
        inputDto.setEmployeeId(999L);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceGoalService.createGoal(inputDto));
        assertTrue(ex.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById(999L);
        verifyNoInteractions(goalRepository);
    }

    @Test
    @DisplayName("Create goal with null status and progress should apply defaults")
    void givenGoalDTOWithNullStatusAndProgress_whenCreateGoal_thenDefaultsApplied() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(2L);
        employee.setFirstName("Alice");
        employee.setLastName("Smith");

        PerformanceGoalDTO inputDto = new PerformanceGoalDTO();
        inputDto.setEmployeeId(2L);
        inputDto.setGoalTitle("Improve Quality");
        inputDto.setGoalDescription("Reduce defects");
        inputDto.setCategory("QUALITY");
        inputDto.setStartDate(LocalDate.now());
        inputDto.setTargetDate(LocalDate.now().plusMonths(1));
        // status and progress left null intentionally

        PerformanceGoal savedGoal = new PerformanceGoal();
        savedGoal.setId(200L);
        savedGoal.setEmployee(employee);
        savedGoal.setGoalTitle(inputDto.getGoalTitle());
        savedGoal.setGoalDescription(inputDto.getGoalDescription());
        savedGoal.setCategory(inputDto.getCategory());
        savedGoal.setStartDate(inputDto.getStartDate());
        savedGoal.setTargetDate(inputDto.getTargetDate());
        savedGoal.setStatus("DRAFT"); // default applied by service
        savedGoal.setProgressPercentage(0); // default applied by service

        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(goalRepository.save(any(PerformanceGoal.class))).thenReturn(savedGoal);

        // Act
        PerformanceGoalDTO resultDto = performanceGoalService.createGoal(inputDto);

        // Assert
        assertEquals("DRAFT", resultDto.getStatus());
        assertEquals(0, resultDto.getProgressPercentage());
    }

    @Test
    @DisplayName("Create goal with null DTO should throw NullPointerException")
    void givenNullGoalDTO_whenCreateGoal_thenThrowNullPointerException() {
        // Arrange
        // No stubbing needed

        // Act & Assert
        assertThrows(NullPointerException.class, () -> performanceGoalService.createGoal(null));
    }

    // -------------------------------------------------------------------------
    // getGoalById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Get existing goal by id should return DTO")
    void givenExistingGoalId_whenGetGoalById_thenReturnGoalDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(3L);
        employee.setFirstName("Bob");
        employee.setLastName("Brown");

        PerformanceGoal goal = new PerformanceGoal();
        goal.setId(300L);
        goal.setEmployee(employee);
        goal.setGoalTitle("Launch Product");
        goal.setGoalDescription("Release v2.0");
        goal.setCategory("PRODUCT");
        goal.setStartDate(LocalDate.now().minusDays(5));
        goal.setTargetDate(LocalDate.now().plusDays(30));
        goal.setStatus("ACTIVE");
        goal.setPriority("MEDIUM");
        goal.setProgressPercentage(10);
        goal.setKeyResults("KR1");
        goal.setRemarks("On track");

        when(goalRepository.findById(300L)).thenReturn(Optional.of(goal));

        // Act
        PerformanceGoalDTO resultDto = performanceGoalService.getGoalById(300L);

        // Assert
        assertNotNull(resultDto);
        assertEquals(300L, resultDto.getId());
        assertEquals("Bob Brown", resultDto.getEmployeeName());
        verify(goalRepository, times(1)).findById(300L);
    }

    @Test
    @DisplayName("Get non‑existing goal by id should throw RuntimeException")
    void givenNonExistingGoalId_whenGetGoalById_thenThrowRuntimeException() {
        // Arrange
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceGoalService.getGoalById(999L));
        assertTrue(ex.getMessage().contains("Performance goal not found"));
        verify(goalRepository, times(1)).findById(999L);
    }

    // -------------------------------------------------------------------------
    // getGoalsByEmployee
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Get goals for existing employee should return list")
    void givenExistingEmployee_whenGetGoalsByEmployee_thenReturnGoalList() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(4L);
        employee.setFirstName("Carol");
        employee.setLastName("White");

        PerformanceGoal goal1 = new PerformanceGoal();
        goal1.setId(401L);
        goal1.setEmployee(employee);
        goal1.setGoalTitle("Goal 1");

        PerformanceGoal goal2 = new PerformanceGoal();
        goal2.setId(402L);
        goal2.setEmployee(employee);
        goal2.setGoalTitle("Goal 2");

        when(employeeRepository.findById(4L)).thenReturn(Optional.of(employee));
        when(goalRepository.findByEmployee(employee)).thenReturn(List.of(goal1, goal2));

        // Act
        List<PerformanceGoalDTO> result = performanceGoalService.getGoalsByEmployee(4L);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(401L)));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(402L)));
        verify(employeeRepository, times(1)).findById(4L);
        verify(goalRepository, times(1)).findByEmployee(employee);
    }

    @Test
    @DisplayName("Get goals for non‑existing employee should throw RuntimeException")
    void givenNonExistingEmployee_whenGetGoalsByEmployee_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceGoalService.getGoalsByEmployee(999L));
        assertTrue(ex.getMessage().contains("Employee not found"));
        verify(employeeRepository, times(1)).findById(999L);
        verifyNoInteractions(goalRepository);
    }

    // -------------------------------------------------------------------------
    // getActiveGoals
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Get active goals should return list filtered by ACTIVE status")
    void givenActiveStatus_whenGetActiveGoals_thenReturnGoalList() {
        // Arrange
        PerformanceGoal activeGoal = new PerformanceGoal();
        activeGoal.setId(501L);
        Employee emp = new Employee();
        emp.setId(5L);
        emp.setFirstName("Dave");
        emp.setLastName("Green");
        activeGoal.setEmployee(emp);
        activeGoal.setStatus("ACTIVE");

        when(goalRepository.findByStatus("ACTIVE")).thenReturn(List.of(activeGoal));

        // Act
        List<PerformanceGoalDTO> result = performanceGoalService.getActiveGoals();

        // Assert
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
        verify(goalRepository, times(1)).findByStatus("ACTIVE");
    }

    // -------------------------------------------------------------------------
    // getGoalsByStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Get goals by arbitrary status should return matching list")
    void givenSpecificStatus_whenGetGoalsByStatus_thenReturnGoalList() {
        // Arrange
        PerformanceGoal draftGoal = new PerformanceGoal();
        draftGoal.setId(601L);
        Employee emp = new Employee();
        emp.setId(6L);
        emp.setFirstName("Eve");
        emp.setLastName("Black");
        draftGoal.setEmployee(emp);
        draftGoal.setStatus("DRAFT");

        when(goalRepository.findByStatus("DRAFT")).thenReturn(List.of(draftGoal));

        // Act
        List<PerformanceGoalDTO> result = performanceGoalService.getGoalsByStatus("DRAFT");

        // Assert
        assertEquals(1, result.size());
        assertEquals("DRAFT", result.get(0).getStatus());
        verify(goalRepository, times(1)).findByStatus("DRAFT");
    }

    // -------------------------------------------------------------------------
    // updateGoal
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Update existing goal should persist changes and return updated DTO")
    void givenExistingGoalId_whenUpdateGoal_thenReturnUpdatedDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(7L);
        employee.setFirstName("Frank");
        employee.setLastName("Yellow");

        PerformanceGoal existingGoal = new PerformanceGoal();
        existingGoal.setId(701L);
        existingGoal.setEmployee(employee);
        existingGoal.setGoalTitle("Old Title");

        PerformanceGoalDTO updateDto = new PerformanceGoalDTO();
        updateDto.setGoalTitle("New Title");
        updateDto.setGoalDescription("Updated description");
        updateDto.setCategory("NEW_CAT");
        updateDto.setTargetDate(LocalDate.now().plusDays(10));
        updateDto.setStatus("COMPLETED");
        updateDto.setPriority("LOW");
        updateDto.setProgressPercentage(100);
        updateDto.setKeyResults("KR Updated");
        updateDto.setRemarks("All done");

        PerformanceGoal savedGoal = new PerformanceGoal();
        savedGoal.setId(701L);
        savedGoal.setEmployee(employee);
        savedGoal.setGoalTitle(updateDto.getGoalTitle());
        savedGoal.setGoalDescription(updateDto.getGoalDescription());
        savedGoal.setCategory(updateDto.getCategory());
        savedGoal.setTargetDate(updateDto.getTargetDate());
        savedGoal.setStatus(updateDto.getStatus());
        savedGoal.setPriority(updateDto.getPriority());
        savedGoal.setProgressPercentage(updateDto.getProgressPercentage());
        savedGoal.setKeyResults(updateDto.getKeyResults());
        savedGoal.setRemarks(updateDto.getRemarks());

        when(goalRepository.findById(701L)).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(PerformanceGoal.class))).thenReturn(savedGoal);

        // Act
        PerformanceGoalDTO resultDto = performanceGoalService.updateGoal(701L, updateDto);

        // Assert
        assertEquals("New Title", resultDto.getGoalTitle());
        assertEquals("COMPLETED", resultDto.getStatus());
        verify(goalRepository, times(1)).findById(701L);
        verify(goalRepository, times(1)).save(existingGoal);
    }

    @Test
    @DisplayName("Update non‑existing goal should throw RuntimeException")
    void givenNonExistingGoalId_whenUpdateGoal_thenThrowRuntimeException() {
        // Arrange
        PerformanceGoalDTO updateDto = new PerformanceGoalDTO();
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceGoalService.updateGoal(999L, updateDto));
        assertTrue(ex.getMessage().contains("Performance goal not found"));
        verify(goalRepository, times(1)).findById(999L);
        verify(goalRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // updateGoalProgress
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Update goal progress for existing goal should persist new percentage")
    void givenExistingGoalId_whenUpdateGoalProgress_thenReturnUpdatedDTO() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(8L);
        employee.setFirstName("Grace");
        employee.setLastName("Blue");

        PerformanceGoal goal = new PerformanceGoal();
        goal.setId(801L);
        goal.setEmployee(employee);
        goal.setProgressPercentage(20);

        PerformanceGoal updatedGoal = new PerformanceGoal();
        updatedGoal.setId(801L);
        updatedGoal.setEmployee(employee);
        updatedGoal.setProgressPercentage(55);

        when(goalRepository.findById(801L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(PerformanceGoal.class))).thenReturn(updatedGoal);

        // Act
        PerformanceGoalDTO resultDto = performanceGoalService.updateGoalProgress(801L, 55);

        // Assert
        assertEquals(55, resultDto.getProgressPercentage());
        verify(goalRepository, times(1)).findById(801L);
        verify(goalRepository, times(1)).save(goal);
    }

    @Test
    @DisplayName("Update goal progress for non‑existing goal should throw RuntimeException")
    void givenNonExistingGoalId_whenUpdateGoalProgress_thenThrowRuntimeException() {
        // Arrange
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceGoalService.updateGoalProgress(999L, 10));
        assertTrue(ex.getMessage().contains("Performance goal not found"));
        verify(goalRepository, times(1)).findById(999L);
        verify(goalRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deleteGoal
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Delete existing goal should invoke repository delete")
    void givenExistingGoalId_whenDeleteGoal_thenGoalDeleted() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(9L);
        employee.setFirstName("Helen");
        employee.setLastName("Red");

        PerformanceGoal goal = new PerformanceGoal();
        goal.setId(901L);
        goal.setEmployee(employee);

        when(goalRepository.findById(901L)).thenReturn(Optional.of(goal));

        // Act
        performanceGoalService.deleteGoal(901L);

        // Assert
        verify(goalRepository, times(1)).findById(901L);
        verify(goalRepository, times(1)).delete(goal);
    }

    @Test
    @DisplayName("Delete non‑existing goal should throw RuntimeException")
    void givenNonExistingGoalId_whenDeleteGoal_thenThrowRuntimeException() {
        // Arrange
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> performanceGoalService.deleteGoal(999L));
        assertTrue(ex.getMessage().contains("Performance goal not found"));
        verify(goalRepository, times(1)).findById(999L);
        verify(goalRepository, never()).delete(any());
    }
}