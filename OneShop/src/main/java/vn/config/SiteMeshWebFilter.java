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
        // Decorate admin pages using the admin layout
        builder.addDecoratorPath("/admin/categories", "admin.jsp");
        builder.addDecoratorPath("/admin/brands", "admin.jsp");
        builder.addDecoratorPath("/admin/providers", "admin.jsp");
        builder.addDecoratorPath("/admin/users", "admin.jsp");
        builder.addDecoratorPath("/admin/shops", "admin.jsp");
        builder.addDecoratorPath("/admin/products", "admin.jsp");
        builder.addDecoratorPath("/admin/orders", "admin.jsp");
        builder.addDecoratorPath("/admin/shippers-list", "admin.jsp");
        builder.addDecoratorPath("/admin/promotions", "admin.jsp");
        builder.addDecoratorPath("/admin/promotions/add", "admin.jsp");
        builder.addDecoratorPath("/admin/promotions/edit/*", "admin.jsp");
        builder.addDecoratorPath("/admin/promotions/view/*", "admin.jsp");
        
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
