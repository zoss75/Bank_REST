package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role createRole(String name) {
        if (roleRepository.findByName(name).isPresent())
            throw new IllegalArgumentException("Role already exists");
        return roleRepository.save(new Role(null, name));
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }
}