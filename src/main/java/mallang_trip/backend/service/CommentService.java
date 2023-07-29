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
import mallang_trip.backend.domain.entity.Article;
import mallang_trip.backend.domain.entity.Comment;
import mallang_trip.backend.domain.entity.Reply;
import mallang_trip.backend.repository.ArticleRepository;
import mallang_trip.backend.repository.CommentRepository;
import mallang_trip.backend.repository.ReplyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final UserService userService;

    // 댓글 작성
    public void createComment(Long articleId, CommentRequest request) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.Not_Found));
        Comment comment = Comment.builder()
            .user(userService.getCurrentUser())
            .article(article)
            .content(request.getContent())
            .build();
        commentRepository.save(comment);
    }

    // 대댓글 작성
    public void createReply(Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.Not_Found));
        Reply reply = Reply.builder()
            .user(userService.getCurrentUser())
            .comment(comment)
            .content(request.getContent())
            .build();
        replyRepository.save(reply);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    // 대댓글 삭제
    public void deleteReply(Long replyId) {
        replyRepository.deleteById(replyId);
    }

    // 특정 글에 대한 댓글 & 대댓글 조회
    public List<CommentResponse> getCommentsAndReplies(Article article) {
        List<Comment> comments = commentRepository.findByArticle(article);
        List<CommentResponse> responseList = comments.stream()
            .map(comment -> {
                CommentResponse response = CommentResponse.of(comment);
                response.setReplies(getReplies(comment));
                return response;
            })
            .collect(Collectors.toList());
        return responseList;
    }

    private List<ReplyResponse> getReplies(Comment comment) {
        List<Reply> replies = replyRepository.findByComment(comment);
        List<ReplyResponse> responseList = replies
            .stream()
            .map(ReplyResponse::of)
            .collect(Collectors.toList());
        return responseList;
    }

    // 글 댓글 수 조회
    public int getCommentsCount(Article article) {
        int count = 0;
        List<Comment> comments = commentRepository.findByArticle(article);
        for (Comment comment : comments) {
            count += 1 + replyRepository.countByComment(comment);
        }
        return count;
    }

    // 내 댓글 & 대댓글 조회
    public Page<MyCommentResponse> getMyCommentsAndReplies(Pageable pageable) {
        return commentRepository.getMyCommentsAndReplies(userService.getCurrentUser(), pageable);
    }

}
