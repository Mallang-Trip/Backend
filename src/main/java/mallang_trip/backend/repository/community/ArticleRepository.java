package mallang_trip.backend.repository.community;

import mallang_trip.backend.constant.ArticleType;
import mallang_trip.backend.domain.entity.community.Article;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    Page<Article> findByTypeOrderByUpdatedAtDesc(ArticleType type, Pageable pageable);

    Page<Article> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByUpdatedAtDesc
        (String titleKeyword, String contentKeyword, Pageable pageable);

    Page<Article> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);
}
