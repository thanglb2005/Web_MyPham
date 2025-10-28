package vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.service.GoongMapsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/goong")
@CrossOrigin(origins = "*")
public class GoongMapsController {

    @Autowired
    private GoongMapsService goongMapsService;

    /**
     * Geocoding: Chuyển đổi địa chỉ thành tọa độ
     */
    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> geocodeAddress(@RequestParam String address) {
        try {
            GoongMapsService.GeocodingResponse response = goongMapsService.geocodeAddress(address);
            
            Map<String, Object> result = new HashMap<>();
            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                GoongMapsService.GeocodingResult firstResult = response.getResults().get(0);
                result.put("success", true);
                result.put("formatted_address", firstResult.getFormattedAddress());
                result.put("lat", firstResult.getGeometry().getLocation().getLat());
                result.put("lng", firstResult.getGeometry().getLocation().getLng());
            } else {
                result.put("success", false);
                result.put("message", "Không tìm thấy địa chỉ");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi tìm kiếm địa chỉ: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * Reverse Geocoding: Chuyển đổi tọa độ thành địa chỉ
     */
    @GetMapping("/reverse-geocode")
    public ResponseEntity<Map<String, Object>> reverseGeocode(
            @RequestParam double lat, 
            @RequestParam double lng) {
        try {
            GoongMapsService.GeocodingResponse response = goongMapsService.reverseGeocode(lat, lng);
            
            Map<String, Object> result = new HashMap<>();
            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                GoongMapsService.GeocodingResult firstResult = response.getResults().get(0);
                result.put("success", true);
                result.put("formatted_address", firstResult.getFormattedAddress());
                result.put("lat", lat);
                result.put("lng", lng);
            } else {
                result.put("success", false);
                result.put("message", "Không tìm thấy địa chỉ cho tọa độ này");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi tìm kiếm địa chỉ: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * Tìm kiếm địa điểm
     */
    @GetMapping("/search-places")
    public ResponseEntity<Map<String, Object>> searchPlaces(@RequestParam String input) {
        try {
            GoongMapsService.PlacesResponse response = goongMapsService.searchPlaces(input);
            
            Map<String, Object> result = new HashMap<>();
            if (response != null && response.getPredictions() != null && !response.getPredictions().isEmpty()) {
                result.put("success", true);
                result.put("predictions", response.getPredictions());
            } else {
                result.put("success", false);
                result.put("message", "Không tìm thấy địa điểm nào");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi tìm kiếm địa điểm: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * Lấy chi tiết địa điểm
     */
    @GetMapping("/place-detail")
    public ResponseEntity<Map<String, Object>> getPlaceDetail(@RequestParam String placeId) {
        try {
            GoongMapsService.PlaceDetailResponse response = goongMapsService.getPlaceDetail(placeId);
            
            Map<String, Object> result = new HashMap<>();
            if (response != null && response.getResult() != null) {
                GoongMapsService.PlaceDetail place = response.getResult();
                result.put("success", true);
                result.put("formatted_address", place.getFormattedAddress());
                result.put("name", place.getName());
                result.put("lat", place.getGeometry().getLocation().getLat());
                result.put("lng", place.getGeometry().getLocation().getLng());
            } else {
                result.put("success", false);
                result.put("message", "Không tìm thấy chi tiết địa điểm");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi lấy chi tiết địa điểm: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
}
