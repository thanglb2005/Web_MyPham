package vn.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.entity.OneXuTransaction;
import vn.entity.OneXuWeeklySchedule;
import vn.entity.User;
import vn.repository.UserRepository;
import vn.service.OneXuService;

import java.util.List;

@Controller
@RequestMapping("/onexu")
public class OneXuController {
    
    @Autowired
    private OneXuService oneXuService;
    
    @Autowired
    private UserRepository userRepository;
    
    // Dashboard One Xu
    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        
        // Đồng bộ hóa số dư trước khi load dashboard
        oneXuService.syncUserBalance(sessionUser.getUserId());
        
        // Load fresh user data from database to get updated One Xu balance
        User user = userRepository.findById(sessionUser.getUserId()).orElse(sessionUser);
        request.getSession().setAttribute("user", user);
        
        // Lấy thống kê One Xu
        try {
            OneXuService.OneXuStats stats = oneXuService.getOneXuStats(user.getUserId());
            model.addAttribute("stats", stats);
        } catch (Exception e) {
            // Tạo stats mặc định nếu có lỗi
            OneXuService.OneXuStats defaultStats = new OneXuService.OneXuStats(0.0, 0.0, 0.0, 0L, 0L);
            model.addAttribute("stats", defaultStats);
        }
        
        // Lấy lịch trình tuần
        try {
            List<OneXuWeeklySchedule> weeklySchedule = oneXuService.getWeeklySchedule();
            model.addAttribute("weeklySchedule", weeklySchedule);
        } catch (Exception e) {
            model.addAttribute("weeklySchedule", new java.util.ArrayList<>());
        }
        
        // Kiểm tra đã check-in hôm nay chưa
        try {
            boolean hasCheckedInToday = oneXuService.hasCheckedInToday(user.getUserId());
            model.addAttribute("hasCheckedInToday", hasCheckedInToday);
        } catch (Exception e) {
            model.addAttribute("hasCheckedInToday", false);
        }
        
        // Lấy lịch sử check-in gần đây (5 ngày gần nhất)
        try {
            List<OneXuTransaction> recentCheckins = oneXuService.getCheckinHistory(user.getUserId());
            if (recentCheckins != null && recentCheckins.size() > 5) {
                recentCheckins = recentCheckins.subList(0, 5);
            }
            model.addAttribute("recentCheckins", recentCheckins != null ? recentCheckins : new java.util.ArrayList<>());
        } catch (Exception e) {
            model.addAttribute("recentCheckins", new java.util.ArrayList<>());
        }
        
        // Lấy tất cả check-ins để hiển thị trên lịch tuần
        try {
            List<OneXuTransaction> allCheckins = oneXuService.getCheckinHistory(user.getUserId());
            model.addAttribute("allCheckins", allCheckins != null ? allCheckins : new java.util.ArrayList<>());
        } catch (Exception e) {
            model.addAttribute("allCheckins", new java.util.ArrayList<>());
        }
        
