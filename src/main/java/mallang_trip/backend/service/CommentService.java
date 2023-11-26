package mallang_trip.backend.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;
import mallang_trip.backend.domain.dto.comment.CommentRequest;
import mallang_trip.backend.domain.dto.comment.CommentResponse;
import mallang_trip.backend.domain.dto.comment.MyCommentResponse;
import mallang_trip.backend.domain.dto.comment.ReplyResponse;
import mallang_trip.backend.domain.entity.community.Article;
import mallang_trip.backend.domain.entity.community.Comment;
import mallang_trip.backend.domain.entity.community.Reply;
import mallang_trip.backend.repository.community.ArticleRepository;
import mallang_trip.backend.repository.community.CommentRepository;
import mallang_trip.backend.repository.community.ReplyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;

    // 내 댓글 & 대댓글 조회
    public Page<MyCommentResponse> getMyCommentsAndReplies(Pageable pageable) {
        return commentRepository.getMyCommentsAndReplies(userService.getCurrentUser(), pageable);
    }

}
