package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.BlogComment;
import vn.entity.BlogPost;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {
    
    // Find comments by post and approval status
    List<BlogComment> findByPostAndIsApprovedTrueOrderByCreatedAtAsc(BlogPost post);
    
    // Find all comments by post (including unapproved)
    List<BlogComment> findByPostOrderByCreatedAtAsc(BlogPost post);
    
    // Find comments by post and parent comment (replies)
    List<BlogComment> findByPostAndParentCommentOrderByCreatedAtAsc(BlogPost post, BlogComment parentComment);
    
    // Find top-level comments (not replies)
    List<BlogComment> findByPostAndParentCommentIsNullAndIsApprovedTrueOrderByCreatedAtAsc(BlogPost post);
    
    // Find comments by user
    List<BlogComment> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find comments by author email
    List<BlogComment> findByAuthorEmailOrderByCreatedAtDesc(String authorEmail);
    
    // Find pending comments (not approved)
    List<BlogComment> findByIsApprovedFalseOrderByCreatedAtDesc();
    
    // Count comments by post
    long countByPostAndIsApprovedTrue(BlogPost post);
    
    // Count pending comments
    long countByIsApprovedFalse();
    
    // Find recent comments
    @Query("SELECT c FROM BlogComment c WHERE c.isApproved = true ORDER BY c.createdAt DESC")
    List<BlogComment> findRecentApprovedComments(org.springframework.data.domain.Pageable pageable);
    
    // Find comments by date range
    @Query("SELECT c FROM BlogComment c WHERE c.createdAt BETWEEN :startDate AND :endDate AND c.isApproved = true")
    List<BlogComment> findCommentsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}


