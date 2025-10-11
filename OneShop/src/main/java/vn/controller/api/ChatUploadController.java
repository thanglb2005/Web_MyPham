package vn.controller.api;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.entity.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatUploadController {

    @Value("${upload.images.path}")
    private String uploadPath; // e.g. upload/images

    /**
     * Upload image for chat and return a URL accessible by LoadImageController
     * Only allow small image types; server returns JSON: { success, url, fileName }
     */
    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadChatImage(@RequestParam("file") MultipartFile file, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                resp.put("success", false);
                resp.put("error", "empty");
                return ResponseEntity.badRequest().body(resp);
            }

            // Optional: require authenticated user (customer or vendor)
            Object userObj = session.getAttribute("user");
            if (!(userObj instanceof User)) {
                resp.put("success", false);
                resp.put("error", "unauthorized");
                return ResponseEntity.status(401).body(resp);
            }

            String original = file.getOriginalFilename();
            String ext = ".png";
            if (original != null && original.contains(".")) {
                String candidate = original.substring(original.lastIndexOf('.')).toLowerCase();
                // allow only common image extensions
                if (candidate.matches("\\.(png|jpg|jpeg|gif|webp)")) {
                    ext = candidate;
                }
            }
            String fileName = "chat_" + UUID.randomUUID().toString().replace("-", "") + ext;

            String workingDir = System.getProperty("user.dir");
            File primaryDir = new File(workingDir + File.separatorChar + uploadPath);
            File moduleRoot = new File(workingDir + File.separator + "DoAn_Web_MyPham" + File.separator + "Web_MyPham" + File.separator + "OneShop");
            File secondaryDir = new File(moduleRoot, uploadPath);
            File targetDir = (moduleRoot.exists() ? secondaryDir : primaryDir);
            if (!targetDir.exists()) targetDir.mkdirs();

            File out = new File(targetDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(file.getBytes());
            }

            // Reuse existing /loadImage?imageName=... endpoint for serving
            String url = "/loadImage?imageName=" + fileName;
            resp.put("success", true);
            resp.put("url", url);
            resp.put("fileName", fileName);
            return ResponseEntity.ok(resp);
        } catch (IOException e) {
            resp.put("success", false);
            resp.put("error", "io_error");
            return ResponseEntity.internalServerError().body(resp);
        }
    }
}


