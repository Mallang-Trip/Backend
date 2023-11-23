package mallang_trip.backend.repository.community;

import java.util.List;
import mallang_trip.backend.domain.entity.community.Article;
import mallang_trip.backend.domain.entity.community.ArticleDibs;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleDibsRepository extends JpaRepository<ArticleDibs, Long> {

    Boolean existsByArticleAndUser(Article article, User user);

    List<ArticleDibs> findAllByUserOrderByUpdatedAtDesc(User user);

    void deleteByArticleAndUser(Article article, User user);
}