        return "web/onexu-dashboard";
    }
    
    // Check-in hàng ngày
    @PostMapping("/checkin")
    @ResponseBody
    public String checkin(HttpServletRequest request) {
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                return "{\"success\": false, \"message\": \"Vui lòng đăng nhập!\"}";
            }
            
            OneXuTransaction checkin = oneXuService.dailyCheckin(user.getUserId());
            
            // Update user in session with fresh data from database
            User freshUser = userRepository.findById(user.getUserId()).orElse(user);
            request.getSession().setAttribute("user", freshUser);
            
            return String.format("{\"success\": true, \"message\": \"Check-in thành công! Nhận được %.0f Xu\", \"xuEarned\": %.0f}", 
                               checkin.getAmount(), checkin.getAmount());
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }
    
    // Lịch sử giao dịch
    @GetMapping("/transactions")
    public String transactions(HttpServletRequest request, Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        
        // Đồng bộ hóa số dư trước khi load transactions
        oneXuService.syncUserBalance(sessionUser.getUserId());
        
        // Load fresh user data from database
        User user = userRepository.findById(sessionUser.getUserId()).orElse(sessionUser);
        
        Page<OneXuTransaction> transactions = oneXuService.getTransactionHistory(user.getUserId(), page, size);
        
        // Lấy thống kê One Xu
        try {
            OneXuService.OneXuStats stats = oneXuService.getOneXuStats(user.getUserId());
            model.addAttribute("stats", stats);
        } catch (Exception e) {
            // Tạo stats mặc định nếu có lỗi
            OneXuService.OneXuStats defaultStats = new OneXuService.OneXuStats(0.0, 0.0, 0.0, 0L, 0L);
            model.addAttribute("stats", defaultStats);
        }
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        
        return "web/onexu-transactions";
    }
    
    // Lịch sử check-in
    @GetMapping("/checkins")
    public String checkins(HttpServletRequest request, Model model) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        
        // Đồng bộ hóa số dư trước khi load checkins
        oneXuService.syncUserBalance(sessionUser.getUserId());
        
        // Load fresh user data from database
        User user = userRepository.findById(sessionUser.getUserId()).orElse(sessionUser);
        
        List<OneXuTransaction> checkins = oneXuService.getCheckinHistory(user.getUserId());
        
        // Lấy thống kê One Xu
        try {
            OneXuService.OneXuStats stats = oneXuService.getOneXuStats(user.getUserId());
            model.addAttribute("stats", stats);
        } catch (Exception e) {
            // Tạo stats mặc định nếu có lỗi
            OneXuService.OneXuStats defaultStats = new OneXuService.OneXuStats(0.0, 0.0, 0.0, 0L, 0L);
            model.addAttribute("stats", defaultStats);
        }
        
        // Lấy tổng xu từ check-in
        try {
            Double totalXuFromCheckin = oneXuService.getTotalXuFromCheckin(user.getUserId());
            model.addAttribute("totalXuFromCheckin", totalXuFromCheckin);
        } catch (Exception e) {
            model.addAttribute("totalXuFromCheckin", 0.0);
        }
        
        model.addAttribute("checkins", checkins);
        
        return "web/onexu-checkins";
    }
    
    // API lấy số dư One Xu (cho AJAX)
    @GetMapping("/balance")
    @ResponseBody
    public String getBalance(HttpServletRequest request) {
        try {
            User sessionUser = (User) request.getSession().getAttribute("user");
            if (sessionUser == null) {
                return "{\"success\": false, \"message\": \"Vui lòng đăng nhập!\"}";
            }
            
            // Load fresh user data from database
            User user = userRepository.findById(sessionUser.getUserId()).orElse(sessionUser);
            request.getSession().setAttribute("user", user);
            
            // Đồng bộ hóa số dư trước khi trả về
            oneXuService.syncUserBalance(user.getUserId());
            
            Double balance = oneXuService.getUserBalance(user.getUserId());
            return String.format("{\"success\": true, \"balance\": %.0f}", balance);
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }
    
    // API lấy thống kê One Xu (cho AJAX)
    @GetMapping("/stats")
    @ResponseBody
    public OneXuService.OneXuStats getStats(HttpServletRequest request) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            return null;
        }
        
        // Đồng bộ hóa số dư trước khi lấy stats
        oneXuService.syncUserBalance(sessionUser.getUserId());
        
        // Load fresh user data from database
        User user = userRepository.findById(sessionUser.getUserId()).orElse(sessionUser);
        request.getSession().setAttribute("user", user);
        
        return oneXuService.getOneXuStats(user.getUserId());
    }
    
    // API đồng bộ hóa số dư (cho AJAX)
    @PostMapping("/sync-balance")
    @ResponseBody
    public String syncBalance(HttpServletRequest request) {
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                return "{\"success\": false, \"message\": \"Vui lòng đăng nhập!\"}";
            }
            
            // Đồng bộ hóa số dư
            oneXuService.syncUserBalance(user.getUserId());
            
            // Load fresh user data
            User freshUser = userRepository.findById(user.getUserId()).orElse(user);
            request.getSession().setAttribute("user", freshUser);
            
            Double balance = freshUser.getOneXuBalance();
            return String.format("{\"success\": true, \"message\": \"Đồng bộ thành công!\", \"balance\": %.0f}", balance);
        } catch (Exception e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }
}
