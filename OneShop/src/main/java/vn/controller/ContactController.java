package vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.service.CategoryService;

@Controller
public class ContactController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/contact")
    public String showContactPage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "web/contact";
    }

    @PostMapping("/contact/send")
    public String sendContactMessage(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            RedirectAttributes redirectAttributes) {
        
        // TODO: Implement email sending or save to database
        System.out.println("Contact form submitted:");
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        
        redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi trong thời gian sớm nhất.");
        return "redirect:/contact";
    }
}

