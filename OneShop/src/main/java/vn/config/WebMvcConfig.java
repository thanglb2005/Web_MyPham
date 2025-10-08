package vn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ URL /images/ tới thư mục upload/images
        Path uploadDir = Paths.get(uploadPath);
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        
        // Ghi log để debug
        System.out.println("Configuring resource handler: /images/ -> " + uploadPath);
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + "/");
                
        // Các resource handler mặc định vẫn được giữ nguyên
    }
}