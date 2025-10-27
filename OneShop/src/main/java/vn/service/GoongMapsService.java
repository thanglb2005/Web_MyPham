package vn.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class GoongMapsService {

    @Value("${goong.maps.api-key}")
    private String apiKey;

    @Value("${goong.maps.geocoding-url}")
    private String geocodingUrl;

    @Value("${goong.maps.places-url}")
    private String placesUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Geocoding: Chuyển đổi địa chỉ thành tọa độ
     */
    public GeocodingResponse geocodeAddress(String address) {
        try {
            String url = UriComponentsBuilder.fromUriString(geocodingUrl)
                    .queryParam("address", address)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            return restTemplate.getForObject(url, GeocodingResponse.class);
        } catch (Exception e) {
            System.err.println("Error geocoding address: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reverse Geocoding: Chuyển đổi tọa độ thành địa chỉ
     */
    public GeocodingResponse reverseGeocode(double lat, double lng) {
        try {
            String latlng = lat + "," + lng;
            String url = UriComponentsBuilder.fromUriString(geocodingUrl)
                    .queryParam("latlng", latlng)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            return restTemplate.getForObject(url, GeocodingResponse.class);
        } catch (Exception e) {
            System.err.println("Error reverse geocoding: " + e.getMessage());
            return null;
        }
    }

    /**
     * Tìm kiếm địa điểm
     */
    public PlacesResponse searchPlaces(String input) {
        try {
            String url = UriComponentsBuilder.fromUriString(placesUrl + "/AutoComplete")
                    .queryParam("input", input)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            return restTemplate.getForObject(url, PlacesResponse.class);
        } catch (Exception e) {
            System.err.println("Error searching places: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy chi tiết địa điểm
     */
    public PlaceDetailResponse getPlaceDetail(String placeId) {
        try {
            String url = UriComponentsBuilder.fromUriString(placesUrl + "/Detail")
                    .queryParam("place_id", placeId)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            return restTemplate.getForObject(url, PlaceDetailResponse.class);
        } catch (Exception e) {
            System.err.println("Error getting place detail: " + e.getMessage());
            return null;
        }
    }

    // DTO Classes
    public static class GeocodingResponse {
        @JsonProperty("results")
        private List<GeocodingResult> results;

        public List<GeocodingResult> getResults() {
            return results;
        }

        public void setResults(List<GeocodingResult> results) {
            this.results = results;
        }
    }

    public static class GeocodingResult {
        @JsonProperty("formatted_address")
        private String formattedAddress;

        @JsonProperty("geometry")
        private Geometry geometry;

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public void setFormattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }
    }

    public static class Geometry {
        @JsonProperty("location")
        private Location location;

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

    public static class Location {
        @JsonProperty("lat")
        private double lat;

        @JsonProperty("lng")
        private double lng;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }

    public static class PlacesResponse {
        @JsonProperty("predictions")
        private List<PlacePrediction> predictions;

        public List<PlacePrediction> getPredictions() {
            return predictions;
        }

        public void setPredictions(List<PlacePrediction> predictions) {
            this.predictions = predictions;
        }
    }

    public static class PlacePrediction {
        @JsonProperty("place_id")
        private String placeId;

        @JsonProperty("description")
        private String description;

        public String getPlaceId() {
            return placeId;
        }

        public void setPlaceId(String placeId) {
            this.placeId = placeId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class PlaceDetailResponse {
        @JsonProperty("result")
        private PlaceDetail result;

        public PlaceDetail getResult() {
            return result;
        }

        public void setResult(PlaceDetail result) {
            this.result = result;
        }
    }

    public static class PlaceDetail {
        @JsonProperty("formatted_address")
        private String formattedAddress;

        @JsonProperty("name")
        private String name;

        @JsonProperty("geometry")
        private Geometry geometry;

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public void setFormattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }
    }
}
