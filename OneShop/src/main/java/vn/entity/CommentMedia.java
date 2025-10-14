package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_media")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "media_type", length = 20, nullable = false)
    private String mediaType; // IMAGE or VIDEO

    @Column(name = "url", length = 500, nullable = false)
    private String url;
}


