package com.employee.system.service;

import com.employee.system.dto.KPIDTO;
import com.employee.system.entity.Employee;
import com.employee.system.entity.KPI;
import com.employee.system.repository.EmployeeRepository;
import com.employee.system.repository.KPIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KPIServiceTest {

    @Mock
    private KPIRepository kpiRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private KPIService kpiService;

    private KPIDTO kpiDTO;
    private KPI kpi;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        kpiDTO = new KPIDTO();
        kpiDTO.setEmployeeId(1L);
        kpiDTO.setKpiName("Performance");
        kpiDTO.setKpiDescription("Monthly Sales");
        kpiDTO.setMeasurementUnit("Units");
        kpiDTO.setTargetValue(BigDecimal.valueOf(1000));
        kpiDTO.setCurrentValue(BigDecimal.valueOf(800));
        kpiDTO.setWeight(0.5);
        kpiDTO.setFrequency("Monthly");
        kpiDTO.setStartDate(LocalDate.now());
        kpiDTO.setEndDate(LocalDate.now().plusMonths(1));
        kpiDTO.setStatus("ACTIVE");
        kpiDTO.setRemarks("Initial KPI");

        kpi = new KPI();
        kpi.setId(1L);
        kpi.setEmployee(employee);
        kpi.setKpiName("Performance");
        kpi.setKpiDescription("Monthly Sales");
        kpi.setMeasurementUnit("Units");
        kpi.setTargetValue(BigDecimal.valueOf(1000));
        kpi.setCurrentValue(BigDecimal.valueOf(800));
        kpi.setWeight(0.5);
        kpi.setFrequency("Monthly");
        kpi.setStartDate(LocalDate.now());
        kpi.setEndDate(LocalDate.now().plusMonths(1));
        kpi.setStatus("ACTIVE");
        kpi.setRemarks("Initial KPI");

        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
    }

    @Test
    @DisplayName("givenValidInput_whenCreateKPI_thenReturnSuccess")
    public void givenValidInput_whenCreateKPI_thenReturnSuccess() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(kpiRepository.save(any(KPI.class))).thenReturn(kpi);

        // Act
        KPIDTO result = kpiService.createKPI(kpiDTO);

        // Assert
        assertEquals(kpiDTO.getEmployeeId(), result.getEmployeeId());
        assertEquals(kpiDTO.getKpiName(), result.getKpiName());
        assertEquals(kpiDTO.getKpiDescription(), result.getKpiDescription());
        assertEquals(kpiDTO.getMeasurementUnit(), result.getMeasurementUnit());
        assertEquals(kpiDTO.getTargetValue(), result.getTargetValue());
        assertEquals(kpiDTO.getCurrentValue(), result.getCurrentValue());
        assertEquals(kpiDTO.getWeight(), result.getWeight());
        assertEquals(kpiDTO.getFrequency(), result.getFrequency());
        assertEquals(kpiDTO.getStartDate(), result.getStartDate());
        assertEquals(kpiDTO.getEndDate(), result.getEndDate());
        assertEquals(kpiDTO.getStatus(), result.getStatus());
        assertEquals(kpiDTO.getRemarks(), result.getRemarks());
        verify(kpiRepository, times(1)).save(any(KPI.class));
    }

    @Test
    @DisplayName("givenNonExistingEmployeeId_whenCreateKPI_thenThrowRuntimeException")
    public void givenNonExistingEmployeeId_whenCreateKPI_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> kpiService.createKPI(kpiDTO));
    }

    @Test
    @DisplayName("givenValidId_whenGetKPIById_thenReturnSuccess")
    public void givenValidId_whenGetKPIById_thenReturnSuccess() {
        // Arrange
        when(kpiRepository.findById(1L)).thenReturn(Optional.of(kpi));

        // Act
        KPIDTO result = kpiService.getKPIById(1L);

        // Assert
        assertEquals(kpiDTO.getEmployeeId(), result.getEmployeeId());
        assertEquals(kpiDTO.getKpiName(), result.getKpiName());
        assertEquals(kpiDTO.getKpiDescription(), result.getKpiDescription());
        assertEquals(kpiDTO.getMeasurementUnit(), result.getMeasurementUnit());
        assertEquals(kpiDTO.getTargetValue(), result.getTargetValue());
        assertEquals(kpiDTO.getCurrentValue(), result.getCurrentValue());
        assertEquals(kpiDTO.getWeight(), result.getWeight());
        assertEquals(kpiDTO.getFrequency(), result.getFrequency());
        assertEquals(kpiDTO.getStartDate(), result.getStartDate());
        assertEquals(kpiDTO.getEndDate(), result.getEndDate());
        assertEquals(kpiDTO.getStatus(), result.getStatus());
        assertEquals(kpiDTO.getRemarks(), result.getRemarks());
        verify(kpiRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("givenNonExistingId_whenGetKPIById_thenThrowRuntimeException")
    public void givenNonExistingId_whenGetKPIById_thenThrowRuntimeException() {
        // Arrange
        when(kpiRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> kpiService.getKPIById(1L));
    }

    @Test
    @DisplayName("givenValidEmployeeId_whenGetKPIsByEmployee_thenReturnSuccess")
    public void givenValidEmployeeId_whenGetKPIsByEmployee_thenReturnSuccess() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(kpiRepository.findByEmployee(employee)).thenReturn(List.of(kpi));

        // Act
        List<KPIDTO> result = kpiService.getKPIsByEmployee(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(kpiDTO.getEmployeeId(), result.get(0).getEmployeeId());
        assertEquals(kpiDTO.getKpiName(), result.get(0).getKpiName());
        assertEquals(kpiDTO.getKpiDescription(), result.get(0).getKpiDescription());
        assertEquals(kpiDTO.getMeasurementUnit(), result.get(0).getMeasurementUnit());
        assertEquals(kpiDTO.getTargetValue(), result.get(0).getTargetValue());
        assertEquals(kpiDTO.getCurrentValue(), result.get(0).getCurrentValue());
        assertEquals(kpiDTO.getWeight(), result.get(0).getWeight());
        assertEquals(kpiDTO.getFrequency(), result.get(0).getFrequency());
        assertEquals(kpiDTO.getStartDate(), result.get(0).getStartDate());
        assertEquals(kpiDTO.getEndDate(), result.get(0).getEndDate());
        assertEquals(kpiDTO.getStatus(), result.get(0).getStatus());
        assertEquals(kpiDTO.getRemarks(), result.get(0).getRemarks());
        verify(kpiRepository, times(1)).findByEmployee(employee);
    }

    @Test
    @DisplayName("givenNonExistingEmployeeId_whenGetKPIsByEmployee_thenThrowRuntimeException")
    public void givenNonExistingEmployeeId_whenGetKPIsByEmployee_thenThrowRuntimeException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> kpiService.getKPIsByEmployee(1L));
    }

    @Test
    @DisplayName("givenStatusACTIVE_whenGetActiveKPIs_thenReturnSuccess")
    public void givenStatusACTIVE_whenGetActiveKPIs_thenReturnSuccess() {
        // Arrange
        when(kpiRepository.findByStatus("ACTIVE")).thenReturn(List.of(kpi));

        // Act
        List<KPIDTO> result = kpiService.getActiveKPIs();

        // Assert
        assertEquals(1, result.size());
        assertEquals(kpiDTO.getEmployeeId(), result.get(0).getEmployeeId());
        assertEquals(kpiDTO.getKpiName(), result.get(0).getKpiName());
        assertEquals(kpiDTO.getKpiDescription(), result.get(0).getKpiDescription());
        assertEquals(kpiDTO.getMeasurementUnit(), result.get(0).getMeasurementUnit());
        assertEquals(kpiDTO.getTargetValue(), result.get(0).getTargetValue());
        assertEquals(kpiDTO.getCurrentValue(), result.get(0).getCurrentValue());
        assertEquals(kpiDTO.getWeight(), result.get(0).getWeight());
        assertEquals(kpiDTO.getFrequency(), result.get(0).getFrequency());
        assertEquals(kpiDTO.getStartDate(), result.get(0).getStartDate());
        assertEquals(kpiDTO.getEndDate(), result.get(0).getEndDate());
        assertEquals(kpiDTO.getStatus(), result.get(0).getStatus());
        assertEquals(kpiDTO.getRemarks(), result.get(0).getRemarks());
        verify(kpiRepository, times(1)).findByStatus("ACTIVE");
    }

    @Test
    @DisplayName("givenFrequencyMonthly_whenGetKPIsByFrequency_thenReturnSuccess")
    public void givenFrequencyMonthly_whenGetKPIsByFrequency_thenReturnSuccess() {
        // Arrange
        when(kpiRepository.findByFrequency("Monthly")).thenReturn(List.of(kpi));

        // Act
        List<KPIDTO> result = kpiService.getKPIsByFrequency("Monthly");

        // Assert
        assertEquals(1, result.size());
        assertEquals(kpiDTO.getEmployeeId(), result.get(0).getEmployeeId());
        assertEquals(kpiDTO.getKpiName(), result.get(0).getKpiName());
        assertEquals(kpiDTO.getKpiDescription(), result.get(0).getKpiDescription());
        assertEquals(kpiDTO.getMeasurementUnit(), result.get(0).getMeasurementUnit());
        assertEquals(kpiDTO.getTargetValue(), result.get(0).getTargetValue());
        assertEquals(kpiDTO.getCurrentValue(), result.get(0).getCurrentValue());
        assertEquals(kpiDTO.getWeight(), result.get(0).getWeight());
        assertEquals(kpiDTO.getFrequency(), result.get(0).getFrequency());
        assertEquals(kpiDTO.getStartDate(), result.get(0).getStartDate());
        assertEquals(kpiDTO.getEndDate(), result.get(0).getEndDate());
        assertEquals(kpiDTO.getStatus(), result.get(0).getStatus());
        assertEquals(kpiDTO.getRemarks(), result.get(0).getRemarks());
        verify(kpiRepository, times(1)).findByFrequency("Monthly");
    }

    @Test
    @DisplayName("givenValidId_whenUpdateKPI_thenReturnSuccess")
    public void givenValidId_whenUpdateKPI_thenReturnSuccess() {
        // Arrange
        when(kpiRepository.findById(1L)).thenReturn(Optional.of(kpi));
        when(kpiRepository.save(any(KPI.class))).thenReturn(kpi);

        // Act
        KPIDTO result = kpiService.updateKPI(1L, kpiDTO);

        // Assert
        assertEquals(kpiDTO.getEmployeeId(), result.getEmployeeId());
        assertEquals(kpiDTO.getKpiName(), result.getKpiName());
        assertEquals(kpiDTO.getKpiDescription(), result.getKpiDescription());
        assertEquals(kpiDTO.getMeasurementUnit(), result.getMeasurementUnit());
        assertEquals(kpiDTO.getTargetValue(), result.getTargetValue());
        assertEquals(kpiDTO.getCurrentValue(), result.getCurrentValue());
        assertEquals(kpiDTO.getWeight(), result.getWeight());
        assertEquals(kpiDTO.getFrequency(), result.getFrequency());
        assertEquals(kpiDTO.getStartDate(), result.getStartDate());
        assertEquals(kpiDTO.getEndDate(), result.getEndDate());
        assertEquals(kpiDTO.getStatus(), result.getStatus());
        assertEquals(kpiDTO.getRemarks(), result.getRemarks());
        verify(kpiRepository, times(1)).save(any(KPI.class));
    }

    @Test
    @DisplayName("givenNonExistingId_whenUpdateKPI_thenThrowRuntimeException")
    public void givenNonExistingId_whenUpdateKPI_thenThrowRuntimeException() {
        // Arrange
        when(kpiRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> kpiService.updateKPI(1L, kpiDTO));
    }

    @Test
    @DisplayName("givenValidId_whenDeleteKPI_thenReturnSuccess")
    public void givenValidId_whenDeleteKPI_thenReturnSuccess() {
        // Arrange
        when(kpiRepository.findById(1L)).thenReturn(Optional.of(kpi));

        // Act
        kpiService.deleteKPI(1L);

        // Assert
        verify(kpiRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("givenNonExistingId_whenDeleteKPI_thenThrowRuntimeException")
    public void givenNonExistingId_whenDeleteKPI_thenThrowRuntimeException() {
        // Arrange
        when(kpiRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> kpiService.deleteKPI(1L));
    }
}