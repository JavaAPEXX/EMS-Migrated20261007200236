package com.employee.system.service;

import com.employee.system.dto.RatingScaleDTO;
import com.employee.system.entity.RatingScale;
import com.employee.system.repository.RatingScaleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingScaleServiceTest {

    @Mock
    private RatingScaleRepository ratingScaleRepository;

    @InjectMocks
    private RatingScaleService ratingScaleService;

    private RatingScale sampleEntity(Long id, Integer value, Boolean active) {
        RatingScale rs = new RatingScale();
        rs.setId(id);
        rs.setRatingValue(value);
        rs.setRatingLabel("Label-" + value);
        rs.setRatingDescription("Desc-" + value);
        rs.setColorCode("#FFFFFF");
        rs.setMinimumScore(0);
        rs.setMaximumScore(100);
        rs.setIsActive(active);
        return rs;
    }

    private RatingScaleDTO sampleDto(Integer value, Boolean active) {
        RatingScaleDTO dto = new RatingScaleDTO();
        dto.setRatingValue(value);
        dto.setRatingLabel("Label-" + value);
        dto.setRatingDescription("Desc-" + value);
        dto.setColorCode("#FFFFFF");
        dto.setMinimumScore(0);
        dto.setMaximumScore(100);
        dto.setIsActive(active);
        return dto;
    }

    @Nested
    @DisplayName("getRatingScaleById")
    class GetById {

        @Test
        @DisplayName("givenExistingId_whenGetRatingScaleById_thenReturnDto")
        void givenExistingId_whenGetRatingScaleById_thenReturnDto() {
            // Arrange
            Long id = 1L;
            RatingScale entity = sampleEntity(id, 5, true);
            when(ratingScaleRepository.findById(id)).thenReturn(Optional.of(entity));

            // Act
            RatingScaleDTO result = ratingScaleService.getRatingScaleById(id);

            // Assert
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(entity.getRatingValue(), result.getRatingValue());
            verify(ratingScaleRepository, times(1)).findById(id);
        }

        @Test
        @DisplayName("givenNonExistingId_whenGetRatingScaleById_thenThrowRuntimeException")
        void givenNonExistingId_whenGetRatingScaleById_thenThrowRuntimeException() {
            // Arrange
            Long id = 99L;
            when(ratingScaleRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> ratingScaleService.getRatingScaleById(id));
            assertTrue(ex.getMessage().contains("Rating scale not found with id: " + id));
            verify(ratingScaleRepository, times(1)).findById(id);
        }
    }

    @Nested
    @DisplayName("getRatingScaleByValue")
    class GetByValue {

        @Test
        @DisplayName("givenExistingValue_whenGetRatingScaleByValue_thenReturnDto")
        void givenExistingValue_whenGetRatingScaleByValue_thenReturnDto() {
            // Arrange
            Integer value = 3;
            RatingScale entity = sampleEntity(2L, value, true);
            when(ratingScaleRepository.findByRatingValue(value)).thenReturn(Optional.of(entity));

            // Act
            RatingScaleDTO result = ratingScaleService.getRatingScaleByValue(value);

            // Assert
            assertNotNull(result);
            assertEquals(value, result.getRatingValue());
            verify(ratingScaleRepository, times(1)).findByRatingValue(value);
        }

        @Test
        @DisplayName("givenNonExistingValue_whenGetRatingScaleByValue_thenThrowRuntimeException")
        void givenNonExistingValue_whenGetRatingScaleByValue_thenThrowRuntimeException() {
            // Arrange
            Integer value = 999;
            when(ratingScaleRepository.findByRatingValue(value)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> ratingScaleService.getRatingScaleByValue(value));
            assertTrue(ex.getMessage().contains("Rating scale not found with value: " + value));
            verify(ratingScaleRepository, times(1)).findByRatingValue(value);
        }
    }

    @Nested
    @DisplayName("getAllActiveRatingScales")
    class GetAllActive {

        @Test
        @DisplayName("givenActiveScales_whenGetAllActiveRatingScales_thenReturnDtoList")
        void givenActiveScales_whenGetAllActiveRatingScales_thenReturnDtoList() {
            // Arrange
            RatingScale rs1 = sampleEntity(1L, 1, true);
            RatingScale rs2 = sampleEntity(2L, 2, true);
            when(ratingScaleRepository.findAllActive()).thenReturn(List.of(rs1, rs2));

            // Act
            List<RatingScaleDTO> result = ratingScaleService.getAllActiveRatingScales();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(rs1.getId(), result.get(0).getId());
            assertEquals(rs2.getId(), result.get(1).getId());
            verify(ratingScaleRepository, times(1)).findAllActive();
        }

        @Test
        @DisplayName("givenNoActiveScales_whenGetAllActiveRatingScales_thenReturnEmptyList")
        void givenNoActiveScales_whenGetAllActiveRatingScales_thenReturnEmptyList() {
            // Arrange
            when(ratingScaleRepository.findAllActive()).thenReturn(Collections.emptyList());

            // Act
            List<RatingScaleDTO> result = ratingScaleService.getAllActiveRatingScales();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(ratingScaleRepository, times(1)).findAllActive();
        }
    }

    @Nested
    @DisplayName("getAllRatingScales")
    class GetAll {

        @Test
        @DisplayName("givenScales_whenGetAllRatingScales_thenReturnDtoList")
        void givenScales_whenGetAllRatingScales_thenReturnDtoList() {
            // Arrange
            RatingScale rs1 = sampleEntity(1L, 1, true);
            RatingScale rs2 = sampleEntity(2L, 2, false);
            when(ratingScaleRepository.findAll()).thenReturn(List.of(rs1, rs2));

            // Act
            List<RatingScaleDTO> result = ratingScaleService.getAllRatingScales();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(ratingScaleRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("givenEmptyDatabase_whenGetAllRatingScales_thenReturnEmptyList")
        void givenEmptyDatabase_whenGetAllRatingScales_thenReturnEmptyList() {
            // Arrange
            when(ratingScaleRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<RatingScaleDTO> result = ratingScaleService.getAllRatingScales();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(ratingScaleRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("createRatingScale")
    class Create {

        @Test
        @DisplayName("givenValidDto_whenCreateRatingScale_thenReturnSavedDto")
        void givenValidDto_whenCreateRatingScale_thenReturnSavedDto() {
            // Arrange
            RatingScaleDTO inputDto = sampleDto(4, null); // isActive null -> default true
            RatingScale savedEntity = sampleEntity(10L, 4, true);
            when(ratingScaleRepository.save(any(RatingScale.class))).thenReturn(savedEntity);

            // Act
            RatingScaleDTO result = ratingScaleService.createRatingScale(inputDto);

            // Assert
            assertNotNull(result);
            assertEquals(savedEntity.getId(), result.getId());
            assertTrue(result.getIsActive()); // defaulted to true
            ArgumentCaptor<RatingScale> captor = ArgumentCaptor.forClass(RatingScale.class);
            verify(ratingScaleRepository, times(1)).save(captor.capture());
            RatingScale captured = captor.getValue();
            assertEquals(inputDto.getRatingValue(), captured.getRatingValue());
            assertEquals(inputDto.getRatingLabel(), captured.getRatingLabel());
        }

        @Test
        @DisplayName("givenNullDto_whenCreateRatingScale_thenThrowNullPointerException")
        void givenNullDto_whenCreateRatingScale_thenThrowNullPointerException() {
            // Arrange
            RatingScaleDTO nullDto = null;

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> ratingScaleService.createRatingScale(nullDto));
            verifyNoInteractions(ratingScaleRepository);
        }
    }

    @Nested
    @DisplayName("updateRatingScale")
    class Update {

        @Test
        @DisplayName("givenExistingIdAndDto_whenUpdateRatingScale_thenReturnUpdatedDto")
        void givenExistingIdAndDto_whenUpdateRatingScale_thenReturnUpdatedDto() {
            // Arrange
            Long id = 5L;
            RatingScale existing = sampleEntity(id, 1, true);
            RatingScaleDTO updateDto = sampleDto(7, false);
            when(ratingScaleRepository.findById(id)).thenReturn(Optional.of(existing));
            when(ratingScaleRepository.save(existing)).thenReturn(existing); // repository returns same instance

            // Act
            RatingScaleDTO result = ratingScaleService.updateRatingScale(id, updateDto);

            // Assert
            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(updateDto.getRatingValue(), result.getRatingValue());
            assertFalse(result.getIsActive());
            verify(ratingScaleRepository, times(1)).findById(id);
            verify(ratingScaleRepository, times(1)).save(existing);
        }

        @Test
        @DisplayName("givenNonExistingId_whenUpdateRatingScale_thenThrowRuntimeException")
        void givenNonExistingId_whenUpdateRatingScale_thenThrowRuntimeException() {
            // Arrange
            Long id = 999L;
            RatingScaleDTO dto = sampleDto(3, true);
            when(ratingScaleRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> ratingScaleService.updateRatingScale(id, dto));
            assertTrue(ex.getMessage().contains("Rating scale not found with id: " + id));
            verify(ratingScaleRepository, times(1)).findById(id);
            verify(ratingScaleRepository, never()).save(any());
        }

        @Test
        @DisplayName("givenNullDto_whenUpdateRatingScale_thenThrowNullPointerException")
        void givenNullDto_whenUpdateRatingScale_thenThrowNullPointerException() {
            // Arrange
            Long id = 1L;
            RatingScale existing = sampleEntity(id, 2, true);
            when(ratingScaleRepository.findById(id)).thenReturn(Optional.of(existing));

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> ratingScaleService.updateRatingScale(id, null));
            verify(ratingScaleRepository, times(1)).findById(id);
            verify(ratingScaleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteRatingScale")
    class Delete {

        @Test
        @DisplayName("givenExistingId_whenDeleteRatingScale_thenEntityDeleted")
        void givenExistingId_whenDeleteRatingScale_thenEntityDeleted() {
            // Arrange
            Long id = 3L;
            RatingScale entity = sampleEntity(id, 5, true);
            when(ratingScaleRepository.findById(id)).thenReturn(Optional.of(entity));
            doNothing().when(ratingScaleRepository).delete(entity);

            // Act
            ratingScaleService.deleteRatingScale(id);

            // Assert
            verify(ratingScaleRepository, times(1)).findById(id);
            verify(ratingScaleRepository, times(1)).delete(entity);
        }

        @Test
        @DisplayName("givenNonExistingId_whenDeleteRatingScale_thenThrowRuntimeException")
        void givenNonExistingId_whenDeleteRatingScale_thenThrowRuntimeException() {
            // Arrange
            Long id = 404L;
            when(ratingScaleRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> ratingScaleService.deleteRatingScale(id));
            assertTrue(ex.getMessage().contains("Rating scale not found with id: " + id));
            verify(ratingScaleRepository, times(1)).findById(id);
            verify(ratingScaleRepository, never()).delete(any());
        }
    }
}