package com.costrip.costrip_backend.service.impl;

import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.exception.ResourceNotFoundException;
import com.costrip.costrip_backend.repository.UserRepository;
import com.costrip.costrip_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}