package vn.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.service.CloudinaryService;
import vn.service.StorageService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for chat-related API endpoints
 * @author OneShop Team
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private StorageService storageService;

    /**
     * Upload image for chat messages
     * @param file The image file to upload
     * @return Response with image URL or error
     */
    @PostMapping("/uploadImage")
    public ResponseEntity<?> uploadChatImage(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== CHAT IMAGE UPLOAD REQUEST ===");
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            
            // Validate file
            if (!cloudinaryService.validateImageFile(file)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "File không hợp lệ! Chỉ chấp nhận JPG, PNG, GIF, WebP và tối đa 10MB.");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Upload to Cloudinary
            String imageUrl = storageService.storeChatImage(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("url", imageUrl);
            response.put("original_filename", file.getOriginalFilename());
            response.put("size", file.getSize());
            
            System.out.println("Image uploaded successfully: " + imageUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error uploading chat image: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

