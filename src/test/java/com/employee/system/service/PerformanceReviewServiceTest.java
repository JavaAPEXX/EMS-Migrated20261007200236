package com.employee.system.service;

import com.employee.system.dto.PerformanceReviewDTO;
import com.employee.system.entity.Employee;
import com.employee.system.entity.PerformanceReview;
import com.employee.system.repository.EmployeeRepository;
import com.employee.system.repository.PerformanceReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceReviewServiceTest {

    @Mock
    private PerformanceReviewRepository reviewRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private PerformanceReviewService performanceReviewService;

    private Employee employee;
    private Employee manager;
    private PerformanceReview review;
    private PerformanceReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");

        manager = new Employee();
        manager.setId(2L);
        manager.setFirstName("Jane");
        manager.setLastName("Smith");

        review = new PerformanceReview();
        review.setId(100L);
        review.setEmployee(employee);
        review.setReviewedBy(manager);
        review.setReviewPeriod("2023-Q1");
        review.setReviewDate(LocalDate.of(2023, 3, 31));
        review.setOverallRating(4.5);
        review.setTechnicalSkillsRating(4.0);
        review.setBehavioralRating(4.5);
        review.setLeadershipRating(4.0);
        review.setTeamworkRating(4.5);
        review.setStrengths("Strong technical skills");
        review.setAreasForImprovement("Communication");
        review.setComments("Good performance");
        review.setStatus("COMPLETED");
        review.setCreatedAt(LocalDate.now());
        review.setUpdatedAt(LocalDate.now());

        reviewDTO = new PerformanceReviewDTO();
        reviewDTO.setEmployeeId(1L);
        reviewDTO.setReviewedById(2L);
        reviewDTO.setReviewPeriod("2023-Q1");
        reviewDTO.setReviewDate(LocalDate.of(2023, 3, 31));
        reviewDTO.setOverallRating(4.5);
        reviewDTO.setTechnicalSkillsRating(4.0);
        reviewDTO.setBehavioralRating(4.5);
        reviewDTO.setLeadershipRating(4.0);
        reviewDTO.setTeamworkRating(4.5);
        reviewDTO.setStrengths("Strong technical skills");
        reviewDTO.setAreasForImprovement("Communication");
        reviewDTO.setComments("Good performance");
        reviewDTO.setStatus("COMPLETED");
    }

    @Test
    @DisplayName("Given valid review DTO when creating review then return saved review DTO")
    void givenValidReviewDTO_whenCreateReview_thenReturnSavedReviewDTO() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(review);

        // Act
        PerformanceReviewDTO result = performanceReviewService.createReview(reviewDTO);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals(2L, result.getReviewedById());
        assertEquals("Jane Smith", result.getReviewedByName());
        assertEquals("2023-Q1", result.getReviewPeriod());
        assertEquals(4.5, result.getOverallRating());
        assertEquals("COMPLETED", result.getStatus());
        verify(reviewRepository, times(1)).save(any(PerformanceReview.class));
    }

    @Test
    @DisplayName("Given null review date when creating review then use current date")
    void givenNullReviewDate_whenCreateReview_thenReturnReviewWithCurrentDate() {
        // Arrange
        reviewDTO.setReviewDate(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(reviewRepository.save(any(PerformanceReview.class))).thenAnswer(invocation -> {
            PerformanceReview saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        // Act
        PerformanceReviewDTO result = performanceReviewService.createReview(reviewDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getReviewDate());
        assertEquals(LocalDate.now(), result.getReviewDate());
    }

    @Test
    @DisplayName("Given null status when creating review then default to DRAFT")
    void givenNullStatus_whenCreateReview_thenReturnReviewWithDraftStatus() {
        // Arrange
        reviewDTO.setStatus(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(reviewRepository.save(any(PerformanceReview.class))).thenAnswer(invocation -> {
            PerformanceReview saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        // Act
        PerformanceReviewDTO result = performanceReviewService.createReview(reviewDTO);

        // Assert
        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
    }

    @Test
    @DisplayName("Given non-existent employee when creating review then throw RuntimeException")
    void givenNonExistentEmployee_whenCreateReview_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            performanceReviewService.createReview(reviewDTO);
        });
        assertTrue(exception.getMessage().contains("Employee not found with id: 1"));
        verify(reviewRepository, never()).save(any(PerformanceReview.class));
    }

    @Test
    @DisplayName("Given non-existent manager when creating review then throw RuntimeException")
    void givenNonExistentManager_whenCreateReview_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            performanceReviewService.createReview(reviewDTO);
        });
        assertTrue(exception.getMessage().contains("Manager not found with id: 2"));
        verify(reviewRepository, never()).save(any(PerformanceReview.class));
    }

    @Test
    @DisplayName("Given valid review ID when getting review by ID then return review DTO")
    void givenValidReviewId_whenGetReviewById_thenReturnReviewDTO() {
        // Arrange
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));

        // Act
        PerformanceReviewDTO result = performanceReviewService.getReviewById(100L);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals(2L, result.getReviewedById());
        assertEquals("Jane Smith", result.getReviewedByName());
        assertEquals("2023-Q1", result.getReviewPeriod());
        assertEquals(4.5, result.getOverallRating());
        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    @DisplayName("Given non-existent review ID when getting review by ID then throw RuntimeException")
    void givenNonExistentReviewId_whenGetReviewById_thenThrowRuntimeException() {
        // Arrange
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            performanceReviewService.getReviewById(999L);
        });
        assertTrue(exception.getMessage().contains("Performance review not found with id: 999"));
    }

    @Test
    @DisplayName("Given valid employee ID when getting reviews by employee then return list of review DTOs")
    void givenValidEmployeeId_whenGetReviewsByEmployee_thenReturnListOfReviewDTOs() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(reviewRepository.findByEmployee(employee)).thenReturn(List.of(review));

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(1L, result.get(0).getEmployeeId());
        assertEquals("John Doe", result.get(0).getEmployeeName());
    }

    @Test
    @DisplayName("Given employee with no reviews when getting reviews by employee then return empty list")
    void givenEmployeeWithNoReviews_whenGetReviewsByEmployee_thenReturnEmptyList() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(reviewRepository.findByEmployee(employee)).thenReturn(Collections.emptyList());

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given non-existent employee ID when getting reviews by employee then throw RuntimeException")
    void givenNonExistentEmployeeId_whenGetReviewsByEmployee_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            performanceReviewService.getReviewsByEmployee(999L);
        });
        assertTrue(exception.getMessage().contains("Employee not found with id: 999"));
    }

    @Test
    @DisplayName("Given valid manager ID when getting reviews by manager then return list of review DTOs")
    void givenValidManagerId_whenGetReviewsByManager_thenReturnListOfReviewDTOs() {
        // Arrange
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(reviewRepository.findByReviewedBy(manager)).thenReturn(List.of(review));

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByManager(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(2L, result.get(0).getReviewedById());
        assertEquals("Jane Smith", result.get(0).getReviewedByName());
    }

    @Test
    @DisplayName("Given manager with no reviews when getting reviews by manager then return empty list")
    void givenManagerWithNoReviews_whenGetReviewsByManager_thenReturnEmptyList() {
        // Arrange
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(reviewRepository.findByReviewedBy(manager)).thenReturn(Collections.emptyList());

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByManager(2L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given non-existent manager ID when getting reviews by manager then throw RuntimeException")
    void givenNonExistentManagerId_whenGetReviewsByManager_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            performanceReviewService.getReviewsByManager(999L);
        });
        assertTrue(exception.getMessage().contains("Manager not found with id: 999"));
    }

    @Test
    @DisplayName("Given valid period when getting reviews by period then return list of review DTOs")
    void givenValidPeriod_whenGetReviewsByPeriod_thenReturnListOfReviewDTOs() {
        // Arrange
        when(reviewRepository.findByReviewPeriod("2023-Q1")).thenReturn(List.of(review));

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByPeriod("2023-Q1");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals("2023-Q1", result.get(0).getReviewPeriod());
    }

    @Test
    @DisplayName("Given period with no reviews when getting reviews by period then return empty list")
    void givenPeriodWithNoReviews_whenGetReviewsByPeriod_thenReturnEmptyList() {
        // Arrange
        when(reviewRepository.findByReviewPeriod("2023-Q2")).thenReturn(Collections.emptyList());

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByPeriod("2023-Q2");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given valid review ID and DTO when updating review then return updated review DTO")
    void givenValidReviewIdAndDTO_whenUpdateReview_thenReturnUpdatedReviewDTO() {
        // Arrange
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(review);

        // Act
        PerformanceReviewDTO result = performanceReviewService.updateReview(100L, reviewDTO);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(4.5, result.getOverallRating());
        assertEquals("COMPLETED", result.getStatus());
        verify(reviewRepository, times(1)).save(any(PerformanceReview.class));
    }

    @Test
    @DisplayName("Given non-existent review ID when updating review then throw RuntimeException")
    void givenNonExistentReviewId_whenUpdateReview_thenThrowRuntimeException() {
        // Arrange
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            performanceReviewService.updateReview(999L, reviewDTO);
        });
        assertTrue(exception.getMessage().contains("Performance review not found with id: 999"));
        verify(reviewRepository, never()).save(any(PerformanceReview.class));
    }

    @Test
    @DisplayName("Given valid review ID when deleting review then delete review successfully")
    void givenValidReviewId_whenDeleteReview_thenDeleteReviewSuccessfully() {
        // Arrange
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));

        // Act
        performanceReviewService.deleteReview(100L);

        // Assert
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("Given non-existent review ID when deleting review then throw RuntimeException")
    void givenNonExistentReviewId_whenDeleteReview_thenThrowRuntimeException() {
        // Arrange
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            performanceReviewService.deleteReview(999L);
        });
        assertTrue(exception.getMessage().contains("Performance review not found with id: 999"));
        verify(reviewRepository, never()).delete(any(PerformanceReview.class));
    }

    @Test
    @DisplayName("Given multiple reviews for employee when getting reviews by employee then return all review DTOs")
    void givenMultipleReviewsForEmployee_whenGetReviewsByEmployee_thenReturnAllReviewDTOs() {
        // Arrange
        PerformanceReview review2 = new PerformanceReview();
        review2.setId(101L);
        review2.setEmployee(employee);
        review2.setReviewedBy(manager);
        review2.setReviewPeriod("2023-Q2");
        review2.setReviewDate(LocalDate.of(2023, 6, 30));
        review2.setOverallRating(4.0);
        review2.setTechnicalSkillsRating(3.5);
        review2.setBehavioralRating(4.0);
        review2.setLeadershipRating(3.5);
        review2.setTeamworkRating(4.0);
        review2.setStrengths("Good teamwork");
        review2.setAreasForImprovement("Technical depth");
        review2.setComments("Solid performance");
        review2.setStatus("COMPLETED");
        review2.setCreatedAt(LocalDate.now());
        review2.setUpdatedAt(LocalDate.now());

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(reviewRepository.findByEmployee(employee)).thenReturn(List.of(review, review2));

        // Act
        List<PerformanceReviewDTO> result = performanceReviewService.getReviewsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(101L, result.get(1).getId());
    }
}