package mallang_trip.backend.domains.article.repository;

import mallang_trip.backend.domains.article.entity.Article;
import mallang_trip.backend.domains.article.entity.ArticleDibs;
import mallang_trip.backend.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleDibsRepository extends JpaRepository<ArticleDibs, Long> {

    Boolean existsByArticleAndUser(Article article, User user);

    void deleteByArticleAndUser(Article article, User user);
}
