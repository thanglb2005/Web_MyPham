package vn.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.entity.CustomerShippingInfo;
import vn.entity.User;
import vn.repository.CustomerShippingInfoRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/shipping-info")
public class ShippingInfoController {

    @Autowired
    private CustomerShippingInfoRepository customerShippingInfoRepository;

    @PostMapping("/set-default")
    public Map<String, Object> setDefault(@RequestParam("shippingInfoId") Long shippingInfoId,
                                          HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }

        Optional<CustomerShippingInfo> targetOpt =
                customerShippingInfoRepository.findByShippingInfoIdAndUserUserId(shippingInfoId, user.getUserId());
        if (targetOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Không tìm thấy địa chỉ");
            return response;
        }

        List<CustomerShippingInfo> allInfos =
                customerShippingInfoRepository.findByUserUserIdOrderByIsDefaultDescCreatedAtDesc(user.getUserId());
        for (CustomerShippingInfo info : allInfos) {
            boolean isTarget = info.getShippingInfoId().equals(shippingInfoId);
            info.setIsDefault(isTarget);
            info.setUpdatedAt(LocalDateTime.now());
        }
        customerShippingInfoRepository.saveAll(allInfos);

        response.put("success", true);
        response.put("message", "Đã đặt làm địa chỉ mặc định");
        return response;
    }
}
