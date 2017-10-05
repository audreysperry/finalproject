package com.audreysperry.finalproject.services;

import com.audreysperry.finalproject.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User findByUsername(String username);
}
