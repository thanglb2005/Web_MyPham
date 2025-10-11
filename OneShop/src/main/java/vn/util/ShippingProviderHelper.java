package vn.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for shipping provider display names and logos
 * @author OneShop Team
 */
public class ShippingProviderHelper {
    
    private static final Map<String, String> PROVIDER_FULL_NAMES = new HashMap<>();
    
    static {
        PROVIDER_FULL_NAMES.put("GHN", "Giao Hàng Nhanh (GHN)");
        PROVIDER_FULL_NAMES.put("GHTK", "Giao Hàng Tiết Kiệm (GHTK)");
        PROVIDER_FULL_NAMES.put("J&T Express", "J&T Express");
        PROVIDER_FULL_NAMES.put("Viettel Post", "Viettel Post");
        PROVIDER_FULL_NAMES.put("VNPost", "Bưu điện Việt Nam (VNPost)");
    }
    
    /**
     * Get full display name for shipping provider
     * @param providerCode Provider code (GHN, GHTK, etc.)
     * @return Full display name
     */
    public static String getFullName(String providerCode) {
        if (providerCode == null || providerCode.trim().isEmpty()) {
            return "Nhà vận chuyển";
        }
        return PROVIDER_FULL_NAMES.getOrDefault(providerCode, providerCode);
    }
    
    /**
     * Get logo filename for shipping provider
     * @param providerCode Provider code
     * @return Logo filename
     */
    public static String getLogoFilename(String providerCode) {
        if (providerCode == null || providerCode.trim().isEmpty()) {
            return null;
        }
        
        // Normalize provider code to match logo filename
        String normalized = providerCode.toLowerCase()
                .replace("&", "")
                .replace(" ", "-")
                .trim();
        
        return normalized + "-logo.png";
    }
    
    /**
     * Get logo path for shipping provider
     * @param providerCode Provider code
     * @return Logo path relative to /upload/providers/
     */
    public static String getLogoPath(String providerCode) {
        String filename = getLogoFilename(providerCode);
        return filename != null ? "/upload/providers/" + filename : null;
    }
}

