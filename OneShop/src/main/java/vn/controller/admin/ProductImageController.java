package vn.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.entity.Product;
import vn.service.CloudinaryService;
import vn.service.ProductService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/products")
public class ProductImageController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Upload product image
     */
    @PostMapping("/{productId}/upload-image")
    @ResponseBody
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Check if product exists
            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Product not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Upload image to Cloudinary
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "products");
            
            // Update product with new image URL
            Product product = productOpt.get();
            product.setProductImage((String) uploadResult.get("secure_url"));
            productService.save(product);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("image_url", uploadResult.get("secure_url"));
            response.put("public_id", uploadResult.get("public_id"));
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Upload product image with specific dimensions
     */
    @PostMapping("/{productId}/upload-image/{width}/{height}")
    @ResponseBody
    public ResponseEntity<?> uploadProductImageWithDimensions(
            @PathVariable Long productId,
            @PathVariable int width,
            @PathVariable int height,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Check if product exists
            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Product not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Upload image with specific dimensions
            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "products", width, height);
            
            // Update product with new image URL
            Product product = productOpt.get();
            product.setProductImage((String) uploadResult.get("secure_url"));
            productService.save(product);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("image_url", uploadResult.get("secure_url"));
            response.put("public_id", uploadResult.get("public_id"));
            response.put("width", uploadResult.get("width"));
            response.put("height", uploadResult.get("height"));
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Delete product image
     */
    @DeleteMapping("/{productId}/delete-image")
    @ResponseBody
    public ResponseEntity<?> deleteProductImage(@PathVariable Long productId) {
        try {
            // Check if product exists
            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Product not found");
                return ResponseEntity.badRequest().body(error);
            }

            Product product = productOpt.get();
            String imageUrl = product.getProductImage();
            
            if (imageUrl == null || imageUrl.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "No image to delete");
                return ResponseEntity.badRequest().body(error);
            }

            // Extract public ID from URL
            String publicId = cloudinaryService.extractPublicId(imageUrl);
            if (publicId != null) {
                // Delete from Cloudinary
                cloudinaryService.deleteImage(publicId);
            }

            // Clear image URL from product
            product.setProductImage(null);
            productService.save(product);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get optimized product image URL
     */
    @GetMapping("/{productId}/image-url")
    @ResponseBody
    public ResponseEntity<?> getOptimizedProductImageUrl(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {
        
        try {
            // Check if product exists
            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Product not found");
                return ResponseEntity.badRequest().body(error);
            }

            Product product = productOpt.get();
            String imageUrl = product.getProductImage();
            
            if (imageUrl == null || imageUrl.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "No image available");
                return ResponseEntity.badRequest().body(error);
            }

            // Extract public ID and generate optimized URL
            String publicId = cloudinaryService.extractPublicId(imageUrl);
            if (publicId != null) {
                String optimizedUrl = cloudinaryService.getOptimizedImageUrl(publicId, width, height);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("original_url", imageUrl);
                response.put("optimized_url", optimizedUrl);
                response.put("width", width);
                response.put("height", height);
                
                return ResponseEntity.ok(response);
            } else {
                // Return original URL if not a Cloudinary URL
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("original_url", imageUrl);
                response.put("optimized_url", imageUrl);
                response.put("note", "Not a Cloudinary URL");
                
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to generate URL: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
