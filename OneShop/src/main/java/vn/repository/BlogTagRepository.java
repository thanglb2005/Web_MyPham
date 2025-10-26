package vn.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.BlogTag;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {
    
    // Find by slug
    Optional<BlogTag> findByTagSlug(String tagSlug);
    
    // Find by name
    Optional<BlogTag> findByTagName(String tagName);
    
    // Find popular tags (most used)
    @Query("SELECT t FROM BlogTag t LEFT JOIN t.posts p WHERE p.status = 'PUBLISHED' GROUP BY t ORDER BY COUNT(p) DESC")
    List<BlogTag> findPopularTags(Pageable pageable);
    
    // Find tags with at least one published post
    @Query("SELECT DISTINCT t FROM BlogTag t JOIN t.posts p WHERE p.status = 'PUBLISHED'")
    List<BlogTag> findTagsWithPublishedPosts();
    
    // Find tags by name containing (for search)
    List<BlogTag> findByTagNameContainingIgnoreCase(String tagName);
    
    // Count posts for each tag
    @Query("SELECT t, COUNT(p) FROM BlogTag t LEFT JOIN t.posts p WHERE p.status = 'PUBLISHED' OR p IS NULL GROUP BY t ORDER BY COUNT(p) DESC")
    List<Object[]> findTagsWithPostCount();
    
    // Find tags used in specific category
    @Query("SELECT DISTINCT t FROM BlogTag t JOIN t.posts p WHERE p.category.categorySlug = :categorySlug AND p.status = 'PUBLISHED'")
    List<BlogTag> findTagsByCategory(@Param("categorySlug") String categorySlug);
}


