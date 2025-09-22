package vn.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.entity.Role;
import vn.entity.User;
import vn.repository.RoleRepository;
import vn.service.SendMailService;
import vn.service.UserService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private HttpSession session;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String name,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam String confirmPassword,
                                  Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "register";
        }

        if (!checkEmail(email)) {
            model.addAttribute("error", "Email này đã được sử dụng!");
            return "register";
        }

        // Tạo OTP và gửi email
        session.removeAttribute("otp");
        int random_otp = (int) Math.floor(Math.random() * (999999 - 100000 + 1) + 100000);
        session.setAttribute("otp", random_otp);
        
        System.out.println("=== REGISTER OTP ===");
        System.out.println("Email: " + email);
        System.out.println("OTP: " + random_otp);
        
        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                     "<h2 style='color: #007bff; text-align: center;'>OneShop - Xác thực đăng ký tài khoản</h2>" +
                     "<p>Xin chào <strong>" + name + "</strong>,</p>" +
                     "<p>Chào mừng bạn đến với OneShop! Cảm ơn bạn đã đăng ký tài khoản.</p>" +
                     "<p>Để hoàn tất quá trình đăng ký, vui lòng nhập mã xác thực OTP sau:</p>" +
                     "<div style='text-align: center; margin: 20px 0;'>" +
                     "<span style='background-color: #007bff; color: white; padding: 15px 30px; font-size: 24px; font-weight: bold; border-radius: 5px;'>" + random_otp + "</span>" +
                     "</div>" +
                     "<p>Mã này có hiệu lực trong 10 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>" +
                     "<p>Nếu bạn không đăng ký tài khoản OneShop, vui lòng bỏ qua email này.</p>" +
                     "<hr style='margin: 20px 0;'>" +
                     "<p style='color: #666; font-size: 12px;'>OneShop Team</p>" +
                     "</div>";
        sendMailService.queue(email, "OneShop - Xác thực đăng ký tài khoản", body);

        // Lưu thông tin user tạm thời vào session
        User tempUser = new User();
        tempUser.setName(name);
        tempUser.setEmail(email);
        tempUser.setPassword(password);
        session.setAttribute("tempUser", tempUser);

        model.addAttribute("user", tempUser);
        model.addAttribute("message", "Mã xác thực OTP đã được gửi tới Email : " + email + " , hãy kiểm tra Email của bạn!");
        return "confirmOtpRegister";
    }

    @PostMapping("/confirmOtpRegister")
    public String confirmRegister(@RequestParam String otp, Model model) {
        User tempUser = (User) session.getAttribute("tempUser");
        if (tempUser == null) {
            return "redirect:/register";
        }

        if (otp.equals(String.valueOf(session.getAttribute("otp")))) {
            // Tạo user thật và lưu vào database
            User user = new User();
            user.setName(tempUser.getName());
            user.setEmail(tempUser.getEmail());
            user.setPassword(tempUser.getPassword()); // Simple password storage
            user.setRegisterDate(new Date());
            user.setStatus(true);
            user.setAvatar("user.png");

            Role userRole = roleRepository.findByName("ROLE_USER").orElse(null);
            if (userRole == null) {
                userRole = new Role("ROLE_USER");
                roleRepository.save(userRole);
            }
            user.setRoles(Arrays.asList(userRole));

            userService.saveUser(user);

            // Xóa session
            session.removeAttribute("otp");
            session.removeAttribute("tempUser");

            model.addAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "login";
        }

        model.addAttribute("user", tempUser);
        model.addAttribute("error", "Mã xác thực OTP không chính xác, hãy thử lại!");
        return "confirmOtpRegister";
    }

    // Check email
    public boolean checkEmail(String email) {
        List<User> list = userService.getAllUsers();
        for (User c : list) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                return false;
            }
        }
        return true;
    }
}