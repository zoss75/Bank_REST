package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private User.Role role;
}