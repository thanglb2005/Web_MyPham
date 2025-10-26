package vn.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Controller for search autocomplete
 * @author OneShop Team
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    /**
     * Autocomplete endpoint
     * Returns suggestions based on search query
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<Map<String, String>>> autocomplete(@RequestParam(required = false, defaultValue = "") String q) {
        List<Map<String, String>> suggestions = new ArrayList<>();
        
        // Simple mock data for now
        // In production, query from database
        String query = (q != null ? q : "").toLowerCase();
        
        if (query.length() >= 2) {
            // Mock suggestions
            List<String> products = List.of(
                "Son môi", "Kem dưỡng da", "Nước hoa", "Serum", 
                "Kem chống nắng", "Toner", "Mặt nạ", "Kem dưỡng ẩm"
            );
            
            for (String product : products) {
                if (product.toLowerCase().contains(query)) {
                    Map<String, String> suggestion = new HashMap<>();
                    suggestion.put("name", product);
                    suggestions.add(suggestion);
                }
            }
        }
        
        return ResponseEntity.ok(suggestions);
    }
}
