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
    @Update("UPDATE file_metadata SET file_name = #{fileName}, file_path = #{filePath}, size = #{size}, updated_at = NOW() WHERE id = #{id}")
    void update(FileMetaData fileMetaData);

    // Insert directory
    @Insert("INSERT INTO file_metadata (file_name, file_path, user_id, created_at, updated_at, is_directory, parent_id, is_root) VALUES (#{fileName}, #{filePath}, #{userId}, NOW(), NOW(), TRUE, #{parentId}, #{isRoot})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertDirectory(FileMetaData fileMetaData);

    // Rename file or directory
    @Update("UPDATE file_metadata SET file_name = #{fileName}, file_path = #{filePath}, updated_at = NOW() WHERE id = #{id}")
    void rename(FileMetaData fileMetaData);

    // Move file or directory
    @Update("UPDATE file_metadata SET parent_id = #{parentId}, file_path = #{filePath}, updated_at = NOW() WHERE id = #{id}")
    void move(FileMetaData fileMetaData);

    // Update paths for all children when moving or renaming a directory - optimized version
    @Update("WITH RECURSIVE file_tree AS (" +
            "  SELECT id, file_path FROM file_metadata WHERE parent_id = #{id} " +
            "  UNION ALL " +
            "  SELECT f.id, f.file_path FROM file_metadata f " +
            "  INNER JOIN file_tree ft ON f.parent_id = ft.id" +
            ") " +
            "UPDATE file_metadata SET " +
            "  file_path = CONCAT(#{newPath}, SUBSTRING(file_path, LENGTH(#{oldPath}) + 1)), " +
            "  updated_at = NOW() " +
            "WHERE id IN (SELECT id FROM file_tree)")
    void updateChildrenPaths(@Param("id") Long id, @Param("oldPath") String oldPath, @Param("newPath") String newPath);
}
