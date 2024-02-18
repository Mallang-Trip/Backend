package mallang_trip.backend.domain.article.repository;

import java.util.Optional;
import mallang_trip.backend.domain.article.constant.ArticleType;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByDeletedOrderByUpdatedAtDesc(Boolean deleted, Pageable pageable);

    Page<Article> findByDeletedAndTypeOrderByUpdatedAtDesc(Boolean deleted,ArticleType type, Pageable pageable);

    Page<Article> findByDeletedAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByUpdatedAtDesc
        (Boolean deleted, String titleKeyword, String contentKeyword, Pageable pageable);

    Page<Article> findByDeletedAndUserOrderByUpdatedAtDesc(Boolean deleted, User user, Pageable pageable);

    Optional<Article> findByDeletedAndId(Boolean deleted, Long articleId);
}
