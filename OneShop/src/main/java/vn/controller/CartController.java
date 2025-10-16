package vn.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.dto.CartByShopDTO;
import vn.entity.CartItem;
import vn.entity.CartItemEntity;
import vn.entity.Order;
import vn.entity.Product;
import vn.entity.Promotion;
import vn.entity.User;
import vn.service.CartService;
import vn.service.OneXuService;
import vn.service.OrderService;
import vn.service.ProductService;
import vn.service.VietQRService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

@Controller
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    private OneXuService oneXuService;
    
    @Autowired
    private VietQRService vietQRService;
    

    @GetMapping("/add-to-cart")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                            HttpServletRequest request, Model model,
                            RedirectAttributes redirectAttributes) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            try {
                cartService.addToCart(user, product, quantity);
                redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại hoặc đã bị gỡ.");
        }

        // Redirect back to previous page (Referer) if safe, else fallback
        String redirectPath = "/products";
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                String path = uri.getPath();
                String query = uri.getQuery();
                if (path != null && path.startsWith("/")
                        && !path.startsWith("/add-to-cart")
                        && !path.startsWith("/login")) {
                    redirectPath = path + (query != null ? ("?" + query) : "");
                }
            } catch (java.net.URISyntaxException ignored) { }
        }

        return "redirect:" + redirectPath;
    }

    @GetMapping("/cart")
    public String viewCart(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get cart items grouped by shop (Shopee-like)
        List<CartByShopDTO> cartItemsByShop = cartService.getCartItemsByShop(user);
        
        // Calculate overall totals
        Integer totalItems = cartService.getCartItemCount(user);
        Double totalPrice = cartService.getCartTotalPrice(user);
        Integer selectedItems = cartService.getSelectedCartItemCount(user);
        Double selectedPrice = cartService.getSelectedCartTotalPrice(user);

        model.addAttribute("cartItemsByShop", cartItemsByShop);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("selectedItems", selectedItems);
        model.addAttribute("selectedPrice", selectedPrice);
        model.addAttribute("user", user);

        return "web/cart";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") Integer quantity,
                                 HttpServletRequest request) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            cartService.updateCartItemQuantity(user, product, quantity);
        }

        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable("productId") Long productId,
                                 HttpServletRequest request) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            cartService.removeFromCart(user, product);
        }

        return "redirect:/cart";
    }

    @GetMapping("/cart/clear")
    public String clearCart(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        cartService.clearCart(user);
        return "redirect:/cart";
    }

    @GetMapping("/cart/count")
    @ResponseBody
    public Integer getCartCount(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return 0;
        }

        return cartService.getCartItemCount(user);
    }
    
    @PostMapping("/cart/update-selected")
    @ResponseBody
    public Map<String, Object> updateCartItemSelected(@RequestParam("productId") Long productId,
                                                     @RequestParam("selected") Boolean selected,
                                                     HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }

        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            cartService.updateCartItemSelected(user, product, selected);
            
            // Get updated cart totals
            Double selectedTotal = cartService.getSelectedCartTotalPrice(user);
            Integer selectedCount = cartService.getSelectedCartItemCount(user);
            
            response.put("success", true);
            response.put("selectedTotal", selectedTotal);
            response.put("selectedCount", selectedCount);
        } else {
            response.put("success", false);
            response.put("message", "Sản phẩm không tồn tại");
        }

        return response;
    }
    
    @PostMapping("/cart/select-all")
    @ResponseBody
    public Map<String, Object> selectAllCartItems(@RequestParam("selected") Boolean selected,
                                                 HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }

        cartService.updateAllCartItemsSelected(user, selected);
        
        // Get updated cart totals
        Double selectedTotal = cartService.getSelectedCartTotalPrice(user);
        Integer selectedCount = cartService.getSelectedCartItemCount(user);
        
        response.put("success", true);
        response.put("selectedTotal", selectedTotal);
        response.put("selectedCount", selectedCount);

        return response;
    }
    
    @PostMapping("/cart/select-shop")
    @ResponseBody
    public Map<String, Object> selectShopItems(@RequestParam("shopId") Long shopId,
                                              @RequestParam("selected") Boolean selected,
                                              HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }

        cartService.updateShopItemsSelected(user, shopId, selected);
        
        // Get updated cart totals
        Double selectedTotal = cartService.getSelectedCartTotalPrice(user);
        Integer selectedCount = cartService.getSelectedCartItemCount(user);
        
        response.put("success", true);
        response.put("selectedTotal", selectedTotal);
        response.put("selectedCount", selectedCount);

        return response;
    }


    @GetMapping("/checkout-test")
    public String checkoutTest(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItemEntity> cartItemEntities = cartService.getSelectedCartItems(user);
        if (cartItemEntities.isEmpty()) {
            return "redirect:/cart?error=no-selected-items";
        }

        // Convert CartItemEntity to CartItem for compatibility
        Map<Long, CartItem> cartMap = convertToCartItemMap(cartItemEntities);
        
        Collection<CartItem> cartItems = cartMap.values();
        Integer totalItems = cartMap.size();
        Double totalPrice = cartMap.values().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalCartItems", totalItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("user", user);

        return "web/checkout-simple";
    }

    @GetMapping("/checkout")
    public String checkout(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItemEntity> cartItemEntities = cartService.getSelectedCartItems(user);
        if (cartItemEntities.isEmpty()) {
            return "redirect:/cart?error=no-selected-items";
        }

        // Convert CartItemEntity to CartItem for compatibility
        Map<Long, CartItem> cartMap = convertToCartItemMap(cartItemEntities);
        
        Collection<CartItem> cartItems = cartMap.values();
        Integer totalItems = cartMap.size();
        Double totalPrice = cartMap.values().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalCartItems", totalItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("user", user);

        return "web/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("customerName") String customerName,
                                 @RequestParam("customerEmail") String customerEmail,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("address") String address,
                                 @RequestParam(value = "note", required = false) String note,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 @RequestParam(value = "city", required = false) String city,
                                 HttpServletRequest request, Model model) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItemEntity> cartItemEntities = cartService.getSelectedCartItems(user);
        System.out.println("Selected cart items count: " + cartItemEntities.size());
        if (cartItemEntities.isEmpty()) {
            System.out.println("No selected items found, redirecting to cart");
            return "redirect:/cart?error=no-selected-items";
        }

        try {
            Order.PaymentMethod paymentMethodEnum;
            switch (paymentMethod.toLowerCase()) {
                case "cod":
                    paymentMethodEnum = Order.PaymentMethod.COD;
                    break;
                case "momo":
                    paymentMethodEnum = Order.PaymentMethod.MOMO;
                    break;
                case "bank_transfer":
                    paymentMethodEnum = Order.PaymentMethod.BANK_TRANSFER;
                    break;
                case "vietqr":
                    paymentMethodEnum = Order.PaymentMethod.VIETQR;
                    break;
                default:
                    paymentMethodEnum = Order.PaymentMethod.COD;
            }

            // Tạo địa chỉ đầy đủ
            String fullAddress = address;
            if (city != null && !city.trim().isEmpty()) {
                fullAddress = address + ", " + city;
            }

            // Convert CartItemEntity to CartItem Map for OrderService
            Map<Long, CartItem> cartMap = convertToCartItemMap(cartItemEntities);
            System.out.println("Cart map size: " + cartMap.size());
            
            // Debug cart items and prices
            System.out.println("=== Cart Items Debug ===");
            double totalCartPrice = 0;
            for (CartItem item : cartMap.values()) {
                double itemTotal = item.getQuantity() * item.getUnitPrice();
                totalCartPrice += itemTotal;
                System.out.println("Item: " + item.getName() + 
                    " | Qty: " + item.getQuantity() + 
                    " | Unit Price: " + item.getUnitPrice() + 
                    " | Total: " + itemTotal);
            }
            System.out.println("Total Cart Price: " + totalCartPrice);
            System.out.println("======================");
            
            System.out.println("Customer name: " + customerName);
            System.out.println("Customer email: " + customerEmail);
            System.out.println("Phone: " + phone);
            System.out.println("Address: " + fullAddress);
            System.out.println("Payment method: " + paymentMethodEnum);


            // Chỉ tạo order cho COD và BANK_TRANSFER
            // MOMO và VIETQR sẽ tạo order sau khi thanh toán thành công
            if (paymentMethodEnum == Order.PaymentMethod.COD || paymentMethodEnum == Order.PaymentMethod.BANK_TRANSFER) {
                Order order = orderService.createOrder(
                    user,
                    customerName,
                    customerEmail,
                    phone,
                    fullAddress,
                    note,
                    paymentMethodEnum,
                    cartMap,
                    null,
                    0.0
                );
                System.out.println("Order created successfully with ID: " + order.getOrderId());

                // Clear cart after successful order
                cartService.clearCart(user);

                model.addAttribute("message", "Đặt hàng thành công! Mã đơn hàng: #" + order.getOrderId());
                model.addAttribute("orderId", order.getOrderId());

                return "redirect:/order-success?orderId=" + order.getOrderId();
            } else if (paymentMethodEnum == Order.PaymentMethod.MOMO) {
                // Tạo order tạm cho MoMo
                Order momoOrder = orderService.createOrder(
                    user,
                    customerName,
                    customerEmail,
                    phone,
                    fullAddress,
                    note,
                    paymentMethodEnum,
                    cartMap,
                    null,
                    0.0
                );
                return "redirect:/payment/momo/create?orderId=" + momoOrder.getOrderId();
            } else if (paymentMethodEnum == Order.PaymentMethod.VIETQR) {
                // Tạo order tạm cho VietQR
                Order vietqrOrder = orderService.createOrder(
                    user,
                    customerName,
                    customerEmail,
                    phone,
                    fullAddress,
                    note,
                    paymentMethodEnum,
                    cartMap,
                    null,
                    0.0
                );
                return "redirect:/vietqr-payment?orderId=" + vietqrOrder.getOrderId();
            }
            
            // Fallback - không nên xảy ra
            return "redirect:/checkout?error=Invalid payment method";

        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            
            // Reload cart data for checkout page
            List<CartItemEntity> cartItemEntitiesReload = cartService.getSelectedCartItems(user);
            Map<Long, CartItem> cartMap = convertToCartItemMap(cartItemEntitiesReload);
            Collection<CartItem> cartItems = cartMap.values();
            Integer totalItems = cartMap.size();
            Double totalPrice = cartMap.values().stream()
                    .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                    .sum();

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("totalCartItems", totalItems);
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("user", user);
            model.addAttribute("error", "Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại! Lỗi: " + e.getMessage());
            return "web/checkout";
        }
    }

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam("orderId") Long orderId,
                               HttpServletRequest request, Model model) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/products";
        }

        model.addAttribute("order", order);
        model.addAttribute("orderId", orderId);
        model.addAttribute("user", user);

        return "web/order-success";
    }

    @GetMapping("/vietqr-payment")
    public String vietqrPayment(@RequestParam("orderId") Long orderId,
                               HttpServletRequest request, Model model) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/products";
        }

        // Generate VietQR URL for the order
        String vietqrUrl = vietQRService.generateVietQRUrl(order);
        String qrCodeImage = vietQRService.generateQRCodeImage(order);

        model.addAttribute("order", order);
        model.addAttribute("orderId", orderId);
        model.addAttribute("user", user);
        model.addAttribute("vietqrUrl", vietqrUrl);
        model.addAttribute("qrCodeImage", qrCodeImage);

        return "web/vietqr-payment";
    }

    @GetMapping("/checkout-debug")
    public String checkoutDebug(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItemEntity> cartItemEntities = cartService.getSelectedCartItems(user);
        System.out.println("DEBUG - Selected cart items count: " + cartItemEntities.size());
        
        // Convert CartItemEntity to CartItem for compatibility
        Map<Long, CartItem> cartMap = convertToCartItemMap(cartItemEntities);
        
        Collection<CartItem> cartItems = cartMap.values();
        Integer totalItems = cartMap.size();
        Double totalPrice = cartMap.values().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalCartItems", totalItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("user", user);

        return "web/checkout-debug";
    }
    
    /**
     * Helper method to convert CartItemEntity list to CartItem Map for compatibility with OrderService
     */
    private Map<Long, CartItem> convertToCartItemMap(List<CartItemEntity> cartItemEntities) {
        Map<Long, CartItem> cartMap = new HashMap<>();
        for (CartItemEntity entity : cartItemEntities) {
            CartItem cartItem = new CartItem();
            cartItem.setId(entity.getProduct().getProductId());
            cartItem.setName(entity.getProduct().getProductName());
            cartItem.setUnitPrice(entity.getUnitPrice());
            cartItem.setQuantity(entity.getQuantity());
            cartItem.setTotalPrice(entity.getTotalPrice());
            cartItem.setProduct(entity.getProduct());
            
            // Set additional fields to avoid lazy loading issues
            cartItem.setBrandName(entity.getProduct().getBrand() != null ? 
                entity.getProduct().getBrand().getBrandName() : "");
            cartItem.setCategoryName(entity.getProduct().getCategory() != null ? 
                entity.getProduct().getCategory().getCategoryName() : "");
            cartItem.setImageUrl(entity.getProduct().getProductImage());
            cartItem.setSelected(entity.getSelected());
            
            cartMap.put(entity.getProduct().getProductId(), cartItem);
        }
        return cartMap;
    }
    
    // Test endpoint để kiểm tra voucher (không cần đăng nhập)
    @GetMapping("/cart/test-vouchers")
    @ResponseBody
    public Map<String, Object> testVouchers(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        response.put("userLoggedIn", user != null);
        response.put("userId", user != null ? user.getUserId() : null);
        
        try {
            // Test lấy tất cả promotions
            List<Promotion> allPromotions = promotionService.getAllPromotions();
            response.put("totalPromotions", allPromotions.size());
            
            // Test lấy promotions theo shop
            if (!allPromotions.isEmpty()) {
                // Tìm promotion có shop_id
                Promotion validPromotion = null;
                for (Promotion p : allPromotions) {
                    if (p.getShop() != null) {
                        validPromotion = p;
                        break;
                    }
                }
                
                if (validPromotion != null) {
                    Long firstShopId = validPromotion.getShop().getShopId();
                    List<Promotion> shopPromotions = promotionService.getPromotionsByShop(firstShopId);
                    response.put("shopPromotions", shopPromotions.size());
                    response.put("testShopId", firstShopId);
                    
                    // Test lấy active promotions
                    List<Promotion> activePromotions = promotionService.getActivePromotionsByShop(firstShopId);
                    response.put("activePromotions", activePromotions.size());
                    
                    // Test lấy tất cả active promotions
                    List<Promotion> allActivePromotions = promotionService.getAllActivePromotions();
                    response.put("allActivePromotions", allActivePromotions.size());
                    
                    // Debug: Kiểm tra chi tiết các promotions
                    List<Map<String, Object>> promotionDetails = new ArrayList<>();
                    for (Promotion p : allPromotions) {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("id", p.getPromotionId());
                        detail.put("code", p.getPromotionCode());
                        detail.put("name", p.getPromotionName());
                        detail.put("isActive", p.getIsActive());
                        detail.put("startDate", p.getStartDate());
                        detail.put("endDate", p.getEndDate());
                        detail.put("shopId", p.getShop() != null ? p.getShop().getShopId() : null);
                        detail.put("shopName", p.getShop() != null ? p.getShop().getShopName() : null);
                        promotionDetails.add(detail);
                    }
                    response.put("promotionDetails", promotionDetails);
                    
                    // Test voucher data format
                    List<Map<String, Object>> testVouchers = new ArrayList<>();
                    for (Promotion promotion : allActivePromotions) {
                        Map<String, Object> voucher = new HashMap<>();
                        voucher.put("code", promotion.getPromotionCode());
                        voucher.put("name", promotion.getPromotionName());
                        voucher.put("description", promotion.getDescription());
                        voucher.put("type", promotion.getPromotionType().name());
                        voucher.put("value", promotion.getDiscountValue().doubleValue());
                        voucher.put("minOrder", promotion.getMinimumOrderAmount().doubleValue());
                        voucher.put("maxDiscount", promotion.getMaximumDiscountAmount().doubleValue());
                        voucher.put("shopId", promotion.getShop().getShopId());
                        voucher.put("shopName", promotion.getShop().getShopName());
                        testVouchers.add(voucher);
                    }
                    response.put("testVouchers", testVouchers);
                } else {
                    response.put("shopPromotions", 0);
                    response.put("testShopId", null);
                    response.put("message", "Không có promotion nào có shop_id");
                }
            }
            
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    // One Voucher endpoint - hiển thị tất cả promotions của các shop
    @GetMapping("/cart/one-vouchers")
    @ResponseBody
    public Map<String, Object> getOneVouchers(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }
        
        try {
            // Lấy tất cả promotions đang hoạt động từ tất cả các shop
            List<Promotion> allActivePromotions = promotionService.getAllActivePromotions();
            List<Map<String, Object>> oneVouchers = new ArrayList<>();
            
            for (Promotion promotion : allActivePromotions) {
                if (promotion.getShop() != null) {
                    Map<String, Object> voucher = new HashMap<>();
                    voucher.put("code", promotion.getPromotionCode());
                    voucher.put("name", promotion.getPromotionName());
                    voucher.put("description", promotion.getDescription());
                    voucher.put("type", promotion.getPromotionType().name());
                    voucher.put("value", promotion.getDiscountValue().doubleValue());
                    voucher.put("minOrder", promotion.getMinimumOrderAmount().doubleValue());
                    voucher.put("maxDiscount", promotion.getMaximumDiscountAmount().doubleValue());
                    voucher.put("shopId", promotion.getShop().getShopId());
                    voucher.put("shopName", promotion.getShop().getShopName());
                    voucher.put("usageLimit", promotion.getUsageLimit());
                    voucher.put("usedCount", promotion.getUsedCount());
                    voucher.put("startDate", promotion.getStartDate());
                    voucher.put("endDate", promotion.getEndDate());
                    
                    oneVouchers.add(voucher);
                }
            }
            
            response.put("success", true);
            response.put("vouchers", oneVouchers);
            response.put("totalCount", oneVouchers.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi tải One Voucher");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    // Voucher endpoints
    @GetMapping("/cart/available-vouchers")
    @ResponseBody
    public Map<String, Object> getAvailableVouchers(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }
        
        try {
            // Get selected cart items to determine which shops have items
            List<CartByShopDTO> cartItemsByShop = cartService.getCartItemsByShop(user);
            List<Map<String, Object>> availableVouchers = new ArrayList<>();
            
            for (CartByShopDTO shopGroup : cartItemsByShop) {
                if (shopGroup.getShop() != null && shopGroup.getShopSelected()) {
                    // Get active promotions for this shop
                    List<Promotion> promotions = promotionService.getActivePromotionsByShop(shopGroup.getShop().getShopId());
                    
                    for (Promotion promotion : promotions) {
                        Map<String, Object> voucher = new HashMap<>();
                        voucher.put("code", promotion.getPromotionCode());
                        voucher.put("name", promotion.getPromotionName());
                        voucher.put("description", promotion.getDescription());
                        voucher.put("type", promotion.getPromotionType().name());
                        voucher.put("value", promotion.getDiscountValue().doubleValue());
                        voucher.put("minOrder", promotion.getMinimumOrderAmount().doubleValue());
                        voucher.put("maxDiscount", promotion.getMaximumDiscountAmount().doubleValue());
                        voucher.put("shopId", shopGroup.getShop().getShopId());
                        voucher.put("shopName", shopGroup.getShop().getShopName());
                        
                        availableVouchers.add(voucher);
                    }
                }
            }
            
            response.put("success", true);
            response.put("vouchers", availableVouchers);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi tải voucher");
        }
        
        return response;
    }
    
    @PostMapping("/cart/apply-voucher")
    @ResponseBody
    public Map<String, Object> applyVoucher(@RequestParam("code") String code, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }
        
        try {
            // Get selected cart total
            Double selectedTotal = cartService.getSelectedCartTotalPrice(user);
            
            if (selectedTotal == null || selectedTotal <= 0) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn sản phẩm để áp dụng voucher");
                return response;
            }
            
            // Find promotion by code
            Optional<Promotion> promotionOpt = promotionService.getPromotionByCode(code.toUpperCase());
            if (promotionOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Mã voucher không tồn tại");
                return response;
            }
            
            Promotion promotion = promotionOpt.get();
            
            // Check if promotion is available
            if (!promotion.isAvailable()) {
                response.put("success", false);
                response.put("message", "Voucher đã hết hạn hoặc không còn hiệu lực");
                return response;
            }
            
            // Check minimum order amount
            if (selectedTotal < promotion.getMinimumOrderAmount().doubleValue()) {
                response.put("success", false);
                response.put("message", "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher");
                return response;
            }
            
            // Calculate discount
            Double discount = promotionService.calculateDiscountForShop(
                promotion.getShop().getShopId(), 
                code.toUpperCase(), 
                selectedTotal
            );
            
            if (discount > 0) {
                // Store applied voucher in session
                request.getSession().setAttribute("appliedVoucher", promotion);
                request.getSession().setAttribute("voucherDiscount", discount);
                
                response.put("success", true);
                response.put("message", "Áp dụng voucher thành công");
                response.put("discount", discount);
                response.put("selectedTotal", selectedTotal);
                response.put("selectedCount", cartService.getSelectedCartItemCount(user));
            } else {
                response.put("success", false);
                response.put("message", "Không thể áp dụng voucher cho đơn hàng này");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi áp dụng voucher");
        }
        
        return response;
    }
    
    @GetMapping("/cart/shop-vouchers/{shopId}")
    @ResponseBody
    public Map<String, Object> getShopVouchers(@PathVariable Long shopId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }
        
        try {
            // Get all promotions for the shop (not just active ones)
            List<Promotion> promotions = promotionService.getPromotionsByShop(shopId);
            List<Map<String, Object>> vouchers = new ArrayList<>();
            
            for (Promotion promotion : promotions) {
                Map<String, Object> voucher = new HashMap<>();
                voucher.put("code", promotion.getPromotionCode());
                voucher.put("name", promotion.getPromotionName());
                voucher.put("description", promotion.getDescription());
                voucher.put("type", promotion.getPromotionType().name());
                voucher.put("value", promotion.getDiscountValue().doubleValue());
                voucher.put("minOrder", promotion.getMinimumOrderAmount().doubleValue());
                voucher.put("maxDiscount", promotion.getMaximumDiscountAmount().doubleValue());
                voucher.put("usageLimit", promotion.getUsageLimit());
                voucher.put("usedCount", promotion.getUsedCount());
                voucher.put("isActive", promotion.getIsActive());
                voucher.put("startDate", promotion.getStartDate());
                voucher.put("endDate", promotion.getEndDate());
                voucher.put("shopId", shopId);
                
                vouchers.add(voucher);
            }
            
            response.put("success", true);
            response.put("vouchers", vouchers);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi tải voucher của shop");
        }
        
        return response;
    }
    
    @GetMapping("/cart/onexu-info")
    @ResponseBody
    public Map<String, Object> getOneXuInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }
        
        try {
            // Get user's One Xu balance
            Double balance = oneXuService.getUserBalance(user.getUserId());
            
            // Get selected cart total
            Double selectedTotal = cartService.getSelectedCartTotalPrice(user);
            
            // Calculate how much One Xu can be used (max 50% of order value)
            Double maxUsableXu = 0.0;
            if (selectedTotal != null && selectedTotal > 0) {
                maxUsableXu = Math.min(balance, selectedTotal * 0.5);
            }
            
            response.put("success", true);
            response.put("balance", balance);
            response.put("maxUsable", maxUsableXu);
            response.put("selectedTotal", selectedTotal);
            response.put("canUse", maxUsableXu > 0);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy thông tin One Xu");
        }
        
        return response;
    }
    
    @PostMapping("/cart/apply-onexu")
    @ResponseBody
    public Map<String, Object> applyOneXu(@RequestParam("amount") Double amount, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }
        
        try {
            // Get selected cart total
            Double selectedTotal = cartService.getSelectedCartTotalPrice(user);
            
            if (selectedTotal == null || selectedTotal <= 0) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn sản phẩm để sử dụng One Xu");
                return response;
            }
            
            // Validate amount
            Double balance = oneXuService.getUserBalance(user.getUserId());
            Double maxUsableXu = Math.min(balance, selectedTotal * 0.5);
            
            if (amount <= 0) {
                response.put("success", false);
                response.put("message", "Số lượng One Xu phải lớn hơn 0");
                return response;
            }
            
            if (amount > balance) {
                response.put("success", false);
                response.put("message", "Số dư One Xu không đủ");
                return response;
            }
            
            if (amount > maxUsableXu) {
                response.put("success", false);
                response.put("message", "Chỉ có thể sử dụng tối đa " + String.format("%.0f", maxUsableXu) + " One Xu cho đơn hàng này");
                return response;
            }
            
            // Store applied One Xu in session
            request.getSession().setAttribute("appliedOneXu", amount);
            
            response.put("success", true);
            response.put("message", "Áp dụng One Xu thành công");
            response.put("amount", amount);
            response.put("selectedTotal", selectedTotal);
            response.put("selectedCount", cartService.getSelectedCartItemCount(user));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi áp dụng One Xu");
        }
        
        return response;
    }
    
}