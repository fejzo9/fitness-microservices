package com.app.fitness.controller;

import com.app.fitness.dto.TrainerClientRequest;
import com.app.fitness.dto.TrainerClientResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.TrainerClientService;
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
class TrainerClientControllerTest extends ControllerTestSupport {

    @Mock
    private TrainerClientService trainerClientService;

    @InjectMocks
    private TrainerClientController trainerClientController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(trainerClientController);
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(trainerClientService.findAll()).thenReturn(List.of(
                new TrainerClientResponse(1L, 10L, 20L, LocalDate.of(2026, 1, 1), "ACTIVE")));

        mockMvc.perform(get("/api/trainer-clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getById_whenRelationshipExists_shouldReturnRelationship() throws Exception {
        when(trainerClientService.findById(1L))
                .thenReturn(new TrainerClientResponse(1L, 10L, 20L, LocalDate.of(2026, 1, 1), "ACTIVE"));

        mockMvc.perform(get("/api/trainer-clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerId").value(10))
                .andExpect(jsonPath("$.clientId").value(20));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(trainerClientService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Trainer-client relationship not found with id: 99"));

        mockMvc.perform(get("/api/trainer-clients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        TrainerClientRequest request = new TrainerClientRequest(10L, 20L, LocalDate.of(2026, 1, 1), "ACTIVE");
        when(trainerClientService.create(any(TrainerClientRequest.class)))
                .thenReturn(new TrainerClientResponse(1L, 10L, 20L, LocalDate.of(2026, 1, 1), "ACTIVE"));

        mockMvc.perform(post("/api/trainer-clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        TrainerClientRequest request = new TrainerClientRequest(null, null, null, "");

        mockMvc.perform(post("/api/trainer-clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        TrainerClientRequest request = new TrainerClientRequest(10L, 20L, LocalDate.of(2026, 1, 1), "ACTIVE");
        when(trainerClientService.create(any(TrainerClientRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "Trainer-client relationship already exists for trainerId=10, clientId=20"));

        mockMvc.perform(post("/api/trainer-clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        TrainerClientRequest request = new TrainerClientRequest(10L, 20L, LocalDate.of(2026, 1, 1), "PAUSED");
        when(trainerClientService.update(eq(1L), any(TrainerClientRequest.class)))
                .thenReturn(new TrainerClientResponse(1L, 10L, 20L, LocalDate.of(2026, 1, 1), "PAUSED"));

        mockMvc.perform(put("/api/trainer-clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSED"));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        TrainerClientRequest request = new TrainerClientRequest(10L, 20L, LocalDate.of(2026, 1, 1), "PAUSED");
        when(trainerClientService.update(eq(99L), any(TrainerClientRequest.class)))
                .thenThrow(new ResourceNotFoundException("Trainer-client relationship not found with id: 99"));

        mockMvc.perform(put("/api/trainer-clients/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/trainer-clients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Trainer-client relationship not found with id: 99"))
                .when(trainerClientService).delete(99L);

        mockMvc.perform(delete("/api/trainer-clients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
