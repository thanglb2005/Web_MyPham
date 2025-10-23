package vn.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.dto.CartByShopDTO;
import vn.entity.CartItem;
import vn.entity.CartItemEntity;
import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.Product;
import vn.entity.User;
import vn.entity.OneXuTransaction;
import vn.repository.OrderDetailRepository;
import vn.repository.UserRepository;
import vn.repository.OneXuTransactionRepository;
import vn.service.CartService;
import vn.service.OrderService;
import vn.service.ProductService;
import vn.service.PromotionService;
import vn.entity.Promotion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private CartService cartService;
    
    
    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OneXuTransactionRepository oneXuTransactionRepository;

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
        
        // Refresh user from database to get latest OneXu balance
        user = userRepository.findById(user.getUserId()).orElse(user);
        // Update session with fresh data
        request.getSession().setAttribute("user", user);

        // Get cart items grouped by shop (Shopee-like)
        List<CartByShopDTO> cartItemsByShop = cartService.getCartItemsByShop(user);
        
        // Calculate overall totals
        Integer totalItems = cartService.getCartItemCount(user);
        Double totalPrice = cartService.getCartTotalPrice(user);
        Integer selectedItems = cartService.getSelectedCartItemCount(user);
        Double selectedPrice = cartService.getSelectedCartTotalPrice(user);

        // Check for applied vouchers - dual system
        Promotion oneVoucher = (Promotion) request.getSession().getAttribute("oneVoucher");
        Double oneVoucherDiscount = (Double) request.getSession().getAttribute("oneVoucherDiscount");
        Promotion shopVoucher = (Promotion) request.getSession().getAttribute("shopVoucher");
        Double shopVoucherDiscount = (Double) request.getSession().getAttribute("shopVoucherDiscount");
        
        // Check for applied xu
        Integer xuAmount = (Integer) request.getSession().getAttribute("xuAmount");
        Double xuDiscount = (Double) request.getSession().getAttribute("xuDiscount");
        
        Double finalPrice = selectedPrice;
        Double totalDiscount = 0.0;
        
        
        // Auto-clear vouchers and xu if no items selected
        if (selectedItems == 0) {
            request.getSession().removeAttribute("oneVoucher");  
            request.getSession().removeAttribute("oneVoucherDiscount");
            request.getSession().removeAttribute("shopVoucher");
            request.getSession().removeAttribute("shopVoucherDiscount");
            request.getSession().removeAttribute("xuAmount");
            request.getSession().removeAttribute("xuDiscount");
            oneVoucher = null;
            oneVoucherDiscount = null;
            shopVoucher = null;
            shopVoucherDiscount = null;
            xuAmount = null;
            xuDiscount = null;
        }
        
        // Calculate total discount from vouchers and xu
        if (selectedItems > 0) {
            if (oneVoucher != null && oneVoucherDiscount != null) {
                totalDiscount += oneVoucherDiscount;
            }
            if (shopVoucher != null && shopVoucherDiscount != null) {
                totalDiscount += shopVoucherDiscount;
            }
            if (xuAmount != null && xuDiscount != null) {
                totalDiscount += xuDiscount;
            }
            finalPrice = selectedPrice - totalDiscount;
        }
        
        // Add voucher and xu data to model
        model.addAttribute("oneVoucher", oneVoucher);
        model.addAttribute("oneVoucherDiscount", oneVoucherDiscount != null ? oneVoucherDiscount : 0.0);
        model.addAttribute("shopVoucher", shopVoucher);
        model.addAttribute("shopVoucherDiscount", shopVoucherDiscount != null ? shopVoucherDiscount : 0.0);
        model.addAttribute("xuAmount", xuAmount != null ? xuAmount : 0);
        model.addAttribute("xuDiscount", xuDiscount != null ? xuDiscount : 0.0);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("finalPrice", finalPrice);

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

        // Get voucher and xu data from session
        Promotion oneVoucher = (Promotion) request.getSession().getAttribute("oneVoucher");
        Double oneVoucherDiscount = (Double) request.getSession().getAttribute("oneVoucherDiscount");
        Promotion shopVoucher = (Promotion) request.getSession().getAttribute("shopVoucher");
        Double shopVoucherDiscount = (Double) request.getSession().getAttribute("shopVoucherDiscount");
        Promotion shippingVoucher = (Promotion) request.getSession().getAttribute("shippingVoucher");
        Double shippingVoucherDiscount = (Double) request.getSession().getAttribute("shippingVoucherDiscount");
        Integer xuAmount = (Integer) request.getSession().getAttribute("xuAmount");
        Double xuDiscount = (Double) request.getSession().getAttribute("xuDiscount");
        
        // Calculate total discount
        Double totalDiscount = 0.0;
        if (oneVoucherDiscount != null) totalDiscount += oneVoucherDiscount;
        if (shopVoucherDiscount != null) totalDiscount += shopVoucherDiscount;
        if (xuDiscount != null) totalDiscount += xuDiscount;
        
        // Calculate final price (product discount only, shipping will be handled in frontend)
        Double finalPrice = totalPrice - totalDiscount;
        if (finalPrice < 0) finalPrice = 0.0; // Safety check

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalCartItems", totalItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("oneVoucher", oneVoucher);
        model.addAttribute("oneVoucherDiscount", oneVoucherDiscount != null ? oneVoucherDiscount : 0.0);
        model.addAttribute("shopVoucher", shopVoucher);
        model.addAttribute("shopVoucherDiscount", shopVoucherDiscount != null ? shopVoucherDiscount : 0.0);
        model.addAttribute("shippingVoucher", shippingVoucher);
        model.addAttribute("shippingVoucherDiscount", shippingVoucherDiscount != null ? shippingVoucherDiscount : 0.0);
        model.addAttribute("xuAmount", xuAmount != null ? xuAmount : 0);
        model.addAttribute("xuDiscount", xuDiscount != null ? xuDiscount : 0.0);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("finalPrice", finalPrice);
        model.addAttribute("user", user);

        return "web/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("customerName") String customerName,
                                 @RequestParam("customerEmail") String customerEmail,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("address") String address,
                                 @RequestParam(value = "province", required = false) String province,
                                 @RequestParam(value = "commune", required = false) String commune,
                                 @RequestParam(value = "note", required = false) String note,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 @RequestParam(value = "city", required = false) String city,
                                 @RequestParam(value = "shippingFee", required = false, defaultValue = "0") Double shippingFee,
                                 HttpServletRequest request, Model model) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Validate phone number (Vietnam format: 10 digits, start with 03, 05, 07, 08, 09)
        String phonePattern = "^(0)(3|5|7|8|9)[0-9]{8}$";
        if (phone == null || !phone.matches(phonePattern)) {
            model.addAttribute("error", "Số điện thoại không hợp lệ.");
            return "redirect:/checkout?error=invalid-phone";
        }

        List<CartItemEntity> cartItemEntities = cartService.getSelectedCartItems(user);
        System.out.println("Selected cart items count: " + cartItemEntities.size());
        if (cartItemEntities.isEmpty()) {
            System.out.println("No selected items found, redirecting to cart");
            return "redirect:/cart?error=no-selected-items";
        }

        try {
            // Normalize payment method string to avoid casing/whitespace issues
            String normalizedPaymentMethod = paymentMethod == null ? "" : paymentMethod.trim().toLowerCase();

            Order.PaymentMethod paymentMethodEnum;
            switch (normalizedPaymentMethod) {
                case "cod":
                    paymentMethodEnum = Order.PaymentMethod.COD;
                    break;
                case "momo":
                    paymentMethodEnum = Order.PaymentMethod.MOMO;
                    break;
                case "bank_transfer":
                    paymentMethodEnum = Order.PaymentMethod.BANK_TRANSFER;
                    break;
                default:
                    // Unknown method -> fail early to avoid accidental COD
                    return "redirect:/checkout?error=Invalid payment method";
            }

            // Tạo địa chỉ đầy đủ từ các thông tin: địa chỉ cụ thể, phường/xã, tỉnh/thành phố
            StringBuilder fullAddressBuilder = new StringBuilder();
            if (address != null && !address.trim().isEmpty()) {
                fullAddressBuilder.append(address.trim());
            }
            if (commune != null && !commune.trim().isEmpty()) {
                if (fullAddressBuilder.length() > 0) {
                    fullAddressBuilder.append(", ");
                }
                fullAddressBuilder.append(commune.trim());
            }
            if (province != null && !province.trim().isEmpty()) {
                if (fullAddressBuilder.length() > 0) {
                    fullAddressBuilder.append(", ");
                }
                fullAddressBuilder.append(province.trim());
            }
            String fullAddress = fullAddressBuilder.toString();

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


            // Calculate total discount from all sources
            Double oneVoucherDiscount = (Double) request.getSession().getAttribute("oneVoucherDiscount");
            Double shopVoucherDiscount = (Double) request.getSession().getAttribute("shopVoucherDiscount");
            Double shippingVoucherDiscount = (Double) request.getSession().getAttribute("shippingVoucherDiscount");
            Double xuDiscount = (Double) request.getSession().getAttribute("xuDiscount");
            
            Double totalDiscount = 0.0;
            if (oneVoucherDiscount != null) totalDiscount += oneVoucherDiscount;
            if (shopVoucherDiscount != null) totalDiscount += shopVoucherDiscount;
            if (xuDiscount != null) totalDiscount += xuDiscount;
            
            // Build promotion code description
            String promotionDescription = "";
            if (oneVoucherDiscount != null && oneVoucherDiscount > 0) {
                Promotion oneVoucher = (Promotion) request.getSession().getAttribute("oneVoucher");
                promotionDescription += "OneShop: " + (oneVoucher != null ? oneVoucher.getPromotionCode() : "") + "; ";
            }
            if (shopVoucherDiscount != null && shopVoucherDiscount > 0) {
                Promotion shopVoucher = (Promotion) request.getSession().getAttribute("shopVoucher");
                promotionDescription += "Shop: " + (shopVoucher != null ? shopVoucher.getPromotionCode() : "") + "; ";
            }
            if (xuDiscount != null && xuDiscount > 0) {
                Integer xuAmount = (Integer) request.getSession().getAttribute("xuAmount");
                promotionDescription += "OneXu: " + (xuAmount != null ? xuAmount : 0) + " xu";
            }
            
            System.out.println("Total discount applied: " + totalDiscount);
            System.out.println("Promotion description: " + promotionDescription);

            // Get shipping voucher info
            Promotion shippingVoucher = (Promotion) request.getSession().getAttribute("shippingVoucher");
            String shippingVoucherCode = shippingVoucher != null ? shippingVoucher.getPromotionCode() : null;
            
            // Chỉ tạo order cho COD
            // MOMO và BANK_TRANSFER sẽ tạo order sau khi thanh toán thành công
            if (paymentMethodEnum == Order.PaymentMethod.COD) {
                Order order = orderService.createOrder(
                    user,
                    customerName,
                    customerEmail,
                    phone,
                    fullAddress,
                    note,
                    paymentMethodEnum,
                    cartMap,
                    promotionDescription.isEmpty() ? null : promotionDescription,
                    totalDiscount,
                    shippingFee,
                    shippingVoucherCode,
                    shippingVoucherDiscount
                );
                System.out.println("Order created successfully with ID: " + order.getOrderId());

                // Deduct xu from user balance if xu was used
                Integer xuAmount = (Integer) request.getSession().getAttribute("xuAmount");
                if (xuAmount != null && xuAmount > 0) {
                    Double currentBalance = user.getOneXuBalance() != null ? user.getOneXuBalance() : 0.0;
                    Double newBalance = currentBalance - xuAmount;
                    if (newBalance < 0) newBalance = 0.0;
                    
                    user.setOneXuBalance(newBalance);
                    // Save to database
                    userRepository.save(user);
                    
                    // Create OneXu transaction record
                    OneXuTransaction xuTransaction = new OneXuTransaction(
                        user.getUserId(),
                        OneXuTransaction.TransactionType.PURCHASE,
                        -xuAmount.doubleValue(), // Negative because it's a deduction
                        newBalance,
                        "Sử dụng " + xuAmount + " xu cho đơn hàng #" + order.getOrderId(),
                        order.getOrderId()
                    );
                    oneXuTransactionRepository.save(xuTransaction);
                    
                    // Update user in session
                    request.getSession().setAttribute("user", user);
                    
                    System.out.println("Deducted " + xuAmount + " xu. New balance: " + newBalance + ". Transaction saved.");
                }
                
                // Clear cart after successful COD order
                cartService.clearCart(user);
                
                // Clear voucher and xu session data
                request.getSession().removeAttribute("oneVoucher");
                request.getSession().removeAttribute("oneVoucherDiscount");
                request.getSession().removeAttribute("shopVoucher");
                request.getSession().removeAttribute("shopVoucherDiscount");
                request.getSession().removeAttribute("xuAmount");
                request.getSession().removeAttribute("xuDiscount");

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
                    promotionDescription.isEmpty() ? null : promotionDescription,
                    totalDiscount,
                    shippingFee,
                    shippingVoucherCode,
                    shippingVoucherDiscount
                );
                return "redirect:/payment/momo/create?orderId=" + momoOrder.getOrderId();
            } else if (paymentMethodEnum == Order.PaymentMethod.BANK_TRANSFER) {
                // Tạo order tạm cho PayOS
                Order payosOrder = orderService.createOrder(
                    user,
                    customerName,
                    customerEmail,
                    phone,
                    fullAddress,
                    note,
                    paymentMethodEnum,
                    cartMap,
                    promotionDescription.isEmpty() ? null : promotionDescription,
                    totalDiscount,
                    shippingFee,
                    shippingVoucherCode,
                    shippingVoucherDiscount
                );
                return "redirect:/payos/create-payment?orderId=" + payosOrder.getOrderId();
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

        // Load order details with product information to avoid lazy loading issues
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderIdWithProductAndShop(orderId);
        order.setOrderDetails(orderDetails);

        model.addAttribute("order", order);
        model.addAttribute("orderId", orderId);
        model.addAttribute("user", user);

        return "web/order-success";
    }

    @GetMapping("/checkout-success")
    public String checkoutSuccess(@RequestParam("orderId") Long orderId,
                                 HttpServletRequest request, Model model) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/products";
        }

        // Load order details with product information to avoid lazy loading issues
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderIdWithProductAndShop(orderId);
        order.setOrderDetails(orderDetails);

        model.addAttribute("order", order);
        model.addAttribute("orderId", orderId);
        model.addAttribute("user", user);

        return "web/checkout-success";
    }

    @GetMapping("/checkout-error")
    public String checkoutError(@RequestParam(value = "message", required = false) String message,
                               HttpServletRequest request, Model model) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String errorMessage = message != null ? message : "Có lỗi xảy ra trong quá trình thanh toán";
        model.addAttribute("error", errorMessage);
        model.addAttribute("user", user);

        return "web/checkout-error";
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
    
    /**
     * Get available promotions for voucher selection popup
     */
    /**
     * Get shop promotions by shop ID
     */
    @GetMapping("/cart/shop-promotions/{shopId}")
    @ResponseBody
    public Map<String, Object> getShopPromotions(@PathVariable Long shopId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để xem voucher shop");
                return response;
            }
            
            // Get promotions for specific shop
            List<Promotion> shopPromotions = promotionService.getPromotionsByShop(shopId);
            
            // Get shop name for modal title
            String shopName = "Shop";
            if (!shopPromotions.isEmpty() && shopPromotions.get(0).getShop() != null) {
                shopName = shopPromotions.get(0).getShop().getShopName();
            }
            
            // Create simple DTOs to avoid Hibernate proxy issues
            List<Map<String, Object>> simplifiedPromotions = new ArrayList<>();
            
            for (Promotion p : shopPromotions) {
                Map<String, Object> promoDto = new HashMap<>();
                promoDto.put("promotionId", p.getPromotionId());
                promoDto.put("promotionName", p.getPromotionName());
                promoDto.put("description", p.getDescription());
                promoDto.put("promotionCode", p.getPromotionCode());
                promoDto.put("promotionType", p.getPromotionType().name());
                promoDto.put("discountValue", p.getDiscountValue());
                promoDto.put("minimumOrderAmount", p.getMinimumOrderAmount());
                promoDto.put("maximumDiscountAmount", p.getMaximumDiscountAmount());
                promoDto.put("usageLimit", p.getUsageLimit());
                promoDto.put("usedCount", p.getUsedCount());
                promoDto.put("startDate", p.getStartDate() != null ? p.getStartDate().toString() : null);
                promoDto.put("endDate", p.getEndDate() != null ? p.getEndDate().toString() : null);
                promoDto.put("isActive", p.getIsActive());
                
                simplifiedPromotions.add(promoDto);
            }
            
            response.put("success", true);
            response.put("promotions", simplifiedPromotions);
            response.put("shopId", shopId);
            response.put("shopName", shopName);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy voucher shop: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/cart/promotions")
    @ResponseBody
    public Map<String, Object> getAvailablePromotions(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để xem voucher");
                return response;
            }
            
            // Get platform promotions only (shop_id = null, platform-wide vouchers)
            List<Promotion> activePromotions = promotionService.getAllActivePromotions()
                .stream()
                .filter(p -> p.getShop() == null) // Only platform vouchers, not shop-specific
                .collect(Collectors.toList());
            
            // Create simple DTOs to avoid Hibernate proxy issues
            List<Map<String, Object>> simplifiedPromotions = new ArrayList<>();
            
            for (Promotion p : activePromotions) {
                Map<String, Object> promoDto = new HashMap<>();
                promoDto.put("promotionId", p.getPromotionId());
                promoDto.put("promotionName", p.getPromotionName());
                promoDto.put("description", p.getDescription());
                promoDto.put("promotionCode", p.getPromotionCode());
                promoDto.put("promotionType", p.getPromotionType().name());
                promoDto.put("discountValue", p.getDiscountValue());
                promoDto.put("minimumOrderAmount", p.getMinimumOrderAmount());
                promoDto.put("maximumDiscountAmount", p.getMaximumDiscountAmount());
                promoDto.put("usageLimit", p.getUsageLimit());
                promoDto.put("usedCount", p.getUsedCount());
                promoDto.put("startDate", p.getStartDate() != null ? p.getStartDate().toString() : null);
                promoDto.put("endDate", p.getEndDate() != null ? p.getEndDate().toString() : null);
                promoDto.put("isActive", p.getIsActive());
                
                simplifiedPromotions.add(promoDto);
            }
            
            response.put("success", true);
            response.put("promotions", simplifiedPromotions);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi lấy danh sách voucher: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Apply promotion code to cart
     */
    @PostMapping("/cart/apply-promotion")
    @ResponseBody
    public Map<String, Object> applyPromotion(@RequestParam("promotionCode") String promotionCode,
                                             HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để sử dụng voucher");
                return response;
            }
            
            // Find promotion by code
            Optional<Promotion> promotionOpt = promotionService.getPromotionByCode(promotionCode);
            if (!promotionOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Mã voucher không tồn tại");
                return response;
            }
            
            Promotion promotion = promotionOpt.get();
            
            // Validate promotion
            if (!promotionService.isPromotionValid(promotionCode)) {
                response.put("success", false);
                response.put("message", "Mã voucher đã hết hạn hoặc không khả dụng");
                return response;
            }
            
            // Get selected cart items to calculate total
            List<CartItemEntity> selectedItems = cartService.getSelectedCartItems(user);
            if (selectedItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn ít nhất một sản phẩm để áp dụng voucher");
                return response;
            }
            
            double totalAmount = selectedItems.stream()
                .mapToDouble(item -> item.getTotalPrice())
                .sum();
            
            // Check minimum order amount
            if (totalAmount < promotion.getMinimumOrderAmount().doubleValue()) {
                response.put("success", false);
                response.put("message", String.format("Đơn hàng tối thiểu %,.0f VNĐ để sử dụng voucher này", 
                    promotion.getMinimumOrderAmount().doubleValue()));
                return response;
            }
            
            // Calculate discount
            double discountAmount = calculateDiscountAmount(promotion, totalAmount);
            double finalAmount = totalAmount - discountAmount;
            
            // Store promotion in session based on type
            if (promotion.getShop() == null) {
                // OneShop voucher (platform voucher)
                request.getSession().setAttribute("oneVoucher", promotion);
                request.getSession().setAttribute("oneVoucherDiscount", discountAmount);
            } else {
                // Shop voucher
                request.getSession().setAttribute("shopVoucher", promotion);
                request.getSession().setAttribute("shopVoucherDiscount", discountAmount);
            }
            
            // Create simple DTO to avoid circular reference
            Map<String, Object> promotionDto = new HashMap<>();
            promotionDto.put("promotionId", promotion.getPromotionId());
            promotionDto.put("promotionName", promotion.getPromotionName());
            promotionDto.put("promotionCode", promotion.getPromotionCode());
            promotionDto.put("promotionType", promotion.getPromotionType().name());
            promotionDto.put("discountValue", promotion.getDiscountValue());
            promotionDto.put("maximumDiscountAmount", promotion.getMaximumDiscountAmount());
            
            response.put("success", true);
            response.put("message", "Áp dụng voucher thành công");
            response.put("promotion", promotionDto);
            response.put("originalAmount", totalAmount);
            response.put("discountAmount", discountAmount);
            response.put("finalAmount", finalAmount);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi áp dụng voucher: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Remove applied promotion from cart
     */
    @PostMapping("/cart/remove-promotion")
    @ResponseBody
    public Map<String, Object> removePromotion(@RequestParam(value = "type", defaultValue = "all") String type,
                                             HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            // Remove promotion from session based on type
            if ("one".equals(type)) {
                request.getSession().removeAttribute("oneVoucher");
                request.getSession().removeAttribute("oneVoucherDiscount");
                response.put("message", "Đã bỏ OneShop voucher thành công");
            } else if ("shop".equals(type)) {
                request.getSession().removeAttribute("shopVoucher");
                request.getSession().removeAttribute("shopVoucherDiscount");
                response.put("message", "Đã bỏ shop voucher thành công");
            } else {
                // Remove all vouchers
                request.getSession().removeAttribute("oneVoucher");
                request.getSession().removeAttribute("oneVoucherDiscount");
                request.getSession().removeAttribute("shopVoucher");
                request.getSession().removeAttribute("shopVoucherDiscount");
                response.put("message", "Đã bỏ tất cả voucher thành công");
            }
            
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Apply shipping voucher
     */
    @PostMapping("/cart/apply-shipping-voucher")
    @ResponseBody
    public Map<String, Object> applyShippingVoucher(@RequestParam("promotionId") Long promotionId,
                                                   @RequestParam("shippingFee") Double shippingFee,
                                                   HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            // Find promotion
            Optional<Promotion> promotionOpt = promotionService.getPromotionById(promotionId);
            if (promotionOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Voucher không tồn tại");
                return response;
            }
            
            Promotion promotion = promotionOpt.get();
            
            // Calculate discount
            double discount = 0.0;
            switch (promotion.getPromotionType()) {
                case FREE_SHIPPING:
                    discount = shippingFee;
                    break;
                case PERCENTAGE:
                    discount = (shippingFee * promotion.getDiscountValue().doubleValue()) / 100.0;
                    if (promotion.getMaximumDiscountAmount() != null && promotion.getMaximumDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                        discount = Math.min(discount, promotion.getMaximumDiscountAmount().doubleValue());
                    }
                    break;
                case FIXED_AMOUNT:
                    discount = promotion.getDiscountValue().doubleValue();
                    break;
                default:
                    discount = 0.0;
            }
            
            // Cap by shipping fee
            discount = Math.max(0, Math.min(discount, shippingFee));
            
            // Save to session
            request.getSession().setAttribute("shippingVoucher", promotion);
            request.getSession().setAttribute("shippingVoucherDiscount", discount);
            
            response.put("success", true);
            response.put("message", "Áp dụng voucher ship thành công");
            response.put("promotion", promotion);
            response.put("discountAmount", discount);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Remove shipping voucher
     */
    @PostMapping("/cart/remove-shipping-voucher")
    @ResponseBody
    public Map<String, Object> removeShippingVoucher(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            request.getSession().removeAttribute("shippingVoucher");
            request.getSession().removeAttribute("shippingVoucherDiscount");
            
            response.put("success", true);
            response.put("message", "Đã bỏ voucher ship");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Get current voucher status from session
     */
    @GetMapping("/cart/voucher-status")
    @ResponseBody
    public Map<String, Object> getVoucherStatus(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if session exists
            HttpSession session = request.getSession(false);
            if (session == null) {
                System.out.println("No session found, returning default values");
                response.put("oneVoucher", null);
                response.put("oneVoucherDiscount", 0.0);
                response.put("shopVoucher", null);
                response.put("shopVoucherDiscount", 0.0);
                response.put("selectedPrice", 0.0);
                return response;
            }
            
            System.out.println("Session ID: " + session.getId());
            
            // Get current user and calculate selected price
            User user = (User) session.getAttribute("user");
            Double selectedPrice = 0.0;
            if (user != null) {
                selectedPrice = cartService.getSelectedCartTotalPrice(user);
            }
            
            Promotion oneVoucher = (Promotion) session.getAttribute("oneVoucher");
            Double oneVoucherDiscount = (Double) session.getAttribute("oneVoucherDiscount");
            Promotion shopVoucher = (Promotion) session.getAttribute("shopVoucher");
            Double shopVoucherDiscount = (Double) session.getAttribute("shopVoucherDiscount");
            Promotion shippingVoucher = (Promotion) session.getAttribute("shippingVoucher");
            Double shippingVoucherDiscount = (Double) session.getAttribute("shippingVoucherDiscount");
            Integer xuAmount = (Integer) session.getAttribute("xuAmount");
            Double xuDiscount = (Double) session.getAttribute("xuDiscount");
            
            System.out.println("OneVoucher: " + (oneVoucher != null ? oneVoucher.getPromotionCode() : "null"));
            System.out.println("OneVoucherDiscount: " + oneVoucherDiscount);
            System.out.println("ShopVoucher: " + (shopVoucher != null ? shopVoucher.getPromotionCode() : "null"));
            System.out.println("ShopVoucherDiscount: " + shopVoucherDiscount);
            System.out.println("XuAmount: " + xuAmount);
            System.out.println("XuDiscount: " + xuDiscount);
            System.out.println("SelectedPrice: " + selectedPrice);
            
            // Return only essential data to avoid serialization issues
            response.put("oneVoucher", oneVoucher != null ? oneVoucher.getPromotionCode() : null);
            response.put("oneVoucherDiscount", oneVoucherDiscount != null ? oneVoucherDiscount : 0.0);
            response.put("shopVoucher", shopVoucher != null ? shopVoucher.getPromotionCode() : null);
            response.put("shopVoucherDiscount", shopVoucherDiscount != null ? shopVoucherDiscount : 0.0);
            response.put("shippingVoucher", shippingVoucher != null ? shippingVoucher.getPromotionCode() : null);
            response.put("shippingVoucherDiscount", shippingVoucherDiscount != null ? shippingVoucherDiscount : 0.0);
            response.put("xuAmount", xuAmount != null ? xuAmount : 0);
            response.put("xuDiscount", xuDiscount != null ? xuDiscount : 0.0);
            response.put("selectedPrice", selectedPrice);
            
        } catch (Exception e) {
            System.out.println("Error in getVoucherStatus: " + e.getMessage());
            e.printStackTrace();
            response.put("oneVoucher", null);
            response.put("oneVoucherDiscount", 0.0);
            response.put("shopVoucher", null);
            response.put("shopVoucherDiscount", 0.0);
            response.put("xuAmount", 0);
            response.put("xuDiscount", 0.0);
            response.put("selectedPrice", 0.0);
        }
        
        return response;
    }

    /**
     * Apply shop voucher specifically from shop modal
     */
    @PostMapping("/cart/apply-shop-voucher")
    @ResponseBody
    public Map<String, Object> applyShopVoucher(@RequestParam("promotionCode") String promotionCode,
                                               HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để sử dụng voucher");
                return response;
            }
            
            // Find promotion by code
            Optional<Promotion> promotionOpt = promotionService.getPromotionByCode(promotionCode);
            if (!promotionOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Mã voucher không tồn tại");
                return response;
            }
            
            Promotion promotion = promotionOpt.get();
            
            // Validate that this is a shop voucher
            if (promotion.getShop() == null) {
                response.put("success", false);
                response.put("message", "Đây không phải voucher của shop");
                return response;
            }
            
            // Validate promotion
            if (!promotionService.isPromotionValid(promotionCode)) {
                response.put("success", false);
                response.put("message", "Mã voucher đã hết hạn hoặc không khả dụng");
                return response;
            }
            
            // Get selected cart items to calculate total
            List<CartItemEntity> selectedItems = cartService.getSelectedCartItems(user);
            if (selectedItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn ít nhất một sản phẩm để áp dụng voucher");
                return response;
            }
            
            double totalAmount = selectedItems.stream()
                .mapToDouble(item -> item.getTotalPrice())
                .sum();
            
            // Check minimum order amount
            if (totalAmount < promotion.getMinimumOrderAmount().doubleValue()) {
                response.put("success", false);
                response.put("message", String.format("Đơn hàng tối thiểu %,.0f VNĐ để sử dụng voucher này", 
                    promotion.getMinimumOrderAmount().doubleValue()));
                return response;
            }
            
            // Calculate discount
            double discountAmount = calculateDiscountAmount(promotion, totalAmount);
            double finalAmount = totalAmount - discountAmount;
            
            // Store shop voucher in session
            request.getSession().setAttribute("shopVoucher", promotion);
            request.getSession().setAttribute("shopVoucherDiscount", discountAmount);
            
            // Create promotion DTO to match OneShop voucher response structure
            Map<String, Object> promotionDto = new HashMap<>();
            promotionDto.put("promotionId", promotion.getPromotionId());
            promotionDto.put("promotionName", promotion.getPromotionName());
            promotionDto.put("promotionCode", promotion.getPromotionCode());
            promotionDto.put("promotionType", promotion.getPromotionType().name());
            promotionDto.put("discountValue", promotion.getDiscountValue());
            promotionDto.put("maximumDiscountAmount", promotion.getMaximumDiscountAmount());
            
            response.put("success", true);
            response.put("message", "Áp dụng shop voucher thành công");
            response.put("promotion", promotionDto);
            response.put("originalAmount", totalAmount);
            response.put("discountAmount", discountAmount);
            response.put("finalAmount", finalAmount);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi áp dụng voucher: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Calculate discount amount based on promotion type
     */
    private double calculateDiscountAmount(Promotion promotion, double totalAmount) {
        double discountAmount = 0;
        
        switch (promotion.getPromotionType()) {
            case PERCENTAGE:
            case PRODUCT_PERCENTAGE:
                discountAmount = totalAmount * (promotion.getDiscountValue().doubleValue() / 100.0);
                break;
            case FIXED_AMOUNT:
                discountAmount = promotion.getDiscountValue().doubleValue();
                break;
            case FREE_SHIPPING:
                // Assuming shipping cost is 30,000 VND
                discountAmount = 30000;
                break;
            case BUY_X_GET_Y:
                // This would need more complex logic based on specific products
                discountAmount = 0;
                break;
        }
        
        // Apply maximum discount limit
        if (discountAmount > promotion.getMaximumDiscountAmount().doubleValue()) {
            discountAmount = promotion.getMaximumDiscountAmount().doubleValue();
        }
        
        return discountAmount;
    }

    /**
     * Apply xu discount
     */
    @PostMapping("/cart/apply-xu")
    @ResponseBody
    public Map<String, Object> applyXu(@RequestParam("xuAmount") Integer xuAmount,
                                      HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để sử dụng xu");
                return response;
            }
            
            // Validate xu amount
            if (xuAmount == null || xuAmount <= 0) {
                response.put("success", false);
                response.put("message", "Số xu không hợp lệ");
                return response;
            }
            
            // Check if user has enough xu
            Double userXuBalance = user.getOneXuBalance() != null ? user.getOneXuBalance() : 0.0;
            if (xuAmount > userXuBalance) {
                response.put("success", false);
                response.put("message", "Số xu không đủ. Bạn chỉ có " + userXuBalance.intValue() + " xu");
                return response;
            }
            
            // Store xu discount in session (1 xu = 1 đồng)
            Double xuDiscount = xuAmount.doubleValue();
            request.getSession().setAttribute("xuAmount", xuAmount);
            request.getSession().setAttribute("xuDiscount", xuDiscount);
            
            System.out.println("Applied xu: " + xuAmount + " xu = " + xuDiscount + " đồng");
            
            response.put("success", true);
            response.put("message", "Áp dụng xu thành công");
            response.put("xuAmount", xuAmount);
            response.put("xuDiscount", xuDiscount);
            
        } catch (Exception e) {
            System.out.println("Error applying xu: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi áp dụng xu");
        }
        
        return response;
    }

    /**
     * Remove xu discount
     */
    @PostMapping("/cart/remove-xu")
    @ResponseBody
    public Map<String, Object> removeXu(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Remove xu from session
            request.getSession().removeAttribute("xuAmount");
            request.getSession().removeAttribute("xuDiscount");
            
            System.out.println("Removed xu discount from session");
            
            response.put("success", true);
            response.put("message", "Đã bỏ xu");
            
        } catch (Exception e) {
            System.out.println("Error removing xu: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi bỏ xu");
        }
        
        return response;
    }

}
