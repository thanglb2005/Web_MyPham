package vn.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;

/**
 * SiteMesh 3 filter configured programmatically.
 * Applies the admin decorator only to /admin/categories.
 */
public class SiteMeshWebFilter extends ConfigurableSiteMeshFilter {

    @Override
    protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
        // Only decorate the admin categories page using the admin layout
        builder.addDecoratorPath("/admin/categories", "admin.jsp");
        
        // Exclude static resources and other paths
        builder.addExcludedPath("/assets/*");
        builder.addExcludedPath("/css/*");
        builder.addExcludedPath("/js/*");
        builder.addExcludedPath("/images/*");
        builder.addExcludedPath("/fonts/*");
        builder.addExcludedPath("/vendor/*");
        builder.addExcludedPath("/WEB-INF/decorators/*");
        builder.addExcludedPath("/error");
    }
}
