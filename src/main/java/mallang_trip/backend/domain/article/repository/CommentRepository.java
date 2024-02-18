package mallang_trip.backend.domain.article.repository;

import java.util.List;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.article.entity.Comment;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArticle(Article article);

    List<Comment> findByDeletedAndUser(Boolean deleted, User user);
}
