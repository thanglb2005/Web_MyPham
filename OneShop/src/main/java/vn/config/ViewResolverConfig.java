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
        resolver.setViewNames(
                "admin/categories",
                "admin/brands",
                "admin/providers",
                "admin/users",
                "admin/shops",
                "admin/products",
                "admin/orders",
                "admin/shippers-list",
                "admin/promotions",
                "admin/promotion-form",
                "admin/promotion-detail",
                "decorators/*");
        // Give JSP higher priority for these specific views than Thymeleaf
        resolver.setOrder(0);
        return resolver;
    }
}
