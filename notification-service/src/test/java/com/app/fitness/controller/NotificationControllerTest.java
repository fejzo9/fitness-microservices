package com.app.fitness.controller;

import com.app.fitness.dto.NotificationRequest;
import com.app.fitness.dto.NotificationResponse;
import com.app.fitness.exception.GlobalExceptionHandler;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getAll_shouldReturnList() throws Exception {
        List<NotificationResponse> notifications = List.of(
                new NotificationResponse(1L, 3L, "Welcome!", "INFO", false, LocalDateTime.now()));
        when(notificationService.findAll()).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("INFO"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(notificationService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Notification not found with id: 99"));

        mockMvc.perform(get("/api/notifications/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        NotificationRequest request = new NotificationRequest(3L, "Welcome!", "INFO", false);
        NotificationResponse response = new NotificationResponse(1L, 3L, "Welcome!", "INFO", false,
                LocalDateTime.now());
        when(notificationService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Welcome!"));
    }

    @Test
    void create_withMissingType_shouldReturn400() throws Exception {
        NotificationRequest request = new NotificationRequest(3L, "msg", null, false);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_withMissingMessage_shouldReturn400() throws Exception {
        NotificationRequest request = new NotificationRequest(3L, "", "INFO", false);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Notification not found with id: 99"))
                .when(notificationService).delete(99L);

        mockMvc.perform(delete("/api/notifications/99"))
                .andExpect(status().isNotFound());
    }
}
