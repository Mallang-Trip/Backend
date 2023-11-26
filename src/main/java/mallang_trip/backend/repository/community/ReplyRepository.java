package mallang_trip.backend.repository.community;

import java.util.List;
import mallang_trip.backend.domain.entity.community.Comment;
import mallang_trip.backend.domain.entity.community.Reply;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByComment(Comment comment);

    Integer countByComment(Comment comment);

    List<Reply> findByUser(User user);
}
