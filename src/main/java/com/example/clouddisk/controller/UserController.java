package com.example.clouddisk.controller;

import com.example.clouddisk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String register(String username, String password) {
        return userService.register(username, password);
    }

    @PostMapping("/login")
    public String login(String username, String password) {
        return userService.login(username, password);
    }
}
