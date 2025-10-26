package vn.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.service.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Streaming controller for product/brand images.
 * Uses Spring's {@link Resource} to avoid loading whole file into memory and
 * explicitly sets headers (length, cache control) so the browser does not abort the request.
 */
@Controller
public class LoadImageController {

    @Value("${upload.images.path}")
    private String pathUploadImage;
    
    @Autowired
    private StorageService storageService;

    @GetMapping("/loadImage")
    @ResponseBody
    public ResponseEntity<Resource> loadImage(@RequestParam("imageName") String imageName,
                                              HttpServletResponse response) {
        try {
            // Kiểm tra nếu là Cloudinary URL - redirect đến URL đó với transformation
            if (imageName.startsWith("http")) {
                String optimizedUrl = optimizeCloudinaryUrl(imageName);
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", optimizedUrl)
                    .build();
            }
            
            // Xử lý ảnh local như cũ
            Path imagePath = resolvePath(imageName);
            if (imagePath == null || !Files.exists(imagePath) || !Files.isRegularFile(imagePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            MediaType mediaType = MediaTypeFactory.getMediaType(imagePath.getFileName().toString())
                    .orElseGet(() -> guessMediaType(imagePath.getFileName().toString()));

            InputStream inputStream = Files.newInputStream(imagePath);
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setCacheControl(CacheControl.maxAge(java.time.Duration.ofDays(30)).cachePublic());
            headers.setContentLength(Files.size(imagePath));

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    private Path resolvePath(String imageName) throws IOException {
        Path uploadPath = Paths.get(pathUploadImage);
        if (!uploadPath.isAbsolute()) {
            uploadPath = Paths.get(System.getProperty("user.dir")).resolve(uploadPath).normalize();
        }
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath.resolve(imageName).normalize();
    }

    /**
     * Optimize Cloudinary URL for better display
     * @param cloudinaryUrl Original Cloudinary URL
     * @return Optimized URL with transformations
     */
    private String optimizeCloudinaryUrl(String cloudinaryUrl) {
        try {
            // Nếu URL đã có transformation, giữ nguyên
            if (cloudinaryUrl.contains("/w_") || cloudinaryUrl.contains("/h_") || cloudinaryUrl.contains("/c_")) {
                return cloudinaryUrl;
            }
            
            // Thêm transformation để tối ưu hiển thị
            // f_auto: tự động chọn format tốt nhất
            // q_auto: tự động chọn chất lượng
            // fl_progressive: progressive JPEG cho tải nhanh hơn
            if (cloudinaryUrl.contains("/upload/")) {
                return cloudinaryUrl.replace("/upload/", "/upload/f_auto,q_auto,fl_progressive/");
            }
            
            return cloudinaryUrl;
        } catch (Exception e) {
            // Nếu có lỗi, trả về URL gốc
            return cloudinaryUrl;
        }
    }

    private MediaType guessMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (lower.endsWith(".mp4")) return MediaType.parseMediaType("video/mp4");
        if (lower.endsWith(".webm")) return MediaType.parseMediaType("video/webm");
        if (lower.endsWith(".mov")) return MediaType.parseMediaType("video/quicktime");
        return MediaType.IMAGE_JPEG;
    }
}
