package vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.BlogPost;
import vn.entity.BlogComment;
import vn.entity.User;
import vn.repository.BlogPostRepository;
import vn.repository.BlogCategoryRepository;
import vn.repository.BlogTagRepository;
import vn.repository.BlogCommentRepository;
import vn.repository.UserRepository;

import java.util.Optional;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
@RequestMapping("/blogs")
public class BlogWebController {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogCategoryRepository blogCategoryRepository;

    @Autowired
    private BlogTagRepository blogTagRepository;

    @Autowired
    private BlogCommentRepository blogCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String blogList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<BlogPost> blogPosts;

        if (search != null && !search.isEmpty()) {
            blogPosts = blogPostRepository
                    .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndStatus(
                            search, BlogPost.BlogStatus.PUBLISHED, pageable);
        } else {
            blogPosts = blogPostRepository.findByStatus(BlogPost.BlogStatus.PUBLISHED, pageable);
        }

        model.addAttribute("posts", blogPosts);
        model.addAttribute("search", search);
        // Sidebar data
        model.addAttribute("categories", blogCategoryRepository.findAll());
        model.addAttribute("tags", blogTagRepository.findAll());
        return "web/blog-list";
    }

    @GetMapping("/{slug}")
    public String blogDetail(
            @PathVariable String slug,
            Model model,
            RedirectAttributes redirectAttributes,
            Principal principal
    ) {
        Optional<BlogPost> postOpt = blogPostRepository.findBySlugAndStatus(
                slug, BlogPost.BlogStatus.PUBLISHED);

        if (!postOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Bài viết không tồn tại hoặc đã ẩn");
            return "redirect:/blogs";
        }
        BlogPost post = postOpt.get();
        model.addAttribute("post", post);
        // Approved top-level comments
        model.addAttribute("comments",
                blogCommentRepository.findByPostAndParentCommentIsNullAndIsApprovedTrueOrderByCreatedAtAsc(post));
        // Total comment count (including replies)
        model.addAttribute("commentCount", blogCommentRepository.countByPostAndIsApprovedTrue(post));
        model.addAttribute("isAuthenticated", isAuthenticated());
        return "web/blog-detail";
    }

    /**
     * Post a comment - requires user login
     */
    @PostMapping("/{slug}/comment")
    public String postComment(
            @PathVariable String slug,
            @RequestParam("content") String content,
            @RequestParam(value = "parentCommentId", required = false) Long parentCommentId,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        Optional<BlogPost> postOpt = blogPostRepository.findBySlugAndStatus(
                slug, BlogPost.BlogStatus.PUBLISHED);
        if (!postOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Bài viết không tồn tại hoặc đã ẩn");
            return "redirect:/blogs";
        }

        if (!isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để bình luận");
            return "redirect:/login";
        }

        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nội dung bình luận không được để trống");
            return "redirect:/blogs/" + slug + "#comments";
        }

        BlogPost post = postOpt.get();
        User user = resolveCurrentUser(principal);

        BlogComment comment = new BlogComment();
        comment.setPost(post);
        comment.setUser(user);
        String[] nameEmail = resolveDisplayNameAndEmail(user);
        comment.setAuthorName(nameEmail[0]);
        comment.setAuthorEmail(nameEmail[1]);
        comment.setContent(content.trim());
        comment.setIsApproved(true); // publish immediately for logged-in users

        if (parentCommentId != null) {
            blogCommentRepository.findById(parentCommentId).ifPresent(comment::setParentComment);
        }

        blogCommentRepository.save(comment);
        redirectAttributes.addFlashAttribute("success", "Đã gửi bình luận");
        return "redirect:/blogs/" + slug + "#comments";
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private User resolveCurrentUser(Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object p = auth.getPrincipal();
            if (p instanceof UserDetails) {
                email = ((UserDetails) p).getUsername();
            } else if (p instanceof OAuth2User) {
                Object em = ((OAuth2User) p).getAttributes().get("email");
                email = em != null ? em.toString() : auth.getName();
            } else {
                email = auth.getName();
            }
        } else if (principal != null) {
            email = principal.getName();
        }
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    private String[] resolveDisplayNameAndEmail(User user) {
        String displayName = null;
        String email = null;
        if (user != null) {
            displayName = user.getName();
            email = user.getEmail();
        }
        if (displayName == null || displayName.isEmpty() || email == null || email.isEmpty()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                Object p = auth.getPrincipal();
                if (p instanceof OAuth2User) {
                    OAuth2User o = (OAuth2User) p;
                    if (displayName == null) {
                        Object nameAttr = o.getAttributes().get("name");
                        if (nameAttr != null) displayName = nameAttr.toString();
                    }
                    if (email == null) {
                        Object emailAttr = o.getAttributes().get("email");
                        if (emailAttr != null) email = emailAttr.toString();
                    }
                }
                if ((displayName == null || displayName.isEmpty())) displayName = auth.getName();
            }
        }
        return new String[] { displayName != null ? displayName : "Người dùng", email };
    }
}


