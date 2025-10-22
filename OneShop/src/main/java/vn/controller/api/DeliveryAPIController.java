package vn.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.dto.DeliveryOptionsDTO;
import vn.service.DeliveryService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryAPIController {

    @Autowired
    private DeliveryService deliveryService;

    /**
     * API để kiểm tra tùy chọn giao hàng có sẵn
     * 
     * @param customerCity Tỉnh/thành của khách hàng
     * @param shopId ID của shop
     * @return DeliveryOptionsDTO
     */
    @GetMapping("/check-options")
    public ResponseEntity<DeliveryOptionsDTO> checkDeliveryOptions(
            @RequestParam String customerCity,
            @RequestParam Long shopId) {
        
        try {
            DeliveryOptionsDTO options = deliveryService.checkDeliveryOptions(customerCity, shopId);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            // Return error response
            DeliveryOptionsDTO errorResponse = new DeliveryOptionsDTO();
            errorResponse.setExpressAvailable(false);
            errorResponse.setSameCity(false);
            return ResponseEntity.ok(errorResponse);
        }
    }
}

