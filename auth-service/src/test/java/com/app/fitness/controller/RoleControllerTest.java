package com.app.fitness.controller;

import com.app.fitness.dto.RoleRequest;
import com.app.fitness.dto.RoleResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.RoleService;
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
class RoleControllerTest extends ControllerTestSupport {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(roleController);
    }

    @Test
    void getAll_shouldReturnListOfRoles() throws Exception {
        when(roleService.findAll()).thenReturn(List.of(
                new RoleResponse(1L, "ADMIN"),
                new RoleResponse(2L, "USER")));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    void getById_whenRoleExists_shouldReturnRole() throws Exception {
        when(roleService.findById(1L)).thenReturn(new RoleResponse(1L, "ADMIN"));

        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    void getById_whenRoleDoesNotExist_shouldReturn404() throws Exception {
        when(roleService.findById(99L)).thenThrow(new ResourceNotFoundException("Role not found with id: 99"));

        mockMvc.perform(get("/api/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturnCreatedRole() throws Exception {
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
    void create_withBlankName_shouldReturn400() throws Exception {
        RoleRequest request = new RoleRequest("");

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenRoleAlreadyExists_shouldReturn409() throws Exception {
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
    void update_withValidRequest_shouldReturnUpdatedRole() throws Exception {
        RoleRequest request = new RoleRequest("COACH");
        when(roleService.update(eq(1L), any(RoleRequest.class))).thenReturn(new RoleResponse(1L, "COACH"));

        mockMvc.perform(put("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("COACH"));
    }

    @Test
    void update_whenRoleDoesNotExist_shouldReturn404() throws Exception {
        RoleRequest request = new RoleRequest("COACH");
        when(roleService.update(eq(99L), any(RoleRequest.class)))
                .thenThrow(new ResourceNotFoundException("Role not found with id: 99"));

        mockMvc.perform(put("/api/roles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenRoleExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenRoleDoesNotExist_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Role not found with id: 99")).when(roleService).delete(99L);

        mockMvc.perform(delete("/api/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
