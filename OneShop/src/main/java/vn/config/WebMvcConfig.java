package vn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.images.path}")
    private String uploadImagesPath;
    
    @Value("${upload.brands.path}")
    private String uploadBrandsPath;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Ánh xạ URL /images/ tới thư mục upload/images (sử dụng đường dẫn tương đối)
        // Ghi log để debug
        System.out.println("Configuring resource handler: /images/ -> " + uploadImagesPath);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadImagesPath + "/")
                .addResourceLocations("classpath:/static/images/");

        // Ánh xạ URL /brands/ tới thư mục upload/brands (sử dụng đường dẫn tương đối)
        // Ghi log để debug
        System.out.println("Configuring resource handler: /brands/ -> " + uploadBrandsPath);

        registry.addResourceHandler("/brands/**")
                .addResourceLocations("file:" + uploadBrandsPath + "/");

        // Các resource handler mặc định vẫn được giữ nguyên
    }
}