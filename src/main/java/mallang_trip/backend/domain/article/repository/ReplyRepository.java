package mallang_trip.backend.domain.article.repository;

import java.util.List;
import mallang_trip.backend.domain.article.entity.Comment;
import mallang_trip.backend.domain.article.entity.Reply;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByComment(Comment comment);

    List<Reply> findByCommentAndDeleted(Comment comment, Boolean deleted);

    Integer countByComment(Comment comment);

    List<Reply> findByDeletedAndUser(Boolean deleted, User user);
}
