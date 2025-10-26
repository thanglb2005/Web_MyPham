package vn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
public class ViewResolverConfig {

    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/");
        resolver.setSuffix(".jsp");
        resolver.setViewClass(JstlView.class);
        // Only resolve specific JSP views, not all views
        resolver.setViewNames("admin/categories", "decorators/*");
        // Set lower priority than Thymeleaf (higher number = lower priority)
        resolver.setOrder(2);
        return resolver;
    }
}
