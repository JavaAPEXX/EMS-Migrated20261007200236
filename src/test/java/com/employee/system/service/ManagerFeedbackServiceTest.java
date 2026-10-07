package com.employee.system.service;

import com.employee.system.dto.ManagerFeedbackDTO;
import com.employee.system.entity.Employee;
import com.employee.system.entity.ManagerFeedback;
import com.employee.system.repository.EmployeeRepository;
import com.employee.system.repository.ManagerFeedbackRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerFeedbackServiceTest {

    @Mock
    private ManagerFeedbackRepository feedbackRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ManagerFeedbackService service;

    /* Helper methods to create test data */
    private Employee createEmployee(Long id, String firstName, String lastName) {
        Employee e = new Employee();
        e.setId(id);
        e.setFirstName(firstName);
        e.setLastName(lastName);
        return e;
    }

    private ManagerFeedback createFeedback(Long id, Employee employee, Employee manager,
                                           String type, String category, String text,
                                           Integer rating, String actionItems,
                                           LocalDate date, String status) {
        ManagerFeedback f = new ManagerFeedback();
        f.setId(id);
        f.setEmployee(employee);
        f.setProvidedBy(manager);
        f.setFeedbackType(type);
        f.setFeedbackCategory(category);
        f.setFeedbackText(text);
        f.setRating(rating);
        f.setActionItems(actionItems);
        f.setFeedbackDate(date);
        f.setStatus(status);
        f.setCreatedAt(LocalDateTime.now());
        f.setUpdatedAt(LocalDateTime.now());
        return f;
    }

    private ManagerFeedbackDTO createDTO(Long employeeId, Long providedById,
                                         String type, String category, String text,
                                         Integer rating, String actionItems,
                                         LocalDate date, String status) {
        ManagerFeedbackDTO dto = new ManagerFeedbackDTO();
        dto.setEmployeeId(employeeId);
        dto.setProvidedById(providedById);
        dto.setFeedbackType(type);
        dto.setFeedbackCategory(category);
        dto.setFeedbackText(text);
        dto.setRating(rating);
        dto.setActionItems(actionItems);
        dto.setFeedbackDate(date);
        dto.setStatus(status);
        return dto;
    }

    /* ---------- provideFeedback ---------- */

    @Test
    @DisplayName("givenValidFeedbackDTO_whenProvideFeedback_thenReturnSavedFeedbackDTO")
    void givenValidFeedbackDTO_whenProvideFeedback_thenReturnSavedFeedbackDTO() {
        // Arrange
        Employee employee = createEmployee(1L, "John", "Doe");
        Employee manager = createEmployee(2L, "Jane", "Smith");
        ManagerFeedbackDTO dto = createDTO(1L, 2L, "Positive", "Teamwork",
                "Great collaboration", 5, "Continue", LocalDate.of(2023, 5, 10), "FINAL");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));

        ManagerFeedback saved = createFeedback(100L, employee, manager,
                "Positive", "Teamwork", "Great collaboration", 5,
                "Continue", LocalDate.of(2023, 5, 10), "FINAL");

        when(feedbackRepository.save(any(ManagerFeedback.class))).thenReturn(saved);

        // Act
        ManagerFeedbackDTO result = service.provideFeedback(dto);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals(2L, result.getProvidedById());
        assertEquals("Jane Smith", result.getProvidedByName());
        assertEquals("Positive", result.getFeedbackType());
        assertEquals("Teamwork", result.getFeedbackCategory());
        assertEquals("Great collaboration", result.getFeedbackText());
        assertEquals(5, result.getRating());
        assertEquals("Continue", result.getActionItems());
        assertEquals(LocalDate.of(2023, 5, 10), result.getFeedbackDate());
        assertEquals("FINAL", result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).findById(2L);
        verify(feedbackRepository, times(1)).save(any(ManagerFeedback.class));
    }

    @Test
    @DisplayName("givenEmployeeNotFound_whenProvideFeedback_thenThrowRuntimeException")
    void givenEmployeeNotFound_whenProvideFeedback_thenThrowRuntimeException() {
        // Arrange
        ManagerFeedbackDTO dto = createDTO(99L, 2L, "Positive", "Teamwork",
                "Great collaboration", 5, "Continue", LocalDate.now(), "FINAL");

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.provideFeedback(dto));
        assertTrue(ex.getMessage().contains("Employee not found with id: 99"));

        verify(employeeRepository, times(1)).findById(99L);
        verify(employeeRepository, never()).findById(2L);
        verify(feedbackRepository, never()).save(any(ManagerFeedback.class));
    }

    @Test
    @DisplayName("givenManagerNotFound_whenProvideFeedback_thenThrowRuntimeException")
    void givenManagerNotFound_whenProvideFeedback_thenThrowRuntimeException() {
        // Arrange
        Employee employee = createEmployee(1L, "John", "Doe");
        ManagerFeedbackDTO dto = createDTO(1L, 99L, "Positive", "Teamwork",
                "Great collaboration", 5, "Continue", LocalDate.now(), "FINAL");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.provideFeedback(dto));
        assertTrue(ex.getMessage().contains("Manager not found with id: 99"));

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).findById(99L);
        verify(feedbackRepository, never()).save(any(ManagerFeedback.class));
    }

    @Test
    @DisplayName("givenNullFeedbackDTO_whenProvideFeedback_thenThrowNullPointerException")
    void givenNullFeedbackDTO_whenProvideFeedback_thenThrowNullPointerException() {
        // Arrange & Act & Assert
        assertThrows(NullPointerException.class, () -> service.provideFeedback(null));
    }

    @Test
    @DisplayName("givenFeedbackDTOWithNullDateAndStatus_whenProvideFeedback_thenSetDefaults")
    void givenFeedbackDTOWithNullDateAndStatus_whenProvideFeedback_thenSetDefaults() {
        // Arrange
        Employee employee = createEmployee(1L, "John", "Doe");
        Employee manager = createEmployee(2L, "Jane", "Smith");
        ManagerFeedbackDTO dto = createDTO(1L, 2L, "Positive", "Teamwork",
                "Great collaboration", 5, "Continue", null, null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));

        ArgumentCaptor<ManagerFeedback> captor = ArgumentCaptor.forClass(ManagerFeedback.class);
        when(feedbackRepository.save(any(ManagerFeedback.class))).thenAnswer(invocation -> {
            ManagerFeedback f = invocation.getArgument(0);
            f.setId(200L);
            return f;
        });

        // Act
        LocalDate before = LocalDate.now();
        ManagerFeedbackDTO result = service.provideFeedback(dto);
        LocalDate after = LocalDate.now();

        // Assert
        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertEquals("DRAFT", result.getStatus());
        assertTrue(result.getFeedbackDate().equals(before) || result.getFeedbackDate().equals(after));

        verify(feedbackRepository).save(captor.capture());
        ManagerFeedback saved = captor.getValue();
        assertEquals("DRAFT", saved.getStatus());
        assertTrue(saved.getFeedbackDate().equals(before) || saved.getFeedbackDate().equals(after));
    }

    /* ---------- getFeedbackById ---------- */

    @Test
    @DisplayName("givenValidId_whenGetFeedbackById_thenReturnFeedbackDTO")
    void givenValidId_whenGetFeedbackById_thenReturnFeedbackDTO() {
        // Arrange
        Employee employee = createEmployee(1L, "John", "Doe");
        Employee manager = createEmployee(2L, "Jane", "Smith");
        ManagerFeedback feedback = createFeedback(300L, employee, manager,
                "Positive", "Teamwork", "Great collaboration", 5,
                "Continue", LocalDate.of(2023, 5, 10), "FINAL");

        when(feedbackRepository.findById(300L)).thenReturn(Optional.of(feedback));

        // Act
        ManagerFeedbackDTO result = service.getFeedbackById(300L);

        // Assert
        assertNotNull(result);
        assertEquals(300L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals(2L, result.getProvidedById());
        assertEquals("Jane Smith", result.getProvidedByName());
        assertEquals("Positive", result.getFeedbackType());
        assertEquals("Teamwork", result.getFeedbackCategory());
        assertEquals("Great collaboration", result.getFeedbackText());
        assertEquals(5, result.getRating());
        assertEquals("Continue", result.getActionItems());
        assertEquals(LocalDate.of(2023, 5, 10), result.getFeedbackDate());
        assertEquals("FINAL", result.getStatus());

        verify(feedbackRepository, times(1)).findById(300L);
    }

    @Test
    @DisplayName("givenNonExistingId_whenGetFeedbackById_thenThrowRuntimeException")
    void givenNonExistingId_whenGetFeedbackById_thenThrowRuntimeException() {
        // Arrange
        when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getFeedbackById(999L));
        assertTrue(ex.getMessage().contains("Manager feedback not found with id: 999"));

        verify(feedbackRepository, times(1)).findById(999L);
    }

    /* ---------- getFeedbackByEmployee ---------- */

    @Test
    @DisplayName("givenValidEmployeeId_whenGetFeedbackByEmployee_thenReturnList")
    void givenValidEmployeeId_whenGetFeedbackByEmployee_thenReturnList() {
        // Arrange
        Employee employee = createEmployee(1L, "John", "Doe");
        Employee manager = createEmployee(2L, "Jane", "Smith");
        ManagerFeedback f1 = createFeedback(400L, employee, manager,
                "Positive", "Teamwork", "Great collaboration", 5,
                "Continue", LocalDate.of(2023, 5, 10), "FINAL");
        ManagerFeedback f2 = createFeedback(401L, employee, manager,
                "Negative", "Communication", "Needs improvement", 2,
                "Review", LocalDate.of(2023, 6, 12), "DRAFT");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(feedbackRepository.findByEmployee(employee)).thenReturn(List.of(f1, f2));

        // Act
        List<ManagerFeedbackDTO> result = service.getFeedbackByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        ManagerFeedbackDTO dto1 = result.get(0);
        assertEquals(400L, dto1.getId());
        assertEquals("John Doe", dto1.getEmployeeName());
        assertEquals("Jane Smith", dto1.getProvidedByName());

        ManagerFeedbackDTO dto2 = result.get(1);
        assertEquals(401L, dto2.getId());
        assertEquals("John Doe", dto2.getEmployeeName());
        assertEquals("Jane Smith", dto2.getProvidedByName());

        verify(employeeRepository, times(1)).findById(1L);
        verify(feedbackRepository, times(1)).findByEmployee(employee);
    }

    @Test
    @DisplayName("givenEmployeeNotFound_whenGetFeedbackByEmployee_thenThrowRuntimeException")
    void givenEmployeeNotFound_whenGetFeedbackByEmployee_thenThrowRuntimeException() {
        //