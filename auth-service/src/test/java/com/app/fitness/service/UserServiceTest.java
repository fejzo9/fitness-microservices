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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private final Role adminRole = Role.builder().id(1L).name("ADMIN").build();
    private final Role trainerRole = Role.builder().id(2L).name("TRAINER").build();
    private final Role userRole = Role.builder().id(3L).name("USER").build();

    @Test
    void findAll_shouldReturnMappedList() {
        User admin = User.builder().id(1L).username("admin1").role(adminRole).build();
        User trainer = User.builder().id(2L).username("trainer1").role(trainerRole).build();
        User user = User.builder().id(3L).username("user1").role(userRole).build();

        UserResponse adminResponse = UserResponse.builder().id(1L).username("admin1").email("admin1@test.com").roleName("ADMIN").createdAt(LocalDateTime.now()).build();
        UserResponse trainerResponse = UserResponse.builder().id(2L).username("trainer1").email("trainer1@test.com").roleName("TRAINER").createdAt(LocalDateTime.now()).build();
        UserResponse userResponse = UserResponse.builder().id(3L).username("user1").email("user1@test.com").roleName("USER").createdAt(LocalDateTime.now()).build();

        when(userRepository.findAll()).thenReturn(List.of(admin, trainer, user));
        when(userMapper.toResponse(admin)).thenReturn(adminResponse);
        when(userMapper.toResponse(trainer)).thenReturn(trainerResponse);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getUsername()).isEqualTo("admin1");
        assertThat(result.get(1).getUsername()).isEqualTo("trainer1");
        assertThat(result.get(2).getUsername()).isEqualTo("user1");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withDuplicateUsername_shouldThrowDuplicateResourceException() {
        UserRequest request = UserRequest.builder().username("admin1").email("admin1@test.com").password("hash").roleId(1L).build();
        when(userRepository.existsByUsername("admin1")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("admin1");
    }

    @Test
    void create_whenRoleNotFound_shouldThrowResourceNotFoundException() {
        UserRequest request = UserRequest.builder().username("newuser").email("new@test.com").password("hash").roleId(99L).build();
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        UserRequest request = UserRequest.builder().username("newuser").email("new@test.com").password("hash").roleId(3L).build();
        User entity = User.builder().username("newuser").build();
        User saved = User.builder().id(10L).username("newuser").role(userRole).createdAt(LocalDateTime.now()).build();
        UserResponse response = UserResponse.builder().id(10L).username("newuser").email("new@test.com").roleName("USER").createdAt(LocalDateTime.now()).build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findById(3L)).thenReturn(Optional.of(userRole));
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(response);

        UserResponse result = userService.create(request);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getRoleName()).isEqualTo("USER");
        verify(userRepository).save(entity);
    }

    @Test
    void create_trainerRole_shouldSaveAndReturn() {
        UserRequest request = UserRequest.builder().username("newtrainer").email("newtrainer@test.com").password("hash").roleId(2L).build();
        User entity = User.builder().username("newtrainer").build();
        User saved = User.builder().id(11L).username("newtrainer").role(trainerRole).createdAt(LocalDateTime.now()).build();
        UserResponse response = UserResponse.builder().id(11L).username("newtrainer").email("newtrainer@test.com").roleName("TRAINER").createdAt(LocalDateTime.now()).build();

        when(userRepository.existsByUsername("newtrainer")).thenReturn(false);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(trainerRole));
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toResponse(saved)).thenReturn(response);

        UserResponse result = userService.create(request);

        assertThat(result.getUsername()).isEqualTo("newtrainer");
        assertThat(result.getRoleName()).isEqualTo("TRAINER");
        verify(userRepository).save(entity);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        User user = User.builder().id(1L).username("user1").role(userRole).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_withValidRequest_shouldReturnUpdatedUser() {
        UserRequest request = UserRequest.builder().username("updateduser").email("updated@test.com").password("newhash").roleId(2L).build();
        User existing = User.builder().id(1L).username("olduser").role(userRole).build();
        User updated = User.builder().id(1L).username("updateduser").role(trainerRole).build();
        UserResponse response = UserResponse.builder().id(1L).username("updateduser").email("updated@test.com").roleName("TRAINER").createdAt(LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(trainerRole));
        when(userRepository.save(any(User.class))).thenReturn(updated);
        when(userMapper.toResponse(updated)).thenReturn(response);

        UserResponse result = userService.update(1L, request);

        assertThat(result.getUsername()).isEqualTo("updateduser");
        assertThat(result.getRoleName()).isEqualTo("TRAINER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_whenUserDoesNotExist_shouldThrow() {
        UserRequest request = UserRequest.builder().username("updateduser").email("updated@test.com").password("newhash").roleId(1L).build();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfile_withValidRequest_shouldReturnUpdatedUser() {
        com.app.fitness.dto.UserProfileRequest request = new com.app.fitness.dto.UserProfileRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAge(30);
        request.setHeight(180);
        request.setWeight(75);
        request.setGender("MALE");

        User existing = User.builder().id(1L).username("user1").role(userRole).build();
        User updated = User.builder().id(1L).username("user1").role(userRole).firstName("John").lastName("Doe").age(30).height(180).weight(75).gender("MALE").build();
        UserResponse response = UserResponse.builder().id(1L).username("user1").email("user1@test.com").roleName("USER").firstName("John").lastName("Doe").age(30).height(180).weight(75).gender("MALE").createdAt(LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(updated);
        when(userMapper.toResponse(updated)).thenReturn(response);

        UserResponse result = userService.updateProfile(1L, request);

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getAge()).isEqualTo(30);
        verify(userRepository).save(any(User.class));
    }
}
