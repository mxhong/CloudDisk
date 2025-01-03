package com.example.clouddisk.model;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String role;
    private String createdAt;
}
