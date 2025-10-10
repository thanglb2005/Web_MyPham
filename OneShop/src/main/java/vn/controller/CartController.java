package vn.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.CartItem;
import vn.entity.Order;
import vn.entity.Product;
import vn.entity.User;
import vn.service.OrderService;
import vn.service.ProductService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

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
            HttpSession session = request.getSession();

            @SuppressWarnings("unchecked")
            Map<Long, CartItem> cartMap = (Map<Long, CartItem>) session.getAttribute("cartMap");
            if (cartMap == null) {
                cartMap = new HashMap<>();
            }

            CartItem existingItem = cartMap.get(productId);
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                existingItem.updateTotalPrice();
            } else {
                CartItem newItem = new CartItem(product, quantity);
                cartMap.put(productId, newItem);
            }

            session.setAttribute("cartMap", cartMap);
            session.setAttribute("cartItems", cartMap.values());
            model.addAttribute("totalCartItems", cartMap.size());

            // Flash success message for UI feedback after redirect
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng.");
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

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cartMap = (Map<Long, CartItem>) session.getAttribute("cartMap");

        if (cartMap == null) {
            cartMap = new HashMap<>();
        }

        Collection<CartItem> cartItems = cartMap.values();
        Integer totalItems = cartMap.size();
        Double totalPrice = cartMap.values().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPrice", totalPrice);
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

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cartMap = (Map<Long, CartItem>) session.getAttribute("cartMap");

        if (cartMap != null) {
            CartItem item = cartMap.get(productId);
            if (item != null) {
                if (quantity <= 0) {
                    cartMap.remove(productId);
                } else {
                    item.setQuantity(quantity);
                    item.updateTotalPrice();
                }
                session.setAttribute("cartMap", cartMap);
                session.setAttribute("cartItems", cartMap.values());
            }
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

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cartMap = (Map<Long, CartItem>) session.getAttribute("cartMap");

        if (cartMap != null) {
            cartMap.remove(productId);
            session.setAttribute("cartMap", cartMap);
            session.setAttribute("cartItems", cartMap.values());
        }

        return "redirect:/cart";
    }

    @GetMapping("/cart/clear")
    public String clearCart(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        HttpSession session = request.getSession();
        Map<Long, CartItem> cartMap = new HashMap<>();
        session.setAttribute("cartMap", cartMap);
        session.setAttribute("cartItems", cartMap.values());

        return "redirect:/cart";
    }

    @GetMapping("/cart/count")
    @ResponseBody
    public Integer getCartCount(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return 0;
        }

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cartMap = (Map<Long, CartItem>) session.getAttribute("cartMap");

        if (cartMap == null) {
            return 0;
        }

        return cartMap.size();
    }

    @GetMapping("/checkout-debug")
    public String checkoutDebug(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cartMap = (Map<Long, CartItem>) session.getAttribute("cartMap");

        if (cartMap == null || cartMap.isEmpty()) {
            return "redirect:/cart";
        }

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

        return "web/checkout-test";
    }

    @GetMapping("/checkout-test")
    public String checkoutTest(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cartMap = (Map<Long, CartItem>) session.getAttribute("cartMap");

        if (cartMap == null || cartMap.isEmpty()) {
            return "redirect:/cart";
        }

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

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cartMap = (Map<Long, CartItem>) session.getAttribute("cartMap");

        if (cartMap == null || cartMap.isEmpty()) {
            return "redirect:/cart";
        }

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

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cartMap = (Map<Long, CartItem>) session.getAttribute("cartMap");

        if (cartMap == null || cartMap.isEmpty()) {
            return "redirect:/cart";
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

            cartMap.clear();
            session.setAttribute("cartMap", cartMap);
            session.setAttribute("cartItems", cartMap.values());

            model.addAttribute("message", "Đặt hàng thành công! Mã đơn hàng: #" + order.getOrderId());
            model.addAttribute("orderId", order.getOrderId());

            return "redirect:/order-success?orderId=" + order.getOrderId();

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại!");
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
}