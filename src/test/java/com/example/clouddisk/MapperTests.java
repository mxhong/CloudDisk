package com.example.clouddisk;

import com.example.clouddisk.mapper.UserMapper;
import com.example.clouddisk.model.FileMetaData;
import com.example.clouddisk.model.User;
import com.example.clouddisk.mapper.FileMetaDataMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileMetaDataMapper fileMetaDataMapper;

    @Test
    public void testUserMapper() {
        List<User> Users = userMapper.findAll();
        System.out.println(Users);

//        String userName = "mxhong";
//        User user = userMapper.findByUsername(userName);
//        System.out.println(user.getUsername());
        User user = new User();
        user.setUsername("mxhong");
        System.out.println(user.getUsername());
    }

    @Test
    public void testFileMetaDataMapper() {
//        List<FileMetaData> fileMetaDataList = fileMetaDataMapper.findAll();
//        System.out.println(fileMetaDataList);
//
//        Long userId = 1L;
//        List<FileMetaData> filesByUserId = fileMetaDataMapper.findByUserId(userId);
//        System.out.println(filesByUserId);
    }
}
