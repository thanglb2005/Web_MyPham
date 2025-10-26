package vn.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog_tags")
public class BlogTag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;
    
    @Column(name = "tag_name", nullable = false, columnDefinition = "NVARCHAR(50)", unique = true)
    private String tagName;
    
    @Column(name = "tag_slug", nullable = false, length = 50, unique = true)
    private String tagSlug;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Many-to-Many relationship with blog posts
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private List<BlogPost> posts = new ArrayList<>();
    
    // Constructors
    public BlogTag() {}
    
    public BlogTag(String tagName, String tagSlug) {
        this.tagName = tagName;
        this.tagSlug = tagSlug;
    }
    
    // Helper methods
    public void addPost(BlogPost post) {
        posts.add(post);
        post.getTags().add(this);
    }
    
    public void removePost(BlogPost post) {
        posts.remove(post);
        post.getTags().remove(this);
    }
    
    public int getPostCount() {
        return posts != null ? posts.size() : 0;
    }
    
    // Getters and Setters
    public Long getTagId() {
        return tagId;
    }
    
    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
    
    public String getTagName() {
        return tagName;
    }
    
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    public String getTagSlug() {
        return tagSlug;
    }
    
    public void setTagSlug(String tagSlug) {
        this.tagSlug = tagSlug;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<BlogPost> getPosts() {
        return posts;
    }
    
    public void setPosts(List<BlogPost> posts) {
        this.posts = posts;
    }
}
