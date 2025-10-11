package vn.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.entity.Favorite;
import vn.entity.User;
import vn.service.FavoriteService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Favorite functionality
 * @author OneShop Team
 */
@Controller
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * Display favorites page
     */
    @GetMapping("/favorites")
    public String favorites(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Favorite> favorites = favoriteService.getFavoritesByUser(user.getUserId());
        model.addAttribute("favorites", favorites);
        model.addAttribute("user", user);
        return "web/favorites";
    }

    /**
     * Add product to favorites
     */
    @PostMapping("/addToFavorites")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToFavorites(@RequestParam("productId") Long productId, 
                                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để sử dụng chức năng này");
            return ResponseEntity.ok(response);
        }

        boolean success = favoriteService.addToFavorites(productId, user.getUserId());
        response.put("success", success);
        response.put("message", success ? "Đã thêm vào yêu thích" : "Sản phẩm đã có trong yêu thích");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Remove product from favorites
     */
    @PostMapping("/removeFromFavorites")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromFavorites(@RequestParam("productId") Long productId, 
                                                                   HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để sử dụng chức năng này");
            return ResponseEntity.ok(response);
        }

        boolean success = favoriteService.removeFromFavorites(productId, user.getUserId());
        response.put("success", success);
        response.put("message", success ? "Đã xóa khỏi yêu thích" : "Không thể xóa khỏi yêu thích");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Toggle favorite status
     */
    @PostMapping("/toggleFavorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(@RequestParam("productId") Long productId, 
                                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để sử dụng chức năng này");
            return ResponseEntity.ok(response);
        }

        // Check current status BEFORE toggle
        boolean wasFavorited = favoriteService.isFavorited(productId, user.getUserId());
        
        // Perform toggle
        boolean success = favoriteService.toggleFavorite(productId, user.getUserId());
        
        // After toggle, the status is opposite of what it was before
        boolean isFavorited = !wasFavorited;
        
        response.put("success", success);
        response.put("isFavorited", isFavorited);
        response.put("message", isFavorited ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Check if product is favorited
     */
    @GetMapping("/checkFavorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkFavorite(@RequestParam("productId") Long productId, 
                                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("isFavorited", false);
            return ResponseEntity.ok(response);
        }

        boolean isFavorited = favoriteService.isFavorited(productId, user.getUserId());
        response.put("isFavorited", isFavorited);
        
        return ResponseEntity.ok(response);
    }
}

