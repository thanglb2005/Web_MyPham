package vn.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog_categories")
public class BlogCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "category_name", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String categoryName;
    
    @Column(name = "category_slug", nullable = false, length = 100, unique = true)
    private String categorySlug;
    
    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;
    
    @Column(name = "image_url", length = 255)
    private String imageUrl;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-Many relationship with blog posts
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BlogPost> posts = new ArrayList<>();
    
    // Constructors
    public BlogCategory() {}
    
    public BlogCategory(String categoryName, String categorySlug) {
        this.categoryName = categoryName;
        this.categorySlug = categorySlug;
    }
    
    // Helper methods
    public void addPost(BlogPost post) {
        posts.add(post);
        post.setCategory(this);
    }
    
    public void removePost(BlogPost post) {
        posts.remove(post);
        post.setCategory(null);
    }
    
    public int getPostCount() {
        return posts != null ? posts.size() : 0;
    }
    
    // Getters and Setters
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getCategorySlug() {
        return categorySlug;
    }
    
    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    public List<BlogPost> getPosts() {
        return posts;
    }
    
    public void setPosts(List<BlogPost> posts) {
        this.posts = posts;
    }
}
