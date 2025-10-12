package vn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/test-vendor")
    public String testVendor() {
        return "test-vendor";
    }
}
