package vn.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.util.SlugUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class ImageStorageService {

    @Value("${upload.path}")
    private String uploadPath;

    public String store(MultipartFile file, String referenceName) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        File targetDir = resolveUploadDirectory();
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        String extension = extractExtension(file.getOriginalFilename());
        String slug = SlugUtils.toSlug(referenceName);
        if (slug.isEmpty()) {
            slug = "image";
        }
        String fileName = slug + "_" + System.currentTimeMillis() + extension;

        File destination = new File(targetDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(destination)) {
            fos.write(file.getBytes());
        }
        return fileName;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return originalFilename.substring(dotIndex);
    }

    private File resolveUploadDirectory() {
        String workingDir = System.getProperty("user.dir");
        File primary = new File(workingDir + File.separatorChar + uploadPath);
        File moduleRoot = new File(workingDir + File.separator + "DoAn_Web_MyPham" + File.separator + "Web_MyPham" + File.separator + "OneShop");
        File secondary = new File(moduleRoot, uploadPath);
        return moduleRoot.exists() ? secondary : primary;
    }
}

