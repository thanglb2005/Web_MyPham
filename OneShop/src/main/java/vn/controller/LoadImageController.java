package vn.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoadImageController {

    @Value("${upload.path}")
    private String pathUploadImage;

    @GetMapping(value = "/loadImage")
    @ResponseBody
    public byte[] index(@RequestParam(value = "imageName") String imageName, HttpServletResponse response) {
        try {
        System.out.println("=== LoadImage request for: " + imageName + " ===");
        System.out.println("Upload path: " + pathUploadImage);
        
        // Set content type based on file extension
        String contentType = "image/jpeg"; // default
        if (imageName.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        } else if (imageName.toLowerCase().endsWith(".gif")) {
            contentType = "image/gif";
        } else if (imageName.toLowerCase().endsWith(".webp")) {
            contentType = "image/webp";
        }
        response.setContentType(contentType);
        System.out.println("Content-Type: " + contentType);
        // Build absolute path to handle working directory issues
        String workingDir = System.getProperty("user.dir");
        File file = new File(workingDir + File.separatorChar + pathUploadImage + File.separatorChar + imageName);
        System.out.println("Working dir: " + workingDir);
        System.out.println("Looking for file at: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        
        // Debug: List all files in upload directory
        File uploadDir = new File(workingDir + File.separatorChar + pathUploadImage);
        System.out.println("Upload directory: " + uploadDir.getAbsolutePath());
        System.out.println("Upload directory exists: " + uploadDir.exists());
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            File[] files = uploadDir.listFiles();
            System.out.println("Files in upload directory:");
            if (files != null) {
                for (File f : files) {
                    System.out.println("  - " + f.getName());
                }
            }
        }
        
        InputStream inputStream = null;
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(file);
                if (inputStream != null) {
                    return IOUtils.toByteArray(inputStream);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("File not found or could not read");
        
        // Return empty byte array instead of null to avoid conversion errors
        return new byte[0];
        
        } catch (Exception e) {
            System.out.println("ERROR in LoadImageController: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }
}
