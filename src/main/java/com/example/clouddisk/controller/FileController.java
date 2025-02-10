package com.example.clouddisk.controller;

import com.example.clouddisk.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam Long dirId) {
        try{
            return ResponseEntity.ok(fileService.listDirContents(dirId));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file, @RequestParam Long parentId){
        try{
            fileService.uploadFile(file, parentId);
            return ResponseEntity.ok("Upload successful");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createDir(@RequestParam String name, @RequestParam Long parentId){
        try{
            fileService.createDir(name, parentId);
            return ResponseEntity.ok("Create successful");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fail to create directory: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam Long fileId){
        try{
            return fileService.downloadFile(fileId);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Download failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam Long fileId){
        try{
            fileService.deleteFileById(fileId);
            return ResponseEntity.ok("Delete successful");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Delete failed: " + e.getMessage());
        }
    }

    @PostMapping("/rename")
    public ResponseEntity<String> renameFile(@RequestParam Long fileId, @RequestParam String newName){
        try{
            fileService.renameFile(fileId, newName);
            return ResponseEntity.ok("Rename successful");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rename failed: " + e.getMessage());
        }
    }

    @PostMapping("/move")
    public ResponseEntity<String> moveFile(@RequestParam Long fileId, @RequestParam Long newParentId){
        try{
            fileService.moveFile(fileId, newParentId);
            return ResponseEntity.ok("Move successful");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Move failed: " + e.getMessage());
        }
    }

}
