package vn.service.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.Shop;
import vn.entity.ShopSupportAssignment;
import vn.entity.User;
import vn.repository.ShopSupportAssignmentRepository;
import vn.service.ShopService;
import vn.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SupportAssignmentService {

    @Autowired
    private ShopSupportAssignmentRepository assignmentRepository;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserService userService;

    public Optional<ShopSupportAssignment> findByShopId(Long shopId) {
        if (shopId == null) return Optional.empty();
        return assignmentRepository.findByShop_ShopId(shopId);
    }

    /**
     * Ensure the given CSKH can join liaison room for a specific shop.
     * If no assignment exists, create one. If exists with different CSKH, deny.
     */
    @Transactional
    public boolean ensureAssignment(Long shopId, Long cskhId) {
        if (shopId == null || cskhId == null) return false;
        Optional<ShopSupportAssignment> opt = assignmentRepository.findByShop_ShopId(shopId);
        if (opt.isPresent()) {
            ShopSupportAssignment a = opt.get();
            return a.getCskh() != null && a.getCskh().getUserId().equals(cskhId);
        }
        // create new assignment
        Shop shop = shopService.findById(shopId).orElse(null);
        User cskh = userService.findById(cskhId).orElse(null);
        if (shop == null || cskh == null) return false;
        ShopSupportAssignment a = new ShopSupportAssignment();
        a.setShop(shop);
        a.setCskh(cskh);
        a.setAssignedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        assignmentRepository.save(a);
        return true;
    }
}

