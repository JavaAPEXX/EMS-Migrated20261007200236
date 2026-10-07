package com.employee.system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceSummaryTest {

    @InjectMocks
    private AttendanceSummary attendanceSummary;


    @Test
    @DisplayName("Test AttendanceSummary instance initialization")
    public void testInstance_NotNull() {
        assertNotNull(attendanceSummary);
    }

}
