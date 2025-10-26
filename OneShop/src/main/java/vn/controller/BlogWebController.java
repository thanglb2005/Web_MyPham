package vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.BlogPost;
import vn.repository.BlogPostRepository;
import vn.repository.BlogCategoryRepository;
import vn.repository.BlogTagRepository;

import java.util.Optional;

@Controller
@RequestMapping("/blogs")
public class BlogWebController {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogCategoryRepository blogCategoryRepository;

    @Autowired
    private BlogTagRepository blogTagRepository;

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
            RedirectAttributes redirectAttributes
    ) {
        Optional<BlogPost> postOpt = blogPostRepository.findBySlugAndStatus(
                slug, BlogPost.BlogStatus.PUBLISHED);

        if (!postOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Bài viết không tồn tại hoặc đã ẩn");
            return "redirect:/blogs";
        }

        model.addAttribute("post", postOpt.get());
        return "web/blog-detail";
    }
}


