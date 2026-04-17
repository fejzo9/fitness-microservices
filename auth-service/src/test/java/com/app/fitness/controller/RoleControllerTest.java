package com.app.fitness.controller;

import com.app.fitness.dto.RoleRequest;
import com.app.fitness.dto.RoleResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.GlobalExceptionHandler;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import(GlobalExceptionHandler.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    @Test
    void getAll_shouldReturnListOfRoles() throws Exception {
        List<RoleResponse> roles = List.of(
                new RoleResponse(1L, "ADMIN"),
                new RoleResponse(2L, "USER"));
        when(roleService.findAll()).thenReturn(roles);

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    void getById_whenExists_shouldReturnRole() throws Exception {
        when(roleService.findById(1L)).thenReturn(new RoleResponse(1L, "ADMIN"));

        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(roleService.findById(99L)).thenThrow(new ResourceNotFoundException("Role not found with id: 99"));

        mockMvc.perform(get("/api/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        RoleRequest request = new RoleRequest("MANAGER");
        when(roleService.create(any(RoleRequest.class))).thenReturn(new RoleResponse(5L, "MANAGER"));

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("MANAGER"));
    }

    @Test
    void create_withBlankName_shouldReturn400WithValidationError() throws Exception {
        RoleRequest request = new RoleRequest("");

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        RoleRequest request = new RoleRequest("ADMIN");
        when(roleService.create(any(RoleRequest.class)))
                .thenThrow(new DuplicateResourceException("Role already exists with name: ADMIN"));

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        RoleRequest request = new RoleRequest("SUPER_ADMIN");
        when(roleService.update(eq(1L), any(RoleRequest.class)))
                .thenReturn(new RoleResponse(1L, "SUPER_ADMIN"));

        mockMvc.perform(put("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SUPER_ADMIN"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Role not found with id: 99")).when(roleService).delete(99L);

        mockMvc.perform(delete("/api/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
