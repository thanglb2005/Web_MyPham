package vn.controller;

import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.io.InputStream;
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

    @GetMapping("/loadImage")
    @ResponseBody
    public ResponseEntity<Resource> loadImage(@RequestParam("imageName") String imageName,
                                              HttpServletResponse response) {
        try {
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
