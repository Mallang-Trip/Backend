package mallang_trip.backend.repository.community;

import java.util.List;
import mallang_trip.backend.domain.entity.community.Article;
import mallang_trip.backend.domain.entity.community.Comment;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArticle(Article article);

    List<Comment> findByUser(User user);
}
