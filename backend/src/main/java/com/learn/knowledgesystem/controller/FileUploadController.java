package com.learn.knowledgesystem.controller;

import com.learn.knowledgesystem.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${app.upload.image-dir}")
    private String imageDir;

    @Value("${app.upload.file-dir}")
    private String fileDir;


    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path imagePath = Paths.get(imageDir, filename);
            Path filePathInFile = Paths.get(fileDir, filename);
            Path filePath = null;

            if (Files.exists(imagePath)) {
                filePath = imagePath;
            } else if (Files.exists(filePathInFile)) {
                filePath = filePathInFile;
            } else {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 修正：使用 probeContentType 而不是 probeContentPath
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            if (contentType.startsWith("text/")) {
                contentType += ";charset=UTF-8";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }


    /**
     * 上传图片（自动判断是否为图片）
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, imageDir, "image");
    }

    /**
     * 上传其他文件（音频、视频、文档等）
     */
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, fileDir, "file");
    }

    private ApiResponse<String> uploadFile(MultipartFile file, String dir, String type) {
        try {
            // 创建目录（如果不存在）
            File directory = new File(dir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名，避免重名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + suffix;
            Path filePath = Paths.get(dir, filename);
            Files.write(filePath, file.getBytes());

            // 生成访问 URL（前端通过代理访问）
            String url = "/uploads/" + (type.equals("image") ? "images/" : "files/") + filename;
            return ApiResponse.success("上传成功", url);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error(500, "文件上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/image")
    public ApiResponse<?> deleteImage(@RequestParam("url") String imageUrl) {
        // 从URL中提取文件名，例如 /uploads/images/xxx.png -> xxx.png
        String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        Path filePath = Paths.get(imageDir, fileName);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error(404, "文件不存在");
            }
        } catch (IOException e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
//    批量删除方法
    @DeleteMapping("/images")
    public ApiResponse<?> deleteImages(@RequestBody List<String> urls) {
        for (String url : urls) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            Path filePath = Paths.get(imageDir, fileName);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 单个失败不影响其他删除
                System.err.println("删除失败: " + filePath);
            }
        }
        return ApiResponse.success("删除完成", null);
    }

    @DeleteMapping("/files")
    public ApiResponse<?> deleteFiles(@RequestBody List<String> urls) {
        for (String url : urls) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            Path filePath = null;
            if (url.contains("/images/")) {
                filePath = Paths.get(imageDir, fileName);
            } else if (url.contains("/files/")) {
                filePath = Paths.get(fileDir, fileName);
            }
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("删除失败: " + filePath);
                }
            }
        }
        return ApiResponse.success("清理完成", null);
    }

//    用于前端附件删除
    @DeleteMapping("/file")
    public ApiResponse<?> deleteFile(@RequestParam("url") String fileUrl) {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            Path filePath = null;
            if (fileUrl.contains("/images/")) {
                filePath = Paths.get(imageDir, fileName);
            } else if (fileUrl.contains("/files/")) {
                filePath = Paths.get(fileDir, fileName);
            }
            if (filePath != null) {
                boolean deleted = Files.deleteIfExists(filePath);
                if (deleted) {
                    return ApiResponse.success("删除成功", null);
                } else {
                    return ApiResponse.error(404, "文件不存在");
                }
            } else {
                return ApiResponse.error(400, "无效的文件URL");
            }
        } catch (IOException e) {
            return ApiResponse.error(500, "删除失败：" + e.getMessage());
        }
    }

}