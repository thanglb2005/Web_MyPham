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
import vn.repository.UserRepository;
import vn.service.CommentService;
import vn.service.ImageStorageService;
import vn.service.OneXuService;

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
    private UserRepository userRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private OneXuService oneXuService;

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

        // Kiểm tra xem đây có phải là đánh giá đầu tiên của user cho sản phẩm này không
        // Phải kiểm tra TRƯỚC khi xóa đánh giá cũ
        boolean isFirstReview = !commentService.getLatestUserCommentForProduct(user.getUserId(), productId).isPresent();
        
        // Xóa đánh giá cũ nếu có trước khi tạo mới
        commentService.deleteOldCommentIfExists(user.getUserId(), productId);

        // Lưu comment chính
        Comment c = commentService.createComment(user, product, orderDetail, content, rating);

        // Chỉ tặng 300 xu cho đánh giá đầu tiên của người dùng cho sản phẩm này
        if (isFirstReview) {
            try {
                oneXuService.rewardFromReview(user.getUserId(), productId);
                
                // Đồng bộ hóa số dư và refresh session
                oneXuService.syncUserBalance(user.getUserId());
                User freshUser = userRepository.findById(user.getUserId()).orElse(user);
                session.setAttribute("user", freshUser);
            } catch (Exception e) {
                // Log lỗi nhưng không làm gián đoạn quá trình đánh giá
                System.err.println("Lỗi khi tặng xu cho đánh giá: " + e.getMessage());
            }
        }

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

        if (isFirstReview) {
            redirectAttributes.addFlashAttribute("success", "Đã gửi đánh giá thành công! Bạn đã nhận được 300 xu thưởng.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật đánh giá thành công!");
        }
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

        // Kiểm tra xem đây có phải là đánh giá đầu tiên của user cho sản phẩm này không
        // Phải kiểm tra TRƯỚC khi xóa đánh giá cũ
        boolean isFirstReview = !commentService.getLatestUserCommentForProduct(user.getUserId(), productId).isPresent();
        
        // Xóa đánh giá cũ nếu có trước khi tạo mới
        commentService.deleteOldCommentIfExists(user.getUserId(), productId);

        Comment c = commentService.createComment(user, product, null, content, rating);

        // Chỉ tặng 300 xu cho đánh giá đầu tiên của người dùng cho sản phẩm này
        if (isFirstReview) {
            try {
                oneXuService.rewardFromReview(user.getUserId(), productId);
                
                // Đồng bộ hóa số dư và refresh session
                oneXuService.syncUserBalance(user.getUserId());
                User freshUser = userRepository.findById(user.getUserId()).orElse(user);
                session.setAttribute("user", freshUser);
            } catch (Exception e) {
                // Log lỗi nhưng không làm gián đoạn quá trình đánh giá
                System.err.println("Lỗi khi tặng xu cho đánh giá: " + e.getMessage());
            }
        }

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
        
        if (isFirstReview) {
            redirectAttributes.addFlashAttribute("success", "Đã gửi đánh giá thành công! Bạn đã nhận được 300 xu thưởng.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật đánh giá thành công!");
        }

        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        return "redirect:/productDetail?id=" + productId;
    }

    /**
     * Xóa đánh giá của user
     */
    @PostMapping("/reviews/delete/{commentId}")
    public String deleteReview(@PathVariable Long commentId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // Xóa comment và lấy productId để trừ xu
            Long productId = commentService.deleteCommentAndReturnProductId(commentId, user.getUserId());
            
            if (productId != null) {
                // Kiểm tra xem đây có phải là đánh giá đầu tiên không (trước khi xóa)
                boolean wasFirstReview = !commentService.getLatestUserCommentForProduct(user.getUserId(), productId).isPresent();
                
                if (wasFirstReview) {
                    // Trừ 300 xu vì đã xóa đánh giá đầu tiên
                    try {
                        oneXuService.deductFromReviewDeletion(user.getUserId(), productId);
                        
                        // Đồng bộ hóa số dư và refresh session
                        oneXuService.syncUserBalance(user.getUserId());
                        User freshUser = userRepository.findById(user.getUserId()).orElse(user);
                        session.setAttribute("user", freshUser);
                        
                        redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá thành công. Đã trừ 300 xu.");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá thành công.");
                        System.err.println("Lỗi khi trừ xu do xóa đánh giá: " + e.getMessage());
                    }
                } else {
                    redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá thành công.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa đánh giá");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa đánh giá");
        }

        return "redirect:/user/my-orders";
    }

    /**
     * Xóa đánh giá từ trang chi tiết sản phẩm
     */
    @PostMapping("/reviews/delete/product/{commentId}")
    public String deleteProductReview(@PathVariable Long commentId,
                                     @RequestParam(value = "productId", required = false) Long productId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // Xóa comment và lấy productId để trừ xu
            Long actualProductId = commentService.deleteCommentAndReturnProductId(commentId, user.getUserId());
            
            if (actualProductId != null) {
                // Kiểm tra xem đây có phải là đánh giá đầu tiên không (trước khi xóa)
                boolean wasFirstReview = !commentService.getLatestUserCommentForProduct(user.getUserId(), actualProductId).isPresent();
                
                if (wasFirstReview) {
                    // Trừ 300 xu vì đã xóa đánh giá đầu tiên
                    try {
                        oneXuService.deductFromReviewDeletion(user.getUserId(), actualProductId);
                        
                        // Đồng bộ hóa số dư và refresh session
                        oneXuService.syncUserBalance(user.getUserId());
                        User freshUser = userRepository.findById(user.getUserId()).orElse(user);
                        session.setAttribute("user", freshUser);
                        
                        redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá thành công. Đã trừ 300 xu.");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá thành công.");
                        System.err.println("Lỗi khi trừ xu do xóa đánh giá: " + e.getMessage());
                    }
                } else {
                    redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá thành công.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa đánh giá");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa đánh giá");
        }

        if (productId != null) {
            return "redirect:/productDetail?id=" + productId;
        }
        return "redirect:/";
    }
}


