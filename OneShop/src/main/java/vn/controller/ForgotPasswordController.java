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
import vn.service.SendMailService;
import vn.service.UserService;

import java.util.Optional;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private HttpSession session;

    @GetMapping("/forgotPassword")
    public String showForgotPasswordForm(Model model) {
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String processForgotPassword(@RequestParam String email, Model model) {
        // Kiểm tra email có tồn tại không
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống!");
            return "forgotPassword";
        }

        User user = userOpt.get();
        
        // Tạo OTP và gửi email
        session.removeAttribute("resetOtp");
        int random_otp = (int) Math.floor(Math.random() * (999999 - 100000 + 1) + 100000);
        session.setAttribute("resetOtp", random_otp);
        session.setAttribute("resetEmail", email);
        
        System.out.println("=== FORGOT PASSWORD OTP ===");
        System.out.println("Email: " + email);
        System.out.println("OTP: " + random_otp);
        
        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                     "<h2 style='color: #007bff; text-align: center;'>OneShop - Đặt lại mật khẩu</h2>" +
                     "<p>Xin chào <strong>" + user.getName() + "</strong>,</p>" +
                     "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản OneShop của mình.</p>" +
                     "<p>Mã xác thực OTP của bạn là:</p>" +
                     "<div style='text-align: center; margin: 20px 0;'>" +
                     "<span style='background-color: #007bff; color: white; padding: 15px 30px; font-size: 24px; font-weight: bold; border-radius: 5px;'>" + random_otp + "</span>" +
                     "</div>" +
                     "<p>Mã này có hiệu lực trong 10 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>" +
                     "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>" +
                     "<hr style='margin: 20px 0;'>" +
                     "<p style='color: #666; font-size: 12px;'>OneShop Team</p>" +
                     "</div>";
        
        sendMailService.queue(email, "OneShop - Đặt lại mật khẩu", body);

        model.addAttribute("message", "Mã xác thực OTP đã được gửi tới Email: " + email + ". Hãy kiểm tra Email của bạn!");
        model.addAttribute("email", email);
        return "confirmOtpReset";
    }

    @PostMapping("/confirmOtpReset")
    public String confirmOtpReset(@RequestParam String otp, Model model) {
        String resetEmail = (String) session.getAttribute("resetEmail");
        if (resetEmail == null) {
            return "redirect:/forgotPassword";
        }

        if (otp.equals(String.valueOf(session.getAttribute("resetOtp")))) {
            // OTP đúng, chuyển đến trang đặt lại mật khẩu
            model.addAttribute("email", resetEmail);
            model.addAttribute("message", "Xác thực thành công! Vui lòng nhập mật khẩu mới.");
            return "resetPassword";
        }

        model.addAttribute("email", resetEmail);
        model.addAttribute("error", "Mã xác thực OTP không chính xác, hãy thử lại!");
        return "confirmOtpReset";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam String password,
                               @RequestParam String confirmPassword,
                               Model model) {
        String resetEmail = (String) session.getAttribute("resetEmail");
        if (resetEmail == null) {
            return "redirect:/forgotPassword";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            model.addAttribute("email", resetEmail);
            return "resetPassword";
        }

        // Cập nhật mật khẩu mới
        Optional<User> userOpt = userRepository.findByEmail(resetEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(password);
            userService.saveUser(user);
            
            // Xóa session
            session.removeAttribute("resetOtp");
            session.removeAttribute("resetEmail");
            
            model.addAttribute("message", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
            return "login";
        }

        model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        return "resetPassword";
    }
}
