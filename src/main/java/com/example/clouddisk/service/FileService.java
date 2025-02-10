package com.example.clouddisk.service;

import com.example.clouddisk.mapper.FileMetaDataMapper;
import com.example.clouddisk.model.FileMetaData;
import com.example.clouddisk.security.JwtAuth;
import com.google.common.net.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.Files.createDirectories;

@Service
public class FileService {
    private final FileMetaDataMapper fileMetaDataMapper;

    @Autowired
    public FileService(FileMetaDataMapper fileMetaDataMapper) {
        this.fileMetaDataMapper = fileMetaDataMapper;
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getCurrentUserRole(){
        JwtAuth jwtAuth = (JwtAuth) SecurityContextHolder.getContext().getAuthentication();
        return jwtAuth.getRole();
    }

    public FileMetaData getFileById(Long fileId) {
        return fileMetaDataMapper.findById(fileId);
    }

    public List<FileMetaData> listDirContents(Long fileId) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        FileMetaData fileMetaData = fileMetaDataMapper.findById(fileId);

        if (fileMetaData == null){
            throw new RuntimeException("File does not exist");
        }
        if (!fileMetaData.isDirectory()){
            throw new RuntimeException("File is not a directory");
        }
        if (!fileMetaData.getUserId().equals(userId) && role.equals("USER")){
            throw new RuntimeException("Access denied");
        }

        return fileMetaDataMapper.listDirContents(fileId);
    }

    public void deleteFileById(Long fileId) throws IOException {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        FileMetaData fileMetaData = fileMetaDataMapper.findById(fileId);

        if (fileMetaData == null){
            throw new RuntimeException("File does not exist");
        }
        if (!fileMetaData.getUserId().equals(userId) && role.equals("USER")){
            throw new RuntimeException("Access denied");
        }

        Path path = Paths.get(fileMetaData.getFilePath());
        if (fileMetaData.isDirectory()){
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        else{
            Files.delete(path);
        }

        fileMetaDataMapper.deleteById(fileId);
    }

    public void createDir(String name, Long parentId)
            throws IOException{
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        FileMetaData parentDir = fileMetaDataMapper.findById(parentId);

        // Validate parent directory
        if (parentDir == null) {
            throw new RuntimeException("Parent directory is null");
        }
        if (!parentDir.isDirectory()){
            throw new RuntimeException("Parent directory is not a directory");
        }
        if (!parentDir.getUserId().equals(userId) && role.equals("USER")){
            throw new RuntimeException("Access denied");
        }

        String fullPath = parentDir.getFilePath() + '/' + name;

        FileMetaData fileMetaData = fileMetaDataMapper.findByPath(fullPath);
        if (fileMetaData == null) {
            Path path = Paths.get(fullPath);
            createDirectories(path);

            fileMetaData = new FileMetaData();
            fileMetaData.setFileName(name);
            fileMetaData.setFilePath(fullPath);
            fileMetaData.setUserId(userId);
            fileMetaData.setParentId(parentId);
            fileMetaDataMapper.insertDirectory(fileMetaData);
        }
        else{
            throw new RuntimeException("Directory already exists");
        }
    }

    public void uploadFile(MultipartFile file, Long parentId)
            throws IOException{

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("Filename is null");
        }

        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        FileMetaData parentDir = fileMetaDataMapper.findById(parentId);

        // Validate parent directory
        if (parentDir == null) {
            throw new RuntimeException("Parent directory is null");
        }
        if (!parentDir.isDirectory()){
            throw new RuntimeException("Parent directory is not a directory");
        }
        if (!parentDir.getUserId().equals(userId) && role.equals("USER")){
             throw new RuntimeException("Access denied");
        }

        String fullUploadPath = parentDir.getFilePath() + '/' + filename;
        Path path = Paths.get(fullUploadPath);
        Files.write(path, file.getBytes());

        FileMetaData fileMetaData = fileMetaDataMapper.findByPath(fullUploadPath);
        if (fileMetaData != null) { // File exists
            fileMetaData.setSize(file.getSize());
            fileMetaDataMapper.update(fileMetaData);
        }
        else{
            fileMetaData = new FileMetaData();
            fileMetaData.setFileName(filename);
            fileMetaData.setFilePath(fullUploadPath);
            fileMetaData.setUserId(userId);
            fileMetaData.setSize(file.getSize());
            fileMetaData.setDirectory(false);
            fileMetaData.setParentId(parentId);
            fileMetaDataMapper.insert(fileMetaData);
        }
    }

    public ResponseEntity<Resource> downloadFile(Long fileId) throws MalformedURLException {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        FileMetaData fileMetaData = fileMetaDataMapper.findById(fileId);

        if (fileMetaData == null){
            throw new RuntimeException("File does not exist");
        }
        if (fileMetaData.isDirectory()){
            throw new RuntimeException("File is a directory");
        }
        if (!fileMetaData.getUserId().equals(userId) && role.equals("USER")){
            throw new RuntimeException("Access denied");
        }

        Path path = Paths.get(fileMetaData.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        fileMetaData.getFileName() + "\"")
                .body(resource);
    }

    public void renameFile(Long fileId, String newName) throws IOException{
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        FileMetaData fileMetaData = fileMetaDataMapper.findById(fileId);

        if (fileMetaData == null){
            throw new RuntimeException("File does not exist");
        }
        if (!fileMetaData.getUserId().equals(userId) && role.equals("USER")){
            throw new RuntimeException("Access denied");
        }

        String oldPathStr = fileMetaData.getFilePath();
        String newPathStr = oldPathStr.replace(fileMetaData.getFileName(), newName);

        Path oldPath = Paths.get(oldPathStr);
        Path newPath = Paths.get(newPathStr);
        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

        fileMetaData.setFileName(newName);
        fileMetaData.setFilePath(newPathStr);
        fileMetaDataMapper.rename(fileMetaData);
        
        if (fileMetaData.isDirectory()){
            fileMetaDataMapper.updateChildrenPaths(fileId, oldPathStr, newPathStr);
        }        
    }

    public void moveFile(Long fileId, Long newParentId) throws IOException{

        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        FileMetaData newParentDir = fileMetaDataMapper.findById(newParentId);
        FileMetaData fileMetaData = fileMetaDataMapper.findById(fileId);

        if (fileMetaData == null){
            throw new RuntimeException("File does not exist");
        }
        if (!fileMetaData.getUserId().equals(userId) && role.equals("USER")){
            throw new RuntimeException("Access denied");
        }

        if (newParentDir == null){
            throw new RuntimeException("New parent directory is null");
        }
        if (!newParentDir.isDirectory()){
            throw new RuntimeException("New parent directory is not a directory");
        }
        if (!newParentDir.getUserId().equals(userId) && role.equals("USER")){
            throw new RuntimeException("Access denied");
        }

        String oldPathStr = fileMetaData.getFilePath();
        String newPathStr = newParentDir.getFilePath() + '/' + fileMetaData.getFileName();

        Path oldPath = Paths.get(oldPathStr);
        Path newPath = Paths.get(newPathStr);
        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);


        fileMetaData.setParentId(newParentId);
        fileMetaData.setFilePath(newPathStr);
        fileMetaDataMapper.move(fileMetaData);


        if (fileMetaData.isDirectory()){
            fileMetaDataMapper.updateChildrenPaths(fileId, oldPathStr, newPathStr);
        }
    }
}
