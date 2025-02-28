package com.example.clouddisk.service;

import com.example.clouddisk.mapper.FileMetaDataMapper;
import com.example.clouddisk.model.FileMetaData;
import com.example.clouddisk.model.User;
import com.example.clouddisk.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.createDirectories;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final FileMetaDataMapper fileMetaDataMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisService redisService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public UserService(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder,
                       FileMetaDataMapper fileMetaDataMapper, RedisService redisService) {
        this.userMapper = userMapper;
        this.fileMetaDataMapper = fileMetaDataMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
    }

    public String register(String username, String rawPassword) throws IOException {

        if (userMapper.findByUsername(username) != null) {
            return "User already exists";
        }

        String password = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");
        userMapper.insertUser(user);

        Long userId = user.getId();
        // Create root directory for the newly registered user
        String rootDirectoryPath = uploadDir + "/user" + userId;
        FileMetaData userRootDirectory = new FileMetaData();
        userRootDirectory.setFileName("user" + userId);
        userRootDirectory.setUserId(userId);
        userRootDirectory.setFilePath(rootDirectoryPath);
        userRootDirectory.setRoot(true);
        fileMetaDataMapper.insertDirectory(userRootDirectory);

        Path path = Paths.get(rootDirectoryPath);
        createDirectories(path);

        return "Register success";
    }

    public User login(String username, String rawPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        redisService.set("user_id", user.getId());
        redisService.set("user_role", user.getRole());

        return user;
    }

    public void logout() {
        redisService.delete("user_id");
        redisService.delete("user_role");
    }
}
