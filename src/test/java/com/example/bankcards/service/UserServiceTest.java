package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerUser_ShouldSaveNewUser() {
        String username = "newuser";
        String password = "pass";
        User.Role role = User.Role.ADMIN;

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.registerUser(username, password, role);

        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo("encodedPass");
        assertThat(result.getRole()).isEqualTo(role);

        verify(userRepository).save(result);
    }

    @Test
    void registerUser_ShouldThrow_WhenUsernameExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("existing", "pass", User.Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2).contains(user1, user2);
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_ShouldReturnUser() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with id");
    }

    @Test
    void updateUser_ShouldChangeUsernamePasswordRole() {
        User user = new User();
        user.setId(1L);
        user.setUsername("old");
        user.setPassword("oldpass");
        user.setRole(User.Role.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("new")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.updateUser(1L, "new", "newpass", User.Role.ADMIN);

        assertThat(updated.getUsername()).isEqualTo("new");
        assertThat(updated.getPassword()).isEqualTo("encodedNewPass");
        assertThat(updated.getRole()).isEqualTo(User.Role.ADMIN);

        verify(userRepository).save(updated);
    }

    @Test
    void updateUser_ShouldThrow_WhenUsernameExists() {
        User user = new User();
        user.setId(1L);
        user.setUsername("old");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, "existing", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void deleteUser_ShouldCallRepositoryDelete() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
