package vn.service;

import vn.entity.Order;

/**
 * Service for VietQR payment integration
 */
public interface VietQRService {
    
    /**
     * Generate VietQR payment URL for an order
     * @param order The order to generate QR for
     * @return VietQR payment URL
     */
    String generateVietQRUrl(Order order);
    
    /**
     * Generate VietQR payment URL with custom parameters
     * @param accountNo Bank account number
     * @param accountName Bank account name
     * @param amount Payment amount
     * @param addInfo Additional information (order ID, etc.)
     * @return VietQR payment URL
     */
    String generateVietQRUrl(String accountNo, String accountName, double amount, String addInfo);
    
    /**
     * Generate QR code image data URL for display
     * @param order The order to generate QR for
     * @return Base64 encoded QR code image data URL
     */
    String generateQRCodeImage(Order order);
}
