package vn.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.entity.Order;
import vn.service.VietQRService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

/**
 * Implementation of VietQR service
 */
@Service
public class VietQRServiceImpl implements VietQRService {
    
    @Value("${vietqr.bank.code:970422}")
    private String bankCode;
    
    @Value("${vietqr.bank.account.no:0373611257}")
    private String bankAccountNo;
    
    @Value("${vietqr.bank.account.name:LE VAN CHIEN THANG}")
    private String bankAccountName;
    
    @Value("${vietqr.api.base.url:https://img.vietqr.io/image/}")
    private String apiBaseUrl;
    
    @Override
    public String generateVietQRUrl(Order order) {
        String addInfo = "Thanh toan don hang #" + order.getOrderId();
        return generateVietQRUrl(bankAccountNo, bankAccountName, order.getTotalAmount(), addInfo);
    }
    
    @Override
    public String generateVietQRUrl(String accountNo, String accountName, double amount, String addInfo) {
        try {
            // Format amount to remove decimal places for VietQR
            DecimalFormat df = new DecimalFormat("#");
            String formattedAmount = df.format(amount);
            
            // Encode parameters
            String encodedAccountName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString());
            String encodedAddInfo = URLEncoder.encode(addInfo, StandardCharsets.UTF_8.toString());
            
            // Build VietQR URL
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(apiBaseUrl);
            urlBuilder.append(bankCode).append("-");
            urlBuilder.append(accountNo).append("-");
            urlBuilder.append(encodedAccountName).append(".png");
            urlBuilder.append("?amount=").append(formattedAmount);
            urlBuilder.append("&addInfo=").append(encodedAddInfo);
            urlBuilder.append("&accountName=").append(encodedAccountName);
            
            return urlBuilder.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating VietQR URL", e);
        }
    }
    
    @Override
    public String generateQRCodeImage(Order order) {
        // Return VietQR API URL that generates QR code image
        // This is simpler and more reliable than generating QR code locally
        return generateVietQRUrl(order);
    }
}
