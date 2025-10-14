package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.entity.CommentMedia;

import java.util.List;

@Repository
public interface CommentMediaRepository extends JpaRepository<CommentMedia, Long> {
    List<CommentMedia> findByComment_Id(Long commentId);
}


