package mallang_trip.backend.repository;

import mallang_trip.backend.domain.dto.comment.MyCommentResponse;
import mallang_trip.backend.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

    Page<MyCommentResponse> getMyCommentsAndReplies(User user, Pageable pageable);
}
