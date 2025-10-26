package vn.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog_posts")
public class BlogPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;
    
    @Column(name = "title", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;
    
    @Column(name = "slug", nullable = false, length = 255, unique = true)
    private String slug;
    
    @Column(name = "excerpt", columnDefinition = "NVARCHAR(500)")
    private String excerpt;
    
    @Column(name = "content", columnDefinition = "NTEXT", nullable = false)
    private String content;
    
    @Column(name = "featured_image", length = 255)
    private String featuredImage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private BlogCategory category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private BlogStatus status = BlogStatus.DRAFT;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "meta_title", columnDefinition = "NVARCHAR(255)")
    private String metaTitle;
    
    @Column(name = "meta_description", columnDefinition = "NVARCHAR(500)")
    private String metaDescription;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Many-to-Many relationship with tags
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "blog_post_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<BlogTag> tags = new ArrayList<>();
    
    // One-to-Many relationship with comments
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BlogComment> comments = new ArrayList<>();
    
    // Constructors
    public BlogPost() {}
    
    public BlogPost(String title, String slug, String content) {
        this.title = title;
        this.slug = slug;
        this.content = content;
    }
    
    // Helper methods
    public void addTag(BlogTag tag) {
        tags.add(tag);
        tag.getPosts().add(this);
    }
    
    public void removeTag(BlogTag tag) {
        tags.remove(tag);
        tag.getPosts().remove(this);
    }
    
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null) ? 1 : this.viewCount + 1;
    }
    
    public boolean isPublished() {
        return status == BlogStatus.PUBLISHED && publishedAt != null;
    }
    
    // Getters and Setters
    public Long getPostId() {
        return postId;
    }
    
    public void setPostId(Long postId) {
        this.postId = postId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getExcerpt() {
        return excerpt;
    }
    
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getFeaturedImage() {
        return featuredImage;
    }
    
    public void setFeaturedImage(String featuredImage) {
        this.featuredImage = featuredImage;
    }
    
    public BlogCategory getCategory() {
        return category;
    }
    
    public void setCategory(BlogCategory category) {
        this.category = category;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public BlogStatus getStatus() {
        return status;
    }
    
    public void setStatus(BlogStatus status) {
        this.status = status;
    }
    
    public Boolean getIsFeatured() {
        return isFeatured;
    }
    
    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public String getMetaTitle() {
        return metaTitle;
    }
    
    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }
    
    public String getMetaDescription() {
        return metaDescription;
    }
    
    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
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
    
    public List<BlogTag> getTags() {
        return tags;
    }
    
    public void setTags(List<BlogTag> tags) {
        this.tags = tags;
    }
    
    public List<BlogComment> getComments() {
        return comments;
    }
    
    public void setComments(List<BlogComment> comments) {
        this.comments = comments;
    }
    
    // Enum for blog status
    public enum BlogStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
