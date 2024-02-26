package mallang_trip.backend.domain.article.service;

import static mallang_trip.backend.domain.admin.exception.AdminExceptionStatus.SUSPENDING;
import static mallang_trip.backend.domain.article.exception.ArticleExceptionStatus.CANNOT_FOUND_ARTICLE;
import static mallang_trip.backend.domain.article.exception.ArticleExceptionStatus.CANNOT_FOUND_COMMENT;
import static mallang_trip.backend.domain.article.exception.ArticleExceptionStatus.CANNOT_FOUND_REPLY;
import static mallang_trip.backend.domain.article.exception.ArticleExceptionStatus.DELETION_FORBIDDEN;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_ADMIN;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.domain.article.dto.CommentResponse;
import mallang_trip.backend.domain.article.dto.MyCommentResponse;
import mallang_trip.backend.domain.article.dto.ReplyResponse;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.article.entity.Comment;
import mallang_trip.backend.domain.article.entity.Reply;
import mallang_trip.backend.domain.article.repository.ArticleRepository;
import mallang_trip.backend.domain.article.repository.CommentRepository;
import mallang_trip.backend.domain.article.repository.ReplyRepository;
import mallang_trip.backend.domain.global.io.BaseException;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleCommentService {

    private final UserService userService;
    private final SuspensionService suspensionService;
    private final ArticleNotificationService articleNotificationService;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    /**
     * 댓글(Comment) 등록
     */
    public void createComment(Long articleId, String content) {
        Article article = articleRepository.findByDeletedAndId(false, articleId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_ARTICLE));
        User user = userService.getCurrentUser();
        // 정지 CHECK
        if (suspensionService.isSuspending(user)) {
            throw new BaseException(SUSPENDING);
        }
        // 저장
        commentRepository.save(Comment.builder()
            .article(article)
            .user(user)
            .content(content)
            .build());
        // 새 댓글 알림
        articleNotificationService.newComment(article);
    }

    /**
     * 대댓글(Reply) 작성
     */
    public void createReply(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_COMMENT));
        User user = userService.getCurrentUser();
        // 정지 CHECK
        if (suspensionService.isSuspending(user)) {
            throw new BaseException(SUSPENDING);
        }
        // 저장
        replyRepository.save(Reply.builder()
            .comment(comment)
            .user(user)
            .content(content)
            .build());
        // 새 대댓글 알림
        articleNotificationService.newReply(comment);
    }

    /**
     * 댓글(Comment) 삭제
     */
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_COMMENT));
        // 작성자 또는 관리자인지 CHECK
        User user = userService.getCurrentUser();
        if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(comment.getUser())) {
            throw new BaseException(DELETION_FORBIDDEN);
        }
        // 삭제
        commentRepository.delete(comment);
    }

    /**
     * 대댓글(Reply) 삭제
     */
    public void deleteReply(Long replyId) {
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_REPLY));
        // 작성자 또는 관리자인지 CHECK
        User user = userService.getCurrentUser();
        if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(reply.getUser())) {
            throw new BaseException(DELETION_FORBIDDEN);
        }
        replyRepository.delete(reply);
    }

    /**
     * 댓글 + 대댓글 조회
     */
    public List<CommentResponse> getCommentsAndReplies(Article article) {
        return commentRepository.findByArticle(article).stream()
            .map(comment -> CommentResponse.of(comment, getRepliesByComment(comment)))
            .collect(Collectors.toList());
    }

    /**
     * 댓글에 대한 대댓글 조회
     */
    private List<ReplyResponse> getRepliesByComment(Comment comment) {
        return replyRepository.findByComment(comment).stream()
            .map(ReplyResponse::of)
            .collect(Collectors.toList());
    }

    /**
     * 댓글 + 대댓글 수 조회
     */
    public Integer countCommentsAndReplies(Article article) {
        int count = 0;
        List<Comment> comments = commentRepository.findByArticle(article);
        for (Comment comment : comments) {
            count += 1 + replyRepository.countByComment(comment);
        }
        return count;
    }

    /**
     * 내가 댓글 or 대댓글 단 게시글 조회
     */
    public Page<MyCommentResponse> getMyComments(Pageable pageable) {
        User user = userService.getCurrentUser();
        // 내 댓글 + 대댓글
        List<MyCommentResponse> responses = new ArrayList<>();
        responses.addAll(getMyComments(user));
        responses.addAll(getMyReplies(user));
        // sorting
        responses.sort(Comparator.comparing(MyCommentResponse::getCreatedAt).reversed());
        // List -> PageImpl
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > responses.size() ?
            responses.size() : (start + pageable.getPageSize());

        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    /**
     * 내 댓글 조회
     */
    private List<MyCommentResponse> getMyComments(User user) {
        return commentRepository.findByDeletedAndUser(false, user).stream()
            .map(comment -> MyCommentResponse.of(comment.getArticle(), comment.getContent(),
                comment.getCreatedAt(), countCommentsAndReplies(comment.getArticle())))
            .collect(Collectors.toList());
    }

    /**
     * 내 대댓글 조회
     */
    private List<MyCommentResponse> getMyReplies(User user) {
        return replyRepository.findByDeletedAndUser(false, user).stream()
            .map(reply -> MyCommentResponse.of(reply.getComment().getArticle(), reply.getContent(),
                reply.getCreatedAt(), countCommentsAndReplies(reply.getComment().getArticle())))
            .collect(Collectors.toList());
    }
}
