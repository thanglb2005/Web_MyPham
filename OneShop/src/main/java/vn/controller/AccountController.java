package vn.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.entity.User;
import vn.repository.UserRepository;

@Controller
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String name,
                               @RequestParam String email,
                               HttpSession session,
                               Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        user.setName(name);
        user.setEmail(email);
        userRepository.save(user);
        session.setAttribute("user", user);
        
        model.addAttribute("success", "Cập nhật thông tin thành công!");
        return "profile";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "changePassword";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.getPassword().equals(oldPassword)) {
            model.addAttribute("error", "Mật khẩu cũ không đúng!");
            return "changePassword";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu mới không khớp!");
            return "changePassword";
        }
        
        user.setPassword(newPassword);
        userRepository.save(user);
        session.setAttribute("user", user);
        
        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "changePassword";
    }
}
