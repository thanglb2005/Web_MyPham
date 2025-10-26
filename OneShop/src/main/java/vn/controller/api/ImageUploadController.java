package vn.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.service.CloudinaryService;
import vn.service.StorageService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private StorageService storageService;

    /**
     * Upload single image (general)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (!cloudinaryService.validateImageFile(file)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "File không hợp lệ! Chỉ chấp nhận JPG, PNG, GIF, WebP và tối đa 10MB.");
                return ResponseEntity.badRequest().body(error);
            }
            
            String imageUrl = storageService.storeGeneralImage(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("url", imageUrl);
            response.put("original_filename", file.getOriginalFilename());
            response.put("size", file.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Upload product image
     */
    @PostMapping("/upload/product")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (!cloudinaryService.validateImageFile(file)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "File không hợp lệ! Chỉ chấp nhận JPG, PNG, GIF, WebP và tối đa 10MB.");
                return ResponseEntity.badRequest().body(error);
            }
            
            String imageUrl = storageService.storeProductImage(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("url", imageUrl);
            response.put("type", "product");
            response.put("original_filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload product image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Upload user avatar
     */
    @PostMapping("/upload/user")
    public ResponseEntity<?> uploadUserImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (!cloudinaryService.validateImageFile(file)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "File không hợp lệ! Chỉ chấp nhận JPG, PNG, GIF, WebP và tối đa 10MB.");
                return ResponseEntity.badRequest().body(error);
            }
            
            String imageUrl = storageService.storeUserImage(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("url", imageUrl);
            response.put("type", "user");
            response.put("original_filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload user image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Upload brand logo
     */
    @PostMapping("/upload/brand")
    public ResponseEntity<?> uploadBrandImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (!cloudinaryService.validateImageFile(file)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "File không hợp lệ! Chỉ chấp nhận JPG, PNG, GIF, WebP và tối đa 10MB.");
                return ResponseEntity.badRequest().body(error);
            }
            
            String imageUrl = storageService.storeBrandImage(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("url", imageUrl);
            response.put("type", "brand");
            response.put("original_filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload brand image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Upload image to specific folder
     */
    @PostMapping("/upload/{folder}")
    public ResponseEntity<?> uploadImageToFolder(
            @RequestParam("file") MultipartFile file,
            @PathVariable String folder) {
        try {
            Map<String, Object> result = cloudinaryService.uploadImage(file, folder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("public_id", result.get("public_id"));
            response.put("secure_url", result.get("secure_url"));
            response.put("folder", result.get("folder"));
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Upload image with specific dimensions
     */
    @PostMapping("/upload/{folder}/{width}/{height}")
    public ResponseEntity<?> uploadImageWithDimensions(
            @RequestParam("file") MultipartFile file,
            @PathVariable String folder,
            @PathVariable int width,
            @PathVariable int height) {
        try {
            Map<String, Object> result = cloudinaryService.uploadImage(file, folder, width, height);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("public_id", result.get("public_id"));
            response.put("secure_url", result.get("secure_url"));
            response.put("width", result.get("width"));
            response.put("height", result.get("height"));
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Delete image by URL
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteImageByUrl(@RequestParam("url") String imageUrl) {
        try {
            boolean deleted = storageService.deleteImage(imageUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Image deleted successfully" : "Failed to delete image");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Delete image by public ID (for direct Cloudinary access)
     */
    @DeleteMapping("/delete/{publicId}")
    public ResponseEntity<?> deleteImage(@PathVariable String publicId) {
        try {
            Map<String, Object> result = cloudinaryService.deleteImage(publicId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result.get("result"));
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get optimized image URL
     */
    @GetMapping("/url/{publicId}")
    public ResponseEntity<?> getOptimizedImageUrl(
            @PathVariable String publicId,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {
        
        try {
            String url = cloudinaryService.getOptimizedImageUrl(publicId, width, height);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("url", url);
            response.put("public_id", publicId);
            response.put("width", width);
            response.put("height", height);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to generate URL: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
