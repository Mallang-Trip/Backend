package mallang_trip.backend.repository;

import mallang_trip.backend.constant.ArticleType;
import mallang_trip.backend.domain.Article;
import mallang_trip.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndType
        (String titleKeyword, String contentKeyword, ArticleType type, Pageable pageable);

    Page<Article> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase
        (String titleKeyword, String contentKeyword, Pageable pageable);

    Page<Article> findByUser(User user, Pageable pageable);
}
