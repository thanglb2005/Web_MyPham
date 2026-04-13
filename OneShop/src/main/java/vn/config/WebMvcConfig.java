package vn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Spring Boot 3 / SF 6: mặc định /path và /path/ là hai URL khác nhau.
     * Proxy hoặc link có dấu / cuối dễ gây 404 Whitelabel trên một số route admin.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Static resources mapping
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
                
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
                
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
                
        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/static/fonts/");
                
        registry.addResourceHandler("/vendor/**")
                .addResourceLocations("classpath:/static/vendor/");
                
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
                
        // Upload directories
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:upload/");
                
        // Brand images mapping
        registry.addResourceHandler("/brands/**")
                .addResourceLocations("file:upload/brands/");
                
        // Provider images mapping
        registry.addResourceHandler("/providers/**")
                .addResourceLocations("file:upload/providers/");
    }
}