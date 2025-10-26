package vn.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /**
     * Upload product image to Cloudinary/Local storage
     * @param file Product image file
     * @return Image URL
     */
    String storeProductImage(MultipartFile file);
    
    /**
     * Upload user avatar to Cloudinary/Local storage
     * @param file User avatar file
     * @return Image URL
     */
    String storeUserImage(MultipartFile file);
    
    /**
     * Upload category image to Cloudinary/Local storage
     * @param file Category image file
     * @return Image URL
     */
    String storeCategoryImage(MultipartFile file);
    
    /**
     * Upload brand logo to Cloudinary/Local storage
     * @param file Brand logo file
     * @return Image URL
     */
    String storeBrandImage(MultipartFile file);
    
    /**
     * Upload rating image to Cloudinary/Local storage
     * @param file Rating image file
     * @return Image URL
     */
    String storeRatingImage(MultipartFile file);
    
    /**
     * Upload general image to Cloudinary/Local storage
     * @param file General image file
     * @return Image URL
     */
    String storeGeneralImage(MultipartFile file);
    
    /**
     * Upload chat image to Cloudinary/Local storage
     * @param file Chat image file
     * @return Image URL
     */
    String storeChatImage(MultipartFile file);
    
    /**
     * Delete image from storage
     * @param imageUrl Image URL to delete
     * @return true if deleted successfully
     */
    boolean deleteImage(String imageUrl);
}
