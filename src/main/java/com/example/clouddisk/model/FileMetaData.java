package com.example.clouddisk.model;

import lombok.Data;

@Data
public class FileMetaData {
    private Long id;
    private String fileName;
    private String filePath;
    private Long userId;
    private Long size;
    private String createdAt;
    private String updatedAt;
    private boolean isDirectory;
    private Long parentId;
}
