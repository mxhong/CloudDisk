package com.example.clouddisk.mapper;

import com.example.clouddisk.model.FileMetaData;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FileMetaDataMapper {
    // Query all files
    @Select("SELECT * FROM file_metadata")
    List<FileMetaData> findAll();

    // Query all files belong to a user
    @Select("SELECT * FROM file_metadata WHERE user_id = #{userId}")
    List<FileMetaData> findByUserId(Long userId);

    // Query file by path
    @Select("SELECT * FROM file_metadata WHERE file_path = #{path}")
    FileMetaData findByPath(String path);

    // Query file by ID
    @Select("SELECT * FROM file_metadata WHERE id = #{id}")
    FileMetaData findById(Long id);

    // Show directory contents
    @Select("SELECT * FROM file_metadata WHERE parent_id = #{id}")
    List<FileMetaData> listDirContents(Long id);

    // Delete file by ID
    @Delete("DELETE FROM file_metadata WHERE id = #{id}")
    void deleteById(Long id);

    // Insert new file metadata
    @Insert("INSERT INTO file_metadata (file_name, file_path, user_id, size, created_at, updated_at, is_directory, parent_id) VALUES (#{fileName}, #{filePath}, #{userId}, #{size}, NOW(), NOW(), #{isDirectory}, #{parentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FileMetaData fileMetaData);

    // Update existing file
    @Update("UPDATE file_metadata SET size = #{size}, updated_at = NOW() WHERE id = #{id}")
    void update(FileMetaData fileMetaData);

    // Insert directory
    @Insert("INSERT INTO file_metadata (file_name, file_path, user_id, created_at, updated_at, is_directory, parent_id) VALUES (#{fileName}, #{filePath}, #{userId}, NOW(), NOW(), TRUE, #{parentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertDirectory(FileMetaData fileMetaData);
}
