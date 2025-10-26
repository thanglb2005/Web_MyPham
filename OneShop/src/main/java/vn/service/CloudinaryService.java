package vn.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;
    
    // ✅ Folder constants để tách biệt ảnh theo loại
    public static final String PRODUCT_FOLDER = "oneshop/products";
    public static final String USER_FOLDER = "oneshop/users";
    public static final String CATEGORY_FOLDER = "oneshop/categories";
    public static final String BRAND_FOLDER = "oneshop/brands";
    public static final String RATING_FOLDER = "oneshop/ratings";
    public static final String GENERAL_FOLDER = "oneshop/images";

    /**
     * Upload image to Cloudinary with default folder
     * @param file MultipartFile to upload
     * @return Map containing upload result with public_id and secure_url
     */
    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, GENERAL_FOLDER);
    }
    
    /**
     * Upload image to Cloudinary with specific folder (improved from shoe shop)
     * @param file MultipartFile to upload
     * @param folder Folder name in Cloudinary
     * @return Image URL from Cloudinary
     */
    public String uploadImageToFolder(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // ✅ Giữ nguyên tên file gốc (không thêm timestamp)
        String originalFilename = file.getOriginalFilename();
        String filenameWithoutExt = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(0, originalFilename.lastIndexOf("."))
            : (originalFilename != null ? originalFilename : "image");
        
        // Tạo public_id: oneshop/products/abc
        String publicId = folder + "/" + filenameWithoutExt;

        // Upload to Cloudinary
        // overwrite=false: Nếu file đã tồn tại, Cloudinary sẽ tự động thêm suffix (abc_1, abc_2...)
        @SuppressWarnings("rawtypes")
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image",
                        "overwrite", false,
                        "unique_filename", true
                ));

        // Return secure URL
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Upload image with specific folder
     * @param file MultipartFile to upload
     * @param folder Folder name in Cloudinary
     * @return Map containing upload result
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", "oneshop/" + folder,
            "use_filename", true,
            "unique_filename", true,
            "overwrite", false,
            "resource_type", "auto",
            "quality", "auto:best", // Chất lượng tốt nhất
            "format", "auto" // Tự động chọn format tốt nhất
        );

        return (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), uploadParams);
    }

    /**
     * Upload image with transformations
     * @param file MultipartFile to upload
     * @param folder Folder name
     * @param width Image width
     * @param height Image height
     * @return Map containing upload result
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImage(MultipartFile file, String folder, int width, int height) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", "oneshop/" + folder,
            "use_filename", true,
            "unique_filename", true,
            "overwrite", false,
            "resource_type", "auto",
            "transformation", ObjectUtils.asMap(
                "width", width,
                "height", height,
                "crop", "fill",
                "gravity", "auto",
                "quality", "auto"
            )
        );

        return (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), uploadParams);
    }

    /**
     * Delete image from Cloudinary by URL (improved from shoe shop)
     * @param imageUrl Full Cloudinary URL
     * @return true if deleted successfully
     */
    public boolean deleteImageByUrl(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
                return false;
            }

            // Extract public_id from URL
            String publicId = extractPublicId(imageUrl);
            
            if (publicId != null) {
                @SuppressWarnings("rawtypes")
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                return "ok".equals(result.get("result"));
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete image from Cloudinary by public ID
     * @param publicId Public ID of the image to delete
     * @return Map containing deletion result
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        return (Map<String, Object>) cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    /**
     * Get optimized image URL
     * @param publicId Public ID of the image
     * @param width Desired width
     * @param height Desired height
     * @return Optimized image URL
     */
    public String getOptimizedImageUrl(String publicId, int width, int height) {
        return cloudinary.url()
            .transformation(new com.cloudinary.Transformation<>()
                .width(width)
                .height(height)
                .crop("fill")
                .gravity("auto")
                .quality("auto"))
            .generate(publicId);
    }

    /**
     * Get image URL with transformations
     * @param publicId Public ID of the image
     * @param transformations Map of transformations
     * @return Transformed image URL
     */
    public String getImageUrl(String publicId, Map<String, Object> transformations) {
        // Simple approach - just return the base URL for now
        // This method can be enhanced later with proper transformation support
        return cloudinary.url().generate(publicId);
    }

    /**
     * Extract public ID from Cloudinary URL (improved from shoe shop)
     * @param url Cloudinary URL
     * @return Public ID
     */
    public String extractPublicId(String url) {
        try {
            if (url == null || !url.contains("cloudinary.com")) {
                return null;
            }
            
            // Example URL: https://res.cloudinary.com/demo/image/upload/v1234567890/products/abc123.jpg
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;
            
            String afterUpload = parts[1];
            // Remove version (v1234567890/)
            afterUpload = afterUpload.replaceFirst("v\\d+/", "");
            // Remove extension
            afterUpload = afterUpload.replaceFirst("\\.[^.]+$", "");
            
            return afterUpload;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Validate image file (size, type, etc.)
     * @param file MultipartFile to validate
     * @return true if valid
     */
    public boolean validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            return false;
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return false;
        }
        
        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String extension = filename.toLowerCase();
        return extension.endsWith(".jpg") || extension.endsWith(".jpeg") || 
               extension.endsWith(".png") || extension.endsWith(".gif") || 
               extension.endsWith(".webp");
    }
}
