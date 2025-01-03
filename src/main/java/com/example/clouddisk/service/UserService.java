package com.example.clouddisk.service;

import com.example.clouddisk.model.User;
import com.example.clouddisk.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(String username, String rawPassword) {

        if (userMapper.findByUsername(username) != null) {
            return "User already exists";
        }

        String password = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");

        userMapper.insertUser(user);
        return "Register success";
    }

    public String login(String username, String rawPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return "User not found";
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return "Wrong password";
        }
        return "Login success";
    }

}
