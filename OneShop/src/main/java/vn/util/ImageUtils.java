package vn.util;

/**
 * Utility class for handling image URLs
 * Supports both Cloudinary URLs and local file names
 */
public class ImageUtils {
    
    /**
     * Get the appropriate image source for display
     * @param imagePath Image path (could be Cloudinary URL or local filename)
     * @return Proper image source for HTML
     */
    public static String getImageSource(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return "/images/no-image.png";
        }
        
        // If it's a Cloudinary URL (starts with http), use it directly
        if (imagePath.startsWith("http")) {
            return imagePath;
        }
        
        // If it's a local filename, use loadImage endpoint
        return "/loadImage?imageName=" + imagePath;
    }
    
    /**
     * Check if image is a Cloudinary URL
     * @param imagePath Image path to check
     * @return true if it's a Cloudinary URL
     */
    public static boolean isCloudinaryUrl(String imagePath) {
        return imagePath != null && imagePath.startsWith("http");
    }
    
    /**
     * Check if image is a local file
     * @param imagePath Image path to check
     * @return true if it's a local file
     */
    public static boolean isLocalFile(String imagePath) {
        return imagePath != null && !imagePath.startsWith("http");
    }
}
