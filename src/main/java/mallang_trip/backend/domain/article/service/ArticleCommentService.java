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
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleCommentService {

    private final CurrentUserService currentUserService;
    private final SuspensionService suspensionService;
    private final ArticleNotificationService articleNotificationService;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    /**
     * 댓글(Comment)을 등록하는 메소드입니다.
     *
     * @param articleId 댓글을 등록할 게시글의 ID 값
     * @param content 댓글 내용 값
     * @throws BaseException 정지된 사용자이거나 articleId에 해당하는 게시글을 찾지 못할 경우 발생하는 예외
     */
    public void createComment(Long articleId, String content) {
        Article article = articleRepository.findByDeletedAndId(false, articleId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_ARTICLE));
        User user = currentUserService.getCurrentUser();
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
     * 답글(Reply)을 등록하는 메소드입니다.
     *
     * @param commentId 답글을 등록할 댓글의 ID 값
     * @param content 답글 내용 값
     * @throws BaseException 정지된 사용자이거나 commentId에 해당하는 댓글을 찾지 못할 경우 발생하는 예외
     */
    public void createReply(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_COMMENT));
        User user = currentUserService.getCurrentUser();
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
        // 새 답글 알림
        articleNotificationService.newReply(comment);
    }

    /**
     * 댓글(Comment)을 삭제하는 메소드입니다.
     * <p>
     * 해당하는 댓글을 soft delete(deleted = ture) 처리합니다.
     *
     * @param commentId 삭제할 댓글의 ID 값
     * @throws BaseException 작성자또는 관리자가 아닐 경우나 commentId에 해당하는 댓글을 찾지 못할 경우 발생하는 예외
     */
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_COMMENT));
        // 작성자 또는 관리자인지 CHECK
        User user = currentUserService.getCurrentUser();
        if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(comment.getUser())) {
            throw new BaseException(DELETION_FORBIDDEN);
        }
        // 삭제
        commentRepository.delete(comment);
    }

    /**
     * 답글(Reply)을 삭제하는 메소드입니다.
     * <p>
     * 해당하는 답글을 soft delete(deleted = ture) 처리합니다.
     *
     * @param replyId 삭제할 답글의 ID 값
     * @throws BaseException 작성자또는 관리자가 아닐 경우나 replyId에 해당하는 답글을 찾지 못할 경우 발생하는 예외
     */
    public void deleteReply(Long replyId) {
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_REPLY));
        // 작성자 또는 관리자인지 CHECK
        User user = currentUserService.getCurrentUser();
        if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(reply.getUser())) {
            throw new BaseException(DELETION_FORBIDDEN);
        }
        replyRepository.delete(reply);
    }

    /**
     * 게시글의 모든 댓글과 답글을 조회하는 메소드입니다.
     * <p>
     * soft delete 처리된 댓글인 경우, content = "[삭제된 댓글]" 인 상태로 조회됩니다.
     *
     * @param article 조회할 게시글 객체
     * @return 댓글과 답글 정보를 담은 List<CommentResponse> 객체
     */
    public List<CommentResponse> getCommentsAndReplies(Article article) {
        return commentRepository.findByArticle(article).stream()
            .map(comment -> CommentResponse.of(comment, getRepliesByComment(comment)))
            .collect(Collectors.toList());
    }

    /**
     * 댓글에 달린 모든 답글들을 조회하는 메소드입니다.
     * <p>
     * soft delete 처리된 답글인 경우, content = "[삭제된 댓글]" 인 상태로 조회됩니다.
     *
     * @param comment 조회할 댓글 객체
     * @return 답글 정보를 담은 List<ReplyResponse> 객체
     */
    private List<ReplyResponse> getRepliesByComment(Comment comment) {
        return replyRepository.findByComment(comment).stream()
            .map(ReplyResponse::of)
            .collect(Collectors.toList());
    }

    /**
     * 게시글에 달린 댓글과 답글의 총 개수을 계산합니다.
     * <p>
     * soft delete 처리된 댓글과 답글도 계산에 포함됩니다.
     *
     * @param article 조회할 게시글 객체
     * @return 댓글과 답글의 총 개수 값
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
     * 내가 작성한 댓글과 답글을 조회합니다.
     * <p>
     * 작성 시간이 최신인 순서로 조회합니다. soft delete 처리된 댓글과 답글은 조회되지 않습니다.
     *
     * @param pageable 페이징 정보를 담은 Pageable 객체
     * @return 내 댓글과 답글 정보와 페이징 정보를 담은 Page<MyCommentResponse> 객체
     */
    public Page<MyCommentResponse> getMyComments(Pageable pageable) {
        User user = currentUserService.getCurrentUser();
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
     * 내가 작성한 댓글들을 조회합니다.
     * <p>
     * soft delete 처리된 댓글은 조회되지 않습니다.
     *
     * @param user 조회할 User 객체
     * @return 내 댓글 정보를 담은 List<MyCommentResponse> 객체
     */
    private List<MyCommentResponse> getMyComments(User user) {
        return commentRepository.findByDeletedAndUser(false, user).stream()
            .map(comment -> MyCommentResponse.of(comment.getArticle(), comment.getContent(),
                comment.getCreatedAt(), countCommentsAndReplies(comment.getArticle())))
            .collect(Collectors.toList());
    }

    /**
     * 내가 작성한 답글들을 조회합니다.
     * <p>
     * soft delete 처리된 답글은 조회되지 않습니다.
     *
     * @param user 조회할 User 객체
     * @return 내 답글 정보를 담은 List<MyCommentResponse> 객체
     */
    private List<MyCommentResponse> getMyReplies(User user) {
        return replyRepository.findByDeletedAndUser(false, user).stream()
            .map(reply -> MyCommentResponse.of(reply.getComment().getArticle(), reply.getContent(),
                reply.getCreatedAt(), countCommentsAndReplies(reply.getComment().getArticle())))
            .collect(Collectors.toList());
    }
}
