package vn.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.Order;
import vn.entity.Comment;
import vn.entity.OrderDetail;
import vn.entity.Product;
import vn.entity.User;
import vn.repository.OrderDetailRepository;
import vn.repository.ProductRepository;
import vn.service.CommentService;
import vn.service.ImageStorageService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    // Trang đánh giá: truyền orderDetailId để khóa vào mục đã mua
    @GetMapping("/review/{orderDetailId}")
    public String reviewForm(@PathVariable Long orderDetailId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<OrderDetail> odOpt = orderDetailRepository.findById(orderDetailId);
        if (odOpt.isEmpty()) return "redirect:/user/my-orders";

        OrderDetail orderDetail = odOpt.get();
        Order order = orderDetail.getOrder();
        if (order == null || !order.getUser().getUserId().equals(user.getUserId())) return "redirect:/user/my-orders";
        if (order.getStatus() != Order.OrderStatus.DELIVERED) return "redirect:/user/my-orders";

        model.addAttribute("orderDetail", orderDetail);
        model.addAttribute("product", orderDetail.getProduct());
        return "web/review";
    }

    @PostMapping("/reviews")
    public String submitReview(@RequestParam("orderDetailId") Long orderDetailId,
                               @RequestParam("productId") Long productId,
                               @RequestParam("rating") Double rating,
                               @RequestParam("content") String content,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               @RequestParam(value = "videos", required = false) List<MultipartFile> videos,
                               @RequestParam(value = "redirect", required = false) String redirect,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<OrderDetail> odOpt = orderDetailRepository.findById(orderDetailId);
        if (odOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm trong đơn hàng");
            return redirect != null ? "redirect:" + redirect : "redirect:/user/my-orders";
        }
        OrderDetail orderDetail = odOpt.get();
        Order order = orderDetail.getOrder();
        if (order == null || !order.getUser().getUserId().equals(user.getUserId()))
            return "redirect:/user/my-orders";
        if (order.getStatus() != Order.OrderStatus.DELIVERED)
            return "redirect:/user/my-orders";

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return "redirect:/user/my-orders";

        // Lưu comment chính
        Comment c = commentService.createComment(user, product, orderDetail, content, rating);

        // Lưu file media nếu có
        try {
            if (images != null) {
                for (MultipartFile f : images) {
                    String stored = imageStorageService.store(f, product.getProductName());
                    if (stored != null && !stored.isBlank()) {
                        vn.entity.CommentMedia media = new vn.entity.CommentMedia();
                        media.setComment(c);
                        media.setMediaType("IMAGE");
                        media.setUrl(stored);
                        commentService.saveMedia(media);
                    }
                }
            }
            if (videos != null) {
                for (MultipartFile f : videos) {
                    String stored = imageStorageService.store(f, product.getProductName());
                    if (stored != null && !stored.isBlank()) {
                        vn.entity.CommentMedia media = new vn.entity.CommentMedia();
                        media.setComment(c);
                        media.setMediaType("VIDEO");
                        media.setUrl(stored);
                        commentService.saveMedia(media);
                    }
                }
            }
        } catch (IOException ignored) {}

        redirectAttributes.addFlashAttribute("success", "Đã gửi đánh giá thành công");
        return redirect != null ? "redirect:" + redirect : "redirect:/user/my-orders";
    }

    /**
     * Gửi đánh giá trực tiếp từ trang chi tiết sản phẩm (không yêu cầu orderDetail)
     */
    @PostMapping("/reviews/product")
    public String submitProductReview(@RequestParam("productId") Long productId,
                                      @RequestParam("rating") Double rating,
                                      @RequestParam("content") String content,
                                      @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                      @RequestParam(value = "videos", required = false) List<MultipartFile> videos,
                                      @RequestParam(value = "redirect", required = false) String redirect,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return "redirect:/";

        Comment c = commentService.createComment(user, product, null, content, rating);

        // Lưu file media nếu có (ảnh/video) – tái sử dụng ImageStorageService
        try {
            if (images != null) {
                for (MultipartFile f : images) {
                    String stored = imageStorageService.store(f, product.getProductName());
                    if (stored != null && !stored.isBlank()) {
                        vn.entity.CommentMedia media = new vn.entity.CommentMedia();
                        media.setComment(c);
                        media.setMediaType("IMAGE");
                        media.setUrl(stored);
                        commentService.saveMedia(media);
                    }
                }
            }
            if (videos != null) {
                for (MultipartFile f : videos) {
                    String stored = imageStorageService.store(f, product.getProductName());
                    if (stored != null && !stored.isBlank()) {
                        vn.entity.CommentMedia media = new vn.entity.CommentMedia();
                        media.setComment(c);
                        media.setMediaType("VIDEO");
                        media.setUrl(stored);
                        commentService.saveMedia(media);
                    }
                }
            }
        } catch (IOException ignored) {}
        redirectAttributes.addFlashAttribute("success", "Đã gửi đánh giá thành công");

        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        return "redirect:/productDetail?id=" + productId;
    }
}


