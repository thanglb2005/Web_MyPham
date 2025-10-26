package vn.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import vn.entity.BlogPost;
import vn.entity.BlogCategory;
import vn.entity.BlogTag;
import vn.entity.User;
import vn.repository.BlogPostRepository;
import vn.repository.BlogCategoryRepository;
import vn.repository.BlogTagRepository;
import vn.repository.UserRepository;
import vn.service.CloudinaryService;
import jakarta.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin/blog")
public class BlogController {
    
    @Autowired
    private BlogPostRepository blogPostRepository;
    
    @Autowired
    private BlogCategoryRepository blogCategoryRepository;
    
    @Autowired
    private BlogTagRepository blogTagRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;
    
    /**
     * Admin - Danh sách tất cả blog posts
     */
    @GetMapping
    public String adminBlogList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<BlogPost> blogPosts;
            
            if (search != null && !search.isEmpty()) {
                blogPosts = blogPostRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                    search, pageable);
            } else {
                blogPosts = blogPostRepository.findAll(pageable);
            }
            
            model.addAttribute("posts", blogPosts);
            model.addAttribute("search", search);
            return "admin/blog-list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "admin/blog-list";
        }
    }

    /**
     * Người dùng hủy soạn: xóa toàn bộ ảnh tạm vừa upload
     */
    @GetMapping("/cancel-edit")
    public String cancelEdit(HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            Set<String> temp = (Set<String>) session.getAttribute("BLOG_EDITOR_TEMP_IMAGES");
            if (temp != null) {
                for (String url : temp) {
                    try { cloudinaryService.deleteImageByUrl(url); } catch (Exception ignore) {}
                }
                session.removeAttribute("BLOG_EDITOR_TEMP_IMAGES");
            }
        } catch (Exception ignore) {}
        return "redirect:/admin/blog";
    }

    private void cleanupTempEditorImages(HttpSession session, String contentHtml) {
        try {
            @SuppressWarnings("unchecked")
            Set<String> temp = (Set<String>) session.getAttribute("BLOG_EDITOR_TEMP_IMAGES");
            if (temp == null || temp.isEmpty()) return;

            Set<String> used = extractImageUrls(contentHtml);
            for (String url : new HashSet<>(temp)) {
                if (!used.contains(url)) {
                    try { cloudinaryService.deleteImageByUrl(url); } catch (Exception ignore) {}
                    temp.remove(url);
                }
            }
            session.removeAttribute("BLOG_EDITOR_TEMP_IMAGES");
        } catch (Exception ignore) {}
    }

    private Set<String> extractImageUrls(String html) {
        Set<String> urls = new HashSet<>();
        if (html == null) return urls;
        Pattern p = Pattern.compile("<img[^>]+src\\=\\\"([^\\\"]+)\\\"", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(html);
        while (m.find()) urls.add(m.group(1));
        return urls;
    }
    /**
     * Admin - Form tạo blog mới
     */
    @GetMapping("/create")
    public String adminCreateBlogForm(Model model) {
        List<BlogCategory> categories = blogCategoryRepository.findAll();
        List<BlogTag> tags = blogTagRepository.findAll();
        
        model.addAttribute("post", new BlogPost());
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        return "admin/blog-form";
    }

    /**
     * Admin - Tạo blog mới
     */
    @PostMapping("/create")
    public String adminCreateBlog(
            @ModelAttribute BlogPost post,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(value = "featuredImageFile", required = false) MultipartFile featuredImageFile,
            Principal principal,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        try {
            // Set author
            if (principal != null) {
                User author = userRepository.findByEmail(principal.getName()).orElse(null);
                if (author != null) {
                    post.setAuthor(author);
                }
            }
            
            // Set timestamps
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            // Sanitize content: remove heading tags before save
            post.setContent(sanitizeHeadingTags(post.getContent()));
            
            // Set tags
            if (tagIds != null && !tagIds.isEmpty()) {
                List<BlogTag> tags = new ArrayList<>();
                for (Long tagId : tagIds) {
                    blogTagRepository.findById(tagId).ifPresent(tags::add);
                }
                post.setTags(tags);
            }
            
            // Upload featured image to Cloudinary if provided
            if (featuredImageFile != null && !featuredImageFile.isEmpty()) {
                try {
                    String url = cloudinaryService.uploadImageToFolder(featuredImageFile, "blogs");
                    post.setFeaturedImage(url);
                } catch (Exception ex) {
                    redirectAttributes.addFlashAttribute("error", "Upload ảnh thất bại: " + ex.getMessage());
                    return "redirect:/admin/blog/create";
                }
            }

            // Auto-generate slug if empty
            if (post.getSlug() == null || post.getSlug().isEmpty()) {
                post.setSlug(generateSlug(post.getTitle()));
            }
            
            blogPostRepository.save(post);

            // Cleanup temp editor images not used
            cleanupTempEditorImages(session, post.getContent());
            redirectAttributes.addFlashAttribute("success", "Tạo bài viết thành công!");
            return "redirect:/admin/blog";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo bài viết: " + e.getMessage());
            return "redirect:/admin/blog/create";
        }
    }

    /**
     * Upload ảnh dán/kéo-thả từ Summernote
     */
    @PostMapping("/upload-image")
    @ResponseBody
    public java.util.Map<String, Object> uploadEditorImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "image", required = false) MultipartFile image,
            HttpSession session) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            MultipartFile f = (file != null && !file.isEmpty()) ? file : image;
            if (f == null || f.isEmpty()) {
                result.put("error", "File rỗng");
                return result;
            }
            // Use simple URL-returning helper for maximum compatibility
            String url = cloudinaryService.uploadImageToFolder(f, "blogs");
            result.put("url", url);

            // Track as temp for cleanup on cancel
            @SuppressWarnings("unchecked")
            Set<String> temp = (Set<String>) session.getAttribute("BLOG_EDITOR_TEMP_IMAGES");
            if (temp == null) temp = new HashSet<>();
            if (url != null) temp.add(url);
            session.setAttribute("BLOG_EDITOR_TEMP_IMAGES", temp);
            return result;
        } catch (Exception ex) {
            result.put("error", ex.getMessage());
            return result;
        }
    }

    /**
     * Admin - Form sửa blog
     */
    @GetMapping("/edit/{id}")
    public String adminEditBlogForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<BlogPost> postOpt = blogPostRepository.findById(id);
        if (!postOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy bài viết");
            return "redirect:/admin/blog";
        }
        
        List<BlogCategory> categories = blogCategoryRepository.findAll();
        List<BlogTag> tags = blogTagRepository.findAll();
        
        model.addAttribute("post", postOpt.get());
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        return "admin/blog-form";
    }

    /**
     * Admin - Cập nhật blog
     */
    @PostMapping("/edit/{id}")
    public String adminUpdateBlog(
            @PathVariable Long id,
            @ModelAttribute BlogPost post,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(value = "featuredImageFile", required = false) MultipartFile featuredImageFile,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        try {
            Optional<BlogPost> existingPostOpt = blogPostRepository.findById(id);
            if (!existingPostOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy bài viết");
                return "redirect:/admin/blog";
            }
            
            BlogPost existingPost = existingPostOpt.get();
            
            // Update fields
            existingPost.setTitle(post.getTitle());
            existingPost.setSlug(post.getSlug());
            existingPost.setExcerpt(post.getExcerpt());
            existingPost.setContent(sanitizeHeadingTags(post.getContent()));
            // Featured image: prioritize uploaded file; otherwise keep text value
            if (featuredImageFile != null && !featuredImageFile.isEmpty()) {
                try {
                    String url = cloudinaryService.uploadImageToFolder(featuredImageFile, "blogs");
                    existingPost.setFeaturedImage(url);
                } catch (Exception ex) {
                    redirectAttributes.addFlashAttribute("error", "Upload ảnh thất bại: " + ex.getMessage());
                    return "redirect:/admin/blog/edit/" + id;
                }
            } else {
                existingPost.setFeaturedImage(post.getFeaturedImage());
            }
            existingPost.setCategory(post.getCategory());
            existingPost.setStatus(post.getStatus());
            existingPost.setIsFeatured(post.getIsFeatured());
            existingPost.setMetaTitle(post.getMetaTitle());
            existingPost.setMetaDescription(post.getMetaDescription());
            existingPost.setUpdatedAt(LocalDateTime.now());
            
            // Set tags
            if (tagIds != null && !tagIds.isEmpty()) {
                List<BlogTag> tags = new ArrayList<>();
                for (Long tagId : tagIds) {
                    blogTagRepository.findById(tagId).ifPresent(tags::add);
                }
                existingPost.setTags(tags);
            } else {
                existingPost.setTags(new ArrayList<>());
            }
            
            blogPostRepository.save(existingPost);

            // Cleanup temp editor images not used
            cleanupTempEditorImages(session, existingPost.getContent());
            redirectAttributes.addFlashAttribute("success", "Cập nhật bài viết thành công!");
            return "redirect:/admin/blog";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật bài viết: " + e.getMessage());
            return "redirect:/admin/blog/edit/" + id;
        }
    }

    /**
     * Admin - Xóa blog
     */
    @PostMapping("/delete/{id}")
    public String adminDeleteBlog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<BlogPost> postOpt = blogPostRepository.findById(id);
            if (!postOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy bài viết");
                return "redirect:/admin/blog";
            }
            
            blogPostRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa bài viết thành công!");
            return "redirect:/admin/blog";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa bài viết: " + e.getMessage());
            return "redirect:/admin/blog";
        }
    }
    
    /**
     * Generate slug from title
     */
    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }
        
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }

    // Remove heading tags (h1-h6) from content to avoid oversized text on render
    private String sanitizeHeadingTags(String html) {
        if (html == null) return null;
        try {
            // Replace <h1...>...</h1> with <p>...</p>
            String result = html;
            for (int i = 1; i <= 6; i++) {
                result = result.replaceAll("(?is)<h" + i + "[^>]*>", "<p>");
                result = result.replaceAll("(?is)</h" + i + ">", "</p>");
            }
            return result;
        } catch (Exception e) {
            return html;
        }
    }
}
