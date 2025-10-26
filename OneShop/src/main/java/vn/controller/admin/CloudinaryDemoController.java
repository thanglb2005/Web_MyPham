package vn.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class CloudinaryDemoController {

    @GetMapping("/cloudinary-demo")
    public String cloudinaryDemo() {
        return "admin/cloudinary-demo";
    }
}
