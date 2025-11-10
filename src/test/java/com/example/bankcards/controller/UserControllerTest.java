package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    private User user;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(User.Role.USER);

        userPrincipal = new UserPrincipal(1L, "testuser", "password", null);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserDto() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setRole(User.Role.ADMIN);

        when(userService.getAllUsers()).thenReturn(List.of(user, user2));

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().get(1).getRole()).isEqualTo(User.Role.ADMIN);

        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_ShouldReturnUserDto() {
        when(userService.getUserById(1L)).thenReturn(user);

        ResponseEntity<UserDto> response = userController.getUserById(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");

        verify(userService).getUserById(1L);
    }

    @Test
    void getCurrentUser_ShouldReturnUserDto() {
        when(userService.getUserById(1L)).thenReturn(user);

        ResponseEntity<UserDto> response = userController.getCurrentUser(userPrincipal);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");

        verify(userService).getUserById(1L);
    }

    @Test
    void createUser_ShouldReturnCreatedUserDto() {
        UserDto requestDto = new UserDto();
        requestDto.setUsername("newuser");
        requestDto.setPassword("pass");
        requestDto.setRole(User.Role.ADMIN);

        User createdUser = new User();
        createdUser.setId(2L);
        createdUser.setUsername("newuser");
        createdUser.setRole(User.Role.ADMIN);

        when(userService.registerUser("newuser", "pass", User.Role.ADMIN)).thenReturn(createdUser);

        ResponseEntity<UserDto> response = userController.createUser(requestDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getId()).isEqualTo(2L);
        assertThat(response.getBody().getRole()).isEqualTo(User.Role.ADMIN);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User.Role> roleCaptor = ArgumentCaptor.forClass(User.Role.class);

        verify(userService).registerUser(usernameCaptor.capture(), passwordCaptor.capture(), roleCaptor.capture());
        assertThat(usernameCaptor.getValue()).isEqualTo("newuser");
        assertThat(passwordCaptor.getValue()).isEqualTo("pass");
        assertThat(roleCaptor.getValue()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserDto() {
        UserDto requestDto = new UserDto();
        requestDto.setUsername("updateduser");
        requestDto.setPassword("newpass");
        requestDto.setRole(User.Role.ADMIN);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setRole(User.Role.ADMIN);

        when(userService.updateUser(1L, "updateduser", "newpass", User.Role.ADMIN)).thenReturn(updatedUser);

        ResponseEntity<UserDto> response = userController.updateUser(1L, requestDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getUsername()).isEqualTo("updateduser");
        assertThat(response.getBody().getRole()).isEqualTo(User.Role.ADMIN);

        verify(userService).updateUser(1L, "updateduser", "newpass", User.Role.ADMIN);
    }

    @Test
    void deleteUser_ShouldReturnNoContent() {
        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getStatusCodeValue()).isEqualTo(204);

        verify(userService).deleteUser(1L);
    }
}
