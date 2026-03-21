package vn.decorator.storage;

import org.springframework.web.multipart.MultipartFile;
import vn.service.StorageService;

/**
 * Base Decorator (role #3 in Decorator structure).
 * Holds wrappee component and delegates by default.
 */
public abstract class BaseStorageDecorator implements StorageService {

    protected final StorageService wrappee;

    protected BaseStorageDecorator(StorageService wrappee) {
        this.wrappee = wrappee;
    }

    @Override
    public String storeProductImage(MultipartFile file) {
        return wrappee.storeProductImage(file);
    }

    @Override
    public String storeUserImage(MultipartFile file) {
        return wrappee.storeUserImage(file);
    }

    @Override
    public String storeCategoryImage(MultipartFile file) {
        return wrappee.storeCategoryImage(file);
    }

    @Override
    public String storeBrandImage(MultipartFile file) {
        return wrappee.storeBrandImage(file);
    }

    @Override
    public String storeRatingImage(MultipartFile file) {
        return wrappee.storeRatingImage(file);
    }

    @Override
    public String storeGeneralImage(MultipartFile file) {
        return wrappee.storeGeneralImage(file);
    }

    @Override
    public String storeChatImage(MultipartFile file) {
        return wrappee.storeChatImage(file);
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        return wrappee.deleteImage(imageUrl);
    }
}
