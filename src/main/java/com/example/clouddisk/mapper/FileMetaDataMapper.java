package com.example.clouddisk.mapper;

import com.example.clouddisk.model.FileMetaData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FileMetaDataMapper {
    // Query all files
    @Select("SELECT * FROM file_metadata")
    List<FileMetaData> findAll();

    // Query all files belong to a user
    @Select("SELECT * FROM file_metadata WHERE user_id = #{userId}")
    List<FileMetaData> findByUserId(Long userId);
}
