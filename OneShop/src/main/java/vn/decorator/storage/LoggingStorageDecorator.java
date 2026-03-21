package vn.decorator.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import vn.service.StorageService;

import java.util.function.Supplier;

/**
 * Concrete Decorator (role #4): preserve logging concern.
 */
@Slf4j
public class LoggingStorageDecorator extends BaseStorageDecorator {

    public LoggingStorageDecorator(StorageService wrappee) {
        super(wrappee);
    }

    @Override
    public String storeProductImage(MultipartFile file) {
        return logStore("product", file, () -> super.storeProductImage(file));
    }

    @Override
    public String storeUserImage(MultipartFile file) {
        return logStore("user", file, () -> super.storeUserImage(file));
    }

    @Override
    public String storeCategoryImage(MultipartFile file) {
        return logStore("category", file, () -> super.storeCategoryImage(file));
    }

    @Override
    public String storeBrandImage(MultipartFile file) {
        return logStore("brand", file, () -> super.storeBrandImage(file));
    }

    @Override
    public String storeRatingImage(MultipartFile file) {
        return logStore("rating", file, () -> super.storeRatingImage(file));
    }

    @Override
    public String storeGeneralImage(MultipartFile file) {
        return logStore("general", file, () -> super.storeGeneralImage(file));
    }

    @Override
    public String storeChatImage(MultipartFile file) {
        return logStore("chat", file, () -> super.storeChatImage(file));
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        try {
            boolean deleted = super.deleteImage(imageUrl);
            log.info("Image deletion handled: {}, deleted={}", imageUrl, deleted);
            return deleted;
        } catch (RuntimeException ex) {
            log.error("Failed to delete image: {}", ex.getMessage());
            throw ex;
        }
    }

    private String logStore(String imageType, MultipartFile file, Supplier<String> action) {
        String originalName = file != null ? file.getOriginalFilename() : null;
        log.info("Uploading {} image: {}", imageType, originalName);
        try {
            String url = action.get();
            log.info("Uploaded {} image successfully: {}", imageType, url);
            return url;
        } catch (RuntimeException ex) {
            log.error("Upload failed for {} image: {}", imageType, ex.getMessage());
            throw ex;
        }
    }
}
