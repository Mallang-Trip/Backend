package mallang_trip.backend.repository;

import java.util.List;
import mallang_trip.backend.domain.entity.Article;
import mallang_trip.backend.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    List<Comment> findByArticle(Article article);

}
