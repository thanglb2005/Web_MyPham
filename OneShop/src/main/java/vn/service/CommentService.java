package vn.service;

import vn.entity.Comment;
import vn.entity.OrderDetail;
import vn.entity.Product;
import vn.entity.User;
import vn.entity.CommentMedia;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    Comment createComment(User user, Product product, OrderDetail orderDetail, String content, Double rating);

    Optional<Comment> getUserCommentForOrderDetail(Long userId, Long orderDetailId);

    List<Comment> getCommentsByProduct(Long productId);

    // Fallback: lấy review gần nhất của user cho product
    java.util.Optional<Comment> getLatestUserCommentForProduct(Long userId, Long productId);

    // Media
    CommentMedia saveMedia(CommentMedia media);
    List<CommentMedia> listMediaByComment(Long commentId);
}


