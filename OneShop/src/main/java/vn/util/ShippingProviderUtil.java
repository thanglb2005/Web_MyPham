package vn.util;

/**
 * Utility class for shipping provider operations
 * @author OneShop Team
 */
public class ShippingProviderUtil {
    
    /**
     * Get logo filename for shipping provider
     * @param providerName Provider name (GHN, GHTK, J&T Express, Viettel Post, VNPost)
     * @return Logo filename
     */
    public static String getProviderLogo(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            return null;
        }
        
        // Normalize provider name to match logo filename
        String normalized = providerName.toLowerCase()
                .replace("&", "")
                .replace(" ", "-")
                .trim();
        
        return normalized + "-logo.png";
    }
    
    /**
     * Check if provider logo exists
     * @param providerName Provider name
     * @return true if logo exists
     */
    public static boolean hasProviderLogo(String providerName) {
        String logo = getProviderLogo(providerName);
        return logo != null && (
                logo.equals("ghn-logo.png") ||
                logo.equals("ghtk-logo.png") ||
                logo.equals("jt-express-logo.png") ||
                logo.equals("viettel-post-logo.png") ||
                logo.equals("vnpost-logo.png")
        );
    }
}

