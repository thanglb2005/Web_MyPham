package vn.decorator.storage;

import org.springframework.web.multipart.MultipartFile;
import vn.service.CloudinaryService;
import vn.service.StorageService;

/**
 * Concrete Decorator (role #4): keep existing file validation behavior.
 */
public class FileValidationStorageDecorator extends BaseStorageDecorator {

    private final CloudinaryService cloudinaryService;

    public FileValidationStorageDecorator(StorageService wrappee, CloudinaryService cloudinaryService) {
        super(wrappee);
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public String storeProductImage(MultipartFile file) {
        validate(file);
        return super.storeProductImage(file);
    }

    @Override
    public String storeUserImage(MultipartFile file) {
        validate(file);
        return super.storeUserImage(file);
    }

    @Override
    public String storeCategoryImage(MultipartFile file) {
        validate(file);
        return super.storeCategoryImage(file);
    }

    @Override
    public String storeBrandImage(MultipartFile file) {
        validate(file);
        return super.storeBrandImage(file);
    }

    @Override
    public String storeRatingImage(MultipartFile file) {
        validate(file);
        return super.storeRatingImage(file);
    }

    @Override
    public String storeGeneralImage(MultipartFile file) {
        validate(file);
        return super.storeGeneralImage(file);
    }

    @Override
    public String storeChatImage(MultipartFile file) {
        validate(file);
        return super.storeChatImage(file);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File trống!");
        }
        if (cloudinaryService != null && !cloudinaryService.validateImageFile(file)) {
            throw new RuntimeException("File không hợp lệ! Chỉ chấp nhận JPG, PNG, GIF, WebP và tối đa 10MB.");
        }
    }
}
