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
import vn.entity.User;
import vn.service.CartService;
import vn.service.OrderService;
import vn.service.ProductService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;
    

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
            // MOMO sẽ tạo order sau khi thanh toán thành công
            if (paymentMethodEnum == Order.PaymentMethod.COD || paymentMethodEnum == Order.PaymentMethod.BANK_TRANSFER) {
                Order order = orderService.createOrder(
                    user,
                    customerName,
                    customerEmail,
                    phone,
                    fullAddress,
                    note,
                    paymentMethodEnum,
                    cartMap
                );
                System.out.println("Order created successfully with ID: " + order.getOrderId());

                // Clear cart after successful order
                cartService.clearCart(user);

                model.addAttribute("message", "Đặt hàng thành công! Mã đơn hàng: #" + order.getOrderId());
                model.addAttribute("orderId", order.getOrderId());

                return "redirect:/order-success?orderId=" + order.getOrderId();
            } else if (paymentMethodEnum == Order.PaymentMethod.MOMO) {
                // Tạo order tạm cho MoMo
                Order order = orderService.createOrder(
                    user,
                    customerName,
                    customerEmail,
                    phone,
                    fullAddress,
                    note,
                    paymentMethodEnum,
                    cartMap
                );
                return "redirect:/payment/momo/create?orderId=" + order.getOrderId();
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
    
}