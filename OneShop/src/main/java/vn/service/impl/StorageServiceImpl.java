package vn.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.service.CloudinaryService;
import vn.service.StorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    @Autowired(required = false)
    private CloudinaryService cloudinaryService;

    @Value("${upload.images.path:upload/images}")
    private String uploadDir;

    @Value("${upload.base-url:/images/}")
    private String baseUrl;

    @Override
    public String storeProductImage(MultipartFile file) {
        return storeImageWithFallback(file, CloudinaryService.PRODUCT_FOLDER, "product");
    }

    @Override
    public String storeUserImage(MultipartFile file) {
        return storeImageWithFallback(file, CloudinaryService.USER_FOLDER, "user");
    }

    @Override
    public String storeCategoryImage(MultipartFile file) {
        return storeImageWithFallback(file, CloudinaryService.CATEGORY_FOLDER, "category");
    }

    @Override
    public String storeBrandImage(MultipartFile file) {
        return storeImageWithFallback(file, CloudinaryService.BRAND_FOLDER, "brand");
    }

    @Override
    public String storeRatingImage(MultipartFile file) {
        return storeImageWithFallback(file, CloudinaryService.RATING_FOLDER, "rating");
    }

    @Override
    public String storeGeneralImage(MultipartFile file) {
        return storeImageWithFallback(file, CloudinaryService.GENERAL_FOLDER, "general");
    }

    @Override
    public String storeChatImage(MultipartFile file) {
        return storeImageWithFallback(file, CloudinaryService.CHAT_FOLDER, "chat");
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        // Try Cloudinary first
        if (cloudinaryService != null && imageUrl != null && imageUrl.contains("cloudinary.com")) {
            try {
                boolean deleted = cloudinaryService.deleteImageByUrl(imageUrl);
                if (deleted) {
                    log.info("Successfully deleted image from Cloudinary: {}", imageUrl);
                    return true;
                }
            } catch (Exception e) {
                log.error("Failed to delete image from Cloudinary: {}", e.getMessage());
            }
        }

        // For local storage, we would need to implement file deletion logic
        // For now, just return true as local files are typically managed differently
        log.info("Image deletion handled (local storage): {}", imageUrl);
        return true;
    }

    /**
     * Store image with Cloudinary fallback to local storage
     */
    private String storeImageWithFallback(MultipartFile file, String cloudinaryFolder, String imageType) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File trống!");
            }

            // Validate file
            if (cloudinaryService != null && !cloudinaryService.validateImageFile(file)) {
                throw new RuntimeException("File không hợp lệ! Chỉ chấp nhận JPG, PNG, GIF, WebP và tối đa 10MB.");
            }

            // Try Cloudinary first (if configured)
            if (cloudinaryService != null) {
                try {
                    log.info("Uploading {} image to Cloudinary: {}", imageType, file.getOriginalFilename());
                    String cloudinaryUrl = cloudinaryService.uploadImageToFolder(file, cloudinaryFolder);
                    log.info("Uploaded {} image to Cloudinary successfully: {}", imageType, cloudinaryUrl);
                    return cloudinaryUrl;
                } catch (Exception e) {
                    log.error("Cloudinary upload failed for {} image, falling back to local storage: {}", 
                             imageType, e.getMessage());
                    // Fall through to local storage
                }
            }

            // Fallback to local storage
            log.info("Using local storage for {} image: {}", imageType, file.getOriginalFilename());
            return storeToLocalStorage(file, imageType);

        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu " + imageType + " image: " + e.getMessage(), e);
        }
    }

    /**
     * Store file to local storage
     */
    private String storeToLocalStorage(MultipartFile file, String imageType) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        String baseName = "";

        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                baseName = originalFilename.substring(0, dotIndex)
                        .replaceAll("[^a-zA-Z0-9_-]", "");
                extension = originalFilename.substring(dotIndex);
            } else {
                baseName = originalFilename;
            }
        }

        // Create subdirectory for image type
        String subDir = uploadDir + "/" + imageType;
        Path uploadPath = Paths.get(subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = baseName + "_" + System.currentTimeMillis() + extension;
        Path targetLocation = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return baseUrl + imageType + "/" + filename;
    }
}
