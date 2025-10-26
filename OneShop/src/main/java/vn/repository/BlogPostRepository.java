package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.BlogPost;
import vn.entity.BlogCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    
    // Find published posts
    Page<BlogPost> findByStatus(BlogPost.BlogStatus status, Pageable pageable);
    
    // Find by slug and status
    Optional<BlogPost> findBySlugAndStatus(String slug, BlogPost.BlogStatus status);
    
    // Find by category and status
    Page<BlogPost> findByCategoryCategorySlugAndStatus(String categorySlug, BlogPost.BlogStatus status, Pageable pageable);
    
    // Find by tag and status
    @Query("SELECT p FROM BlogPost p JOIN p.tags t WHERE t.tagSlug = :tagSlug AND p.status = :status")
    Page<BlogPost> findByTagsTagSlugAndStatus(@Param("tagSlug") String tagSlug, @Param("status") BlogPost.BlogStatus status, Pageable pageable);
    
    // Search in title and content
    @Query("SELECT p FROM BlogPost p WHERE (LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%'))) AND p.status = :status")
    Page<BlogPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndStatus(@Param("search") String search, @Param("status") BlogPost.BlogStatus status, Pageable pageable);
    
    // Find featured posts
    List<BlogPost> findByIsFeaturedTrueAndStatus(BlogPost.BlogStatus status, Pageable pageable);
    
    // Find related posts (same category, excluding current post)
    List<BlogPost> findByCategoryAndStatusAndPostIdNot(BlogCategory category, BlogPost.BlogStatus status, Long postId, Pageable pageable);
    
    // Find recent posts
    List<BlogPost> findByStatusOrderByPublishedAtDesc(BlogPost.BlogStatus status, Pageable pageable);
    
    // Find by author
    Page<BlogPost> findByAuthorUserIdAndStatus(Long authorId, BlogPost.BlogStatus status, Pageable pageable);
    
    // Count posts by status
    long countByStatus(BlogPost.BlogStatus status);
    
    // Count posts by category
    long countByCategoryAndStatus(BlogCategory category, BlogPost.BlogStatus status);
    
    // Find most viewed posts
    @Query("SELECT p FROM BlogPost p WHERE p.status = :status ORDER BY p.viewCount DESC")
    List<BlogPost> findMostViewedPosts(@Param("status") BlogPost.BlogStatus status, Pageable pageable);
    
    // Find posts by date range
    @Query("SELECT p FROM BlogPost p WHERE p.publishedAt BETWEEN :startDate AND :endDate AND p.status = :status")
    Page<BlogPost> findByPublishedAtBetweenAndStatus(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate, @Param("status") BlogPost.BlogStatus status, Pageable pageable);
    
    // Admin search - search in title and content (all posts)
    @Query("SELECT p FROM BlogPost p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<BlogPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}


