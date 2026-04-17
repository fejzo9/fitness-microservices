package com.app.fitness.service;

import com.app.fitness.dto.RoleRequest;
import com.app.fitness.dto.RoleResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.RoleMapper;
import com.app.fitness.repository.RoleRepository;
import com.fitness.authservice.model.Role;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    @Test
    void findAll_shouldReturnMappedResponses() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        RoleResponse response = new RoleResponse(1L, "ADMIN");
        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(roleMapper.toResponse(role)).thenReturn(response);

        List<RoleResponse> result = roleService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ADMIN");
    }

    @Test
    void findById_whenExists_shouldReturnResponse() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        RoleResponse response = new RoleResponse(1L, "ADMIN");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(response);

        RoleResponse result = roleService.findById(1L);

        assertThat(result.getName()).isEqualTo("ADMIN");
    }

    @Test
    void findById_whenNotFound_shouldThrowResourceNotFoundException() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_withNewName_shouldSaveAndReturnResponse() {
        RoleRequest request = new RoleRequest("MANAGER");
        Role role = Role.builder().name("MANAGER").build();
        Role saved = Role.builder().id(5L).name("MANAGER").build();
        RoleResponse response = new RoleResponse(5L, "MANAGER");

        when(roleRepository.existsByName("MANAGER")).thenReturn(false);
        when(roleMapper.toEntity(request)).thenReturn(role);
        when(roleRepository.save(role)).thenReturn(saved);
        when(roleMapper.toResponse(saved)).thenReturn(response);

        RoleResponse result = roleService.create(request);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("MANAGER");
        verify(roleRepository).save(role);
    }

    @Test
    void create_withExistingName_shouldThrowDuplicateResourceException() {
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> roleService.create(new RoleRequest("ADMIN")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ADMIN");
    }

    @Test
    void update_whenNotFound_shouldThrowResourceNotFoundException() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.update(99L, new RoleRequest("X")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(roleRepository.existsById(1L)).thenReturn(true);

        roleService.delete(1L);

        verify(roleRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotFound_shouldThrowResourceNotFoundException() {
        when(roleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> roleService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
