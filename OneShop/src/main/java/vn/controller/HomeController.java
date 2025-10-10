package vn.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.entity.User;
import vn.entity.Shop;
import vn.repository.UserRepository;
import vn.service.CartService;
import vn.service.CategoryService;
import vn.service.ProductService;
import vn.service.ShopService;
import vn.entity.Product;

import java.util.Base64;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ShopService shopService;
    
    @Autowired
    private CartService cartService;

    @GetMapping("/")
    public String home(HttpServletRequest request, HttpSession session, Model model,
                      @RequestParam(value = "shopId", required = false) Long shopId) {
        User user = (User) session.getAttribute("user");
        
        // If no user in session, check for Remember Me cookie
        if (user == null) {
            user = checkRememberMeCookie(request);
            if (user != null) {
                session.setAttribute("user", user);
            }
        }
        
        model.addAttribute("user", user);
        model.addAttribute("categories", categoryService.getAllCategories());
        
        // Add shop list for filtering
        List<Shop> activeShops = shopService.findAll().stream()
                .filter(shop -> shop.getStatus() == Shop.ShopStatus.ACTIVE)
                .collect(Collectors.toList());
        model.addAttribute("shopList", activeShops);
        model.addAttribute("selectedShopId", shopId);
        
        // Add cart count for header
        if (user != null) {
            int cartCount = cartService.getCartItemCount(user);
            model.addAttribute("totalCartItems", cartCount);
        } else {
            model.addAttribute("totalCartItems", 0);
        }

        // Filter products by shop if shopId is provided
        List<Product> newestProducts;
        List<Product> bestSaleProducts;
        
        if (shopId != null) {
            // Filter products by shop
            newestProducts = productService.findByShopId(shopId).stream()
                    .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                    .limit(20)
                    .collect(Collectors.toList());
            
            // For best sale products, we'll use the same shop filter
            List<Object[]> bestSale = productService.bestSaleProduct20();
            if (bestSale != null && !bestSale.isEmpty()) {
                List<Long> ids = new java.util.ArrayList<>();
                for (Object[] row : bestSale) {
                    if (row != null && row.length > 0 && row[0] != null) {
                        ids.add(((Number) row[0]).longValue());
                    }
                }
                if (!ids.isEmpty()) {
                    bestSaleProducts = productService.findByInventoryIds(ids).stream()
                            .filter(p -> p.getShop() != null && shopId.equals(p.getShop().getShopId()))
                            .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                            .collect(Collectors.toList());
                } else {
                    bestSaleProducts = new java.util.ArrayList<>();
                }
            } else {
                bestSaleProducts = new java.util.ArrayList<>();
            }
        } else {
            // Show all products (original behavior)
            newestProducts = productService.listProductNew20();
            
            java.util.List<Object[]> bestSale = productService.bestSaleProduct20();
            if (bestSale != null && !bestSale.isEmpty()) {
                java.util.List<Long> ids = new java.util.ArrayList<>();
                for (Object[] row : bestSale) {
                    if (row != null && row.length > 0 && row[0] != null) {
                        ids.add(((Number) row[0]).longValue());
                    }
                }
                if (!ids.isEmpty()) {
                    bestSaleProducts = productService.findByInventoryIds(ids);
                } else {
                    bestSaleProducts = new java.util.ArrayList<>();
                }
            } else {
                bestSaleProducts = new java.util.ArrayList<>();
            }
        }

        // Newest products and best-sale products for homepage
        model.addAttribute("productList", newestProducts);
        model.addAttribute("bestSaleProduct20", bestSaleProducts);

        // Category counts for suggest slider like greeny-shop
        model.addAttribute("coutnProductByCategory", productService.listCategoryByProductName());

        return "web/home";
    }
    
    // Helper method to check Remember Me cookie (same as in LoginController)
    private User checkRememberMeCookie(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("rememberMe".equals(cookie.getName())) {
                        String encodedToken = cookie.getValue();
                        String token = new String(Base64.getDecoder().decode(encodedToken));
                        String[] parts = token.split(":");
                        
                        if (parts.length == 3 && "oneshop_remember".equals(parts[2])) {
                            Long userId = Long.parseLong(parts[0]);
                            long timestamp = Long.parseLong(parts[1]);
                            
                            // Check if token is not too old (30 days)
                            long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
                            if (System.currentTimeMillis() - timestamp < thirtyDaysInMillis) {
                                Optional<User> userOpt = userRepository.findById(userId);
                                if (userOpt.isPresent()) {
                                    return userOpt.get();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't break the app
            System.err.println("Error checking remember me cookie: " + e.getMessage());
        }
        return null;
    }
}
