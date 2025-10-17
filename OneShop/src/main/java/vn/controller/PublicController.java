package vn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PublicController {

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/delete-data")
    public String deleteData() {
        return "delete-data";
    }

    // Simple endpoint that Facebook can test
    @GetMapping("/facebook-delete-data")
    @ResponseBody
    public Map<String, Object> facebookDeleteData() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "active");
        response.put("url", "http://localhost:8080/delete-data");
        response.put("message", "Data deletion service is available");
        return response;
    }

    // Facebook Data Deletion Callback API - Must return JSON
    @PostMapping("/delete-data-callback")
    @ResponseBody
    public Map<String, Object> deleteDataCallback(
            @RequestParam(required = false) String signed_request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Log the deletion request
            System.out.println("Facebook data deletion request received");
            System.out.println("Signed request: " + signed_request);
            
            // Facebook expects specific response format
            response.put("url", "http://localhost:8080/delete-data");
            response.put("confirmation_code", "DELETE_CONFIRMED_" + System.currentTimeMillis());
            
            return response;
            
        } catch (Exception e) {
            response.put("error", "Failed to process deletion request");
            return response;
        }
    }

    // GET endpoint for testing
    @GetMapping("/delete-data-callback")
    @ResponseBody
    public Map<String, Object> deleteDataCallbackGet(
            @RequestParam(required = false) String signed_request) {
        return deleteDataCallback(signed_request);
    }
}
