package com.app.fitness.controller;

import com.app.fitness.dto.ProgressEntryRequest;
import com.app.fitness.dto.ProgressEntryResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.ProgressEntryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProgressEntryControllerTest extends ControllerTestSupport {

    @Mock
    private ProgressEntryService progressEntryService;

    @InjectMocks
    private ProgressEntryController progressEntryController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(progressEntryController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(progressEntryService.findAll()).thenReturn(List.of(
                new ProgressEntryResponse(1L, 3L, LocalDate.of(2026, 5, 1),
                        new BigDecimal("82.5"), new BigDecimal("18.2"), "Steady progress")));

        mockMvc.perform(get("/api/progress-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notes").value("Steady progress"));
    }

    @Test
    void getById_whenEntryExists_shouldReturnEntry() throws Exception {
        when(progressEntryService.findById(1L)).thenReturn(
                new ProgressEntryResponse(1L, 3L, LocalDate.of(2026, 5, 1),
                        new BigDecimal("82.5"), new BigDecimal("18.2"), "Steady progress"));

        mockMvc.perform(get("/api/progress-entries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.weightKg").value(82.5));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(progressEntryService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Progress entry not found with id: 99"));

        mockMvc.perform(get("/api/progress-entries/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        ProgressEntryRequest request = new ProgressEntryRequest(3L, LocalDate.of(2026, 5, 2),
                new BigDecimal("82.0"), new BigDecimal("18.0"), "Improved");
        when(progressEntryService.create(any(ProgressEntryRequest.class))).thenReturn(
                new ProgressEntryResponse(2L, 3L, LocalDate.of(2026, 5, 2),
                        new BigDecimal("82.0"), new BigDecimal("18.0"), "Improved"));

        mockMvc.perform(post("/api/progress-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").value("Improved"));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        ProgressEntryRequest request = new ProgressEntryRequest(null, null, new BigDecimal("-1"), null, null);

        mockMvc.perform(post("/api/progress-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        ProgressEntryRequest request = new ProgressEntryRequest(3L, LocalDate.of(2026, 5, 1),
                new BigDecimal("82.5"), new BigDecimal("18.2"), "Steady progress");
        when(progressEntryService.create(any(ProgressEntryRequest.class)))
                .thenThrow(new DuplicateResourceException("Progress entry already exists for userId=3, date=2026-05-01"));

        mockMvc.perform(post("/api/progress-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        ProgressEntryRequest request = new ProgressEntryRequest(3L, LocalDate.of(2026, 5, 3),
                new BigDecimal("81.8"), new BigDecimal("17.8"), "Great");
        when(progressEntryService.update(eq(1L), any(ProgressEntryRequest.class))).thenReturn(
                new ProgressEntryResponse(1L, 3L, LocalDate.of(2026, 5, 3),
                        new BigDecimal("81.8"), new BigDecimal("17.8"), "Great"));

        mockMvc.perform(put("/api/progress-entries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bodyFatPct").value(17.8));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        ProgressEntryRequest request = new ProgressEntryRequest(3L, LocalDate.of(2026, 5, 3),
                new BigDecimal("81.8"), new BigDecimal("17.8"), "Great");
        when(progressEntryService.update(eq(99L), any(ProgressEntryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Progress entry not found with id: 99"));

        mockMvc.perform(put("/api/progress-entries/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/progress-entries/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Progress entry not found with id: 99"))
                .when(progressEntryService).delete(99L);

        mockMvc.perform(delete("/api/progress-entries/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
