import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RatingScaleServiceTest {

    private RatingScaleRepository ratingScaleRepository = mock(RatingScaleRepository.class);
    private RatingScaleService ratingScaleService = new RatingScaleService(ratingScaleRepository);

    @Nested
    @DisplayName("getRatingScaleById")
    class GetById {

        @Test
        @DisplayName("givenExistingId_whenGetRatingScaleById_thenReturnDto")
        void givenExistingId_whenGetRatingScaleById_thenReturnDto() {
            // Arrange
            Long id = 1L;
            RatingScale entity = sampleEntity(id, 1, true);
            when(ratingScaleRepository.findById(id)).thenReturn(Optional.of(entity));

            // Act
            RatingScaleDTO result = ratingScaleService.getRatingScaleById(id);

            // Assert
            assertNotNull(result);
            assertEquals(id, result.getId());
            verify(ratingScaleRepository, times(1)).findById(id);
        }

        @Test
        @DisplayName("givenNonExistingId_whenGetRatingScaleById_thenThrowRuntimeException")
        void givenNonExistingId_whenGetRatingScaleById_thenThrowRuntimeException() {
            // Arrange
            Long id = 999L;
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
            int value = 1;
            RatingScale entity = sampleEntity(1L, value, true);
            when(ratingScaleRepository.findByRatingValue(value)).thenReturn(Optional.of(entity));

            // Act
            RatingScaleDTO result = ratingScaleService.getRatingScaleByValue(value);

            // Assert
            assertNotNull(result);
            assertEquals(entity.getId(), result.getId());
            verify(ratingScaleRepository, times(1)).findByRatingValue(value);
        }

        @Test
        @DisplayName("givenNonExistingValue_whenGetRatingScaleByValue_thenThrowRuntimeException")
        void givenNonExistingValue_whenGetRatingScaleByValue_thenThrowRuntimeException() {
            // Arrange
            int value = 99;
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
            RatingScaleDTO updateDto = sampleDto(7, false);
            when(ratingScaleRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> ratingScaleService.updateRatingScale(id, updateDto));
            assertTrue(ex.getMessage().contains("Rating scale not found with id: " + id));
            verify(ratingScaleRepository, times(1)).findById(id);
        }

        @Test
        @DisplayName("givenNullDto_whenUpdateRatingScale_thenThrowNullPointerException")
        void givenNullDto_whenUpdateRatingScale_thenThrowNullPointerException() {
            // Arrange
            Long id = 5L;
            RatingScaleDTO nullDto = null;

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> ratingScaleService.updateRatingScale(id, nullDto));
            verifyNoInteractions(ratingScaleRepository);
        }
    }

    @Nested
    @DisplayName("deleteRatingScale")
    class Delete {

        @Test
        @DisplayName("givenExistingId_whenDeleteRatingScale_thenNoException")
        void givenExistingId_whenDeleteRatingScale_thenNoException() {
            // Arrange
            Long id = 5L;
            when(ratingScaleRepository.existsById(id)).thenReturn(true);

            // Act
            ratingScaleService.deleteRatingScale(id);

            // Assert
            verify(ratingScaleRepository, times(1)).existsById(id);
            verify(ratingScaleRepository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("givenNonExistingId_whenDeleteRatingScale_thenNoException")
        void givenNonExistingId_whenDeleteRatingScale_thenNoException() {
            // Arrange
            Long id = 999L;
            when(ratingScaleRepository.existsById(id)).thenReturn(false);

            // Act
            ratingScaleService.deleteRatingScale(id);

            // Assert
            verify(ratingScaleRepository, times(1)).existsById(id);
            verifyNoInteractions(ratingScaleRepository);
        }
    }

    private RatingScale sampleEntity(Long id, int value, boolean active) {
        RatingScale entity = new RatingScale();
        entity.setId(id);
        entity.setRatingValue(value);
        entity.setIsActive(active);
        return entity;
    }

    private RatingScaleDTO sampleDto(int value, Boolean active) {
        RatingScaleDTO dto = new RatingScaleDTO();
        dto.setRatingValue(value);
        dto.setIsActive(active);
        return dto;
    }
}
