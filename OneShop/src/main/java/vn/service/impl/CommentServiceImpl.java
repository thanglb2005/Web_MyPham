package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.Comment;
import vn.entity.OrderDetail;
import vn.entity.Product;
import vn.entity.User;
import vn.entity.CommentMedia;
import vn.repository.CommentRepository;
import vn.repository.CommentMediaRepository;
import vn.service.CommentService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentMediaRepository commentMediaRepository;

    @Override
    @Transactional
    public Comment createComment(User user, Product product, OrderDetail orderDetail, String content, Double rating) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setProduct(product);
        comment.setOrderDetail(orderDetail);
        comment.setContent(content);
        comment.setRating(rating);
        comment.setRateDate(new Date());
        return commentRepository.save(comment);
    }

    @Override
    public Optional<Comment> getUserCommentForOrderDetail(Long userId, Long orderDetailId) {
        return commentRepository.findByOrderDetailAndUser(orderDetailId, userId);
    }

    @Override
    public List<Comment> getCommentsByProduct(Long productId) {
        return commentRepository.findByProduct(productId);
    }

    @Override
    public Optional<Comment> getLatestUserCommentForProduct(Long userId, Long productId) {
        return commentRepository.findTopByProduct_ProductIdAndUser_UserIdOrderByRateDateDesc(productId, userId);
    }

    @Override
    public CommentMedia saveMedia(CommentMedia media) {
        return commentMediaRepository.save(media);
    }

    @Override
    public List<CommentMedia> listMediaByComment(Long commentId) {
        return commentMediaRepository.findByComment_Id(commentId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            // Chỉ cho phép xóa đánh giá của chính user đó
            if (comment.getUser().getUserId().equals(userId)) {
                commentRepository.delete(comment);
            }
        }
    }

    @Override
    @Transactional
    public void deleteOldCommentIfExists(Long userId, Long productId) {
        // Tìm đánh giá cũ của user cho sản phẩm này
        Optional<Comment> oldComment = commentRepository.findTopByProduct_ProductIdAndUser_UserIdOrderByRateDateDesc(productId, userId);
        if (oldComment.isPresent()) {
            commentRepository.delete(oldComment.get());
        }
    }
}


