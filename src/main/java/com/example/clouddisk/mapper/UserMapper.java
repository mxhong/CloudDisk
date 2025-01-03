package com.example.clouddisk.mapper;

import com.example.clouddisk.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    // Query all users
    @Select("SELECT * FROM user")
    List<User> findAll();

    // Query user given username
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
}
