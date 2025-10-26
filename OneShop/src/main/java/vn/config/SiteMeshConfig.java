package vn.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.DispatcherType;
import java.util.EnumSet;

@Configuration
public class SiteMeshConfig {

    @Bean
    public FilterRegistrationBean<SiteMeshWebFilter> siteMeshFilter() {
        FilterRegistrationBean<SiteMeshWebFilter> filter = new FilterRegistrationBean<>();

        SiteMeshWebFilter siteMeshFilter = new SiteMeshWebFilter();

        filter.setFilter(siteMeshFilter);
        // Only apply to /admin/categories exactly
        filter.addUrlPatterns("/admin/categories");
        // Only decorate initial REQUESTs, avoid FORWARD/INCLUDE re-processing (prevents double decoration)
        filter.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        filter.setName("sitemesh");
        filter.setOrder(1);
        // Enable the filter
        filter.setEnabled(true);

        return filter;
    }

}
