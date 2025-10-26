package vn.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog_comments")
public class BlogComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BlogPost post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private BlogComment parentComment;
    
    @Column(name = "author_name", columnDefinition = "NVARCHAR(100)")
    private String authorName;
    
    @Column(name = "author_email", length = 100)
    private String authorEmail;
    
    @Column(name = "content", columnDefinition = "NTEXT", nullable = false)
    private String content;
    
    @Column(name = "is_approved")
    private Boolean isApproved = false;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-Many relationship with replies
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BlogComment> replies = new ArrayList<>();
    
    // Constructors
    public BlogComment() {}
    
    public BlogComment(BlogPost post, String content) {
        this.post = post;
        this.content = content;
    }
    
    // Helper methods
    public void addReply(BlogComment reply) {
        replies.add(reply);
        reply.setParentComment(this);
    }
    
    public void removeReply(BlogComment reply) {
        replies.remove(reply);
        reply.setParentComment(null);
    }
    
    public boolean isReply() {
        return parentComment != null;
    }
    
    public boolean hasReplies() {
        return replies != null && !replies.isEmpty();
    }
    
    // Getters and Setters
    public Long getCommentId() {
        return commentId;
    }
    
    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }
    
    public BlogPost getPost() {
        return post;
    }
    
    public void setPost(BlogPost post) {
        this.post = post;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public BlogComment getParentComment() {
        return parentComment;
    }
    
    public void setParentComment(BlogComment parentComment) {
        this.parentComment = parentComment;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getAuthorEmail() {
        return authorEmail;
    }
    
    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Boolean getIsApproved() {
        return isApproved;
    }
    
    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<BlogComment> getReplies() {
        return replies;
    }
    
    public void setReplies(List<BlogComment> replies) {
        this.replies = replies;
    }
}
