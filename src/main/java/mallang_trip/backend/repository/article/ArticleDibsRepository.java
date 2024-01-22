package mallang_trip.backend.repository.article;

import mallang_trip.backend.domain.entity.article.Article;
import mallang_trip.backend.domain.entity.article.ArticleDibs;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleDibsRepository extends JpaRepository<ArticleDibs, Long> {

    Boolean existsByArticleAndUser(Article article, User user);

    void deleteByArticleAndUser(Article article, User user);
}
