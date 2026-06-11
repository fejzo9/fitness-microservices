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
                RoleResponse.builder().id(1L).name("ADMIN").build(),
                RoleResponse.builder().id(2L).name("TRAINER").build(),
                RoleResponse.builder().id(3L).name("USER").build()));

        mockMvc.perform(get("/auth/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("ADMIN"))
                .andExpect(jsonPath("$[1].name").value("TRAINER"))
                .andExpect(jsonPath("$[2].name").value("USER"));
    }

    @Test
    void getById_whenAdminRoleExists_shouldReturnAdmin() throws Exception {
        when(roleService.findById(1L)).thenReturn(RoleResponse.builder().id(1L).name("ADMIN").build());

        mockMvc.perform(get("/auth/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    void getById_whenTrainerRoleExists_shouldReturnTrainer() throws Exception {
        when(roleService.findById(2L)).thenReturn(RoleResponse.builder().id(2L).name("TRAINER").build());

        mockMvc.perform(get("/auth/roles/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("TRAINER"));
    }

    @Test
    void getById_whenUserRoleExists_shouldReturnUser() throws Exception {
        when(roleService.findById(3L)).thenReturn(RoleResponse.builder().id(3L).name("USER").build());

        mockMvc.perform(get("/auth/roles/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("USER"));
    }

    @Test
    void getById_whenRoleDoesNotExist_shouldReturn404() throws Exception {
        when(roleService.findById(99L)).thenThrow(new ResourceNotFoundException("Role not found with id: 99"));

        mockMvc.perform(get("/auth/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_whenAdminAlreadyExists_shouldReturn409() throws Exception {
        RoleRequest request = RoleRequest.builder().name("ADMIN").build();
        when(roleService.create(any(RoleRequest.class)))
                .thenThrow(new DuplicateResourceException("Role already exists with name: ADMIN"));

        mockMvc.perform(post("/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void create_whenTrainerAlreadyExists_shouldReturn409() throws Exception {
        RoleRequest request = RoleRequest.builder().name("TRAINER").build();
        when(roleService.create(any(RoleRequest.class)))
                .thenThrow(new DuplicateResourceException("Role already exists with name: TRAINER"));

        mockMvc.perform(post("/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void create_whenUserAlreadyExists_shouldReturn409() throws Exception {
        RoleRequest request = RoleRequest.builder().name("USER").build();
        when(roleService.create(any(RoleRequest.class)))
                .thenThrow(new DuplicateResourceException("Role already exists with name: USER"));

        mockMvc.perform(post("/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturnUpdatedRole() throws Exception {
        RoleRequest request = RoleRequest.builder().name("ADMIN").build();
        when(roleService.update(eq(1L), any(RoleRequest.class))).thenReturn(RoleResponse.builder().id(1L).name("ADMIN").build());

        mockMvc.perform(put("/auth/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    void update_whenRoleDoesNotExist_shouldReturn404() throws Exception {
        RoleRequest request = RoleRequest.builder().name("ADMIN").build();
        when(roleService.update(eq(99L), any(RoleRequest.class)))
                .thenThrow(new ResourceNotFoundException("Role not found with id: 99"));

        mockMvc.perform(put("/auth/roles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenRoleExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/auth/roles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenRoleDoesNotExist_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Role not found with id: 99")).when(roleService).delete(99L);

        mockMvc.perform(delete("/auth/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}