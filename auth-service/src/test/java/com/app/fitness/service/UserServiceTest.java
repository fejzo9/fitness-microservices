package com.app.fitness.service;

import com.app.fitness.dto.UserRequest;
import com.app.fitness.dto.UserResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.UserMapper;
import com.app.fitness.repository.RoleRepository;
import com.app.fitness.repository.UserRepository;
import com.fitness.authservice.model.Role;
import com.fitness.authservice.model.User;
import java.time.LocalDateTime;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private final Role adminRole = Role.builder().id(1L).name("ADMIN").build();

    @Test
    void findAll_shouldReturnMappedList() {
        User user = User.builder().id(1L).username("admin1").role(adminRole).build();
        UserResponse response = new UserResponse(1L, "admin1", "admin1@test.com", "ADMIN", LocalDateTime.now());
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("admin1");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withDuplicateUsername_shouldThrowDuplicateResourceException() {
        UserRequest request = new UserRequest("admin1", "admin1@test.com", "hash", 1L);
        when(userRepository.existsByUsername("admin1")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("admin1");
    }

    @Test
    void create_whenRoleNotFound_shouldThrowResourceNotFoundException() {
        UserRequest request = new UserRequest("newuser", "new@test.com", "hash", 99L);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        UserRequest request = new UserRequest("newuser", "new@test.com", "hash", 1L);
        User entity = User.builder().username("newuser").build();
        User saved = User.builder().id(10L).username("newuser").role(adminRole).createdAt(LocalDateTime.now()).build();
        UserResponse response = new UserResponse(10L, "newuser", "new@test.com", "ADMIN", LocalDateTime.now());

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(response);

        UserResponse result = userService.create(request);

        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepository).save(entity);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }
}
