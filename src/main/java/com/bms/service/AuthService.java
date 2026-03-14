package com.bms.service;

import com.bms.entity.User;

public interface AuthService {

    User register(User user);

    String login(String email, String password);
}