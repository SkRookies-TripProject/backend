package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.entity.User;

public interface UserService {
    User findByEmail(String email);
}