package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.BlogCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {
    
    // Find active categories
    List<BlogCategory> findByIsActiveTrue();
    
    // Find by slug
    Optional<BlogCategory> findByCategorySlug(String categorySlug);
    
    // Find by name
    Optional<BlogCategory> findByCategoryName(String categoryName);
    
    // Find active categories ordered by name
    List<BlogCategory> findByIsActiveTrueOrderByCategoryNameAsc();
    
    // Count posts in each category
    @Query("SELECT c, COUNT(p) FROM BlogCategory c LEFT JOIN c.posts p WHERE c.isActive = true AND (p.status = 'PUBLISHED' OR p IS NULL) GROUP BY c ORDER BY COUNT(p) DESC")
    List<Object[]> findCategoriesWithPostCount();
    
    // Find categories with at least one published post
    @Query("SELECT DISTINCT c FROM BlogCategory c JOIN c.posts p WHERE c.isActive = true AND p.status = 'PUBLISHED'")
    List<BlogCategory> findCategoriesWithPublishedPosts();
}


