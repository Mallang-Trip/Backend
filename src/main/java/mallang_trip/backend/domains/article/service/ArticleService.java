package mallang_trip.backend.domains.article.service;

import static mallang_trip.backend.domains.user.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.domains.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.domains.global.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.domains.global.io.BaseResponseStatus.SUSPENDING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.article.constant.ArticleType;
import mallang_trip.backend.domains.article.repository.CommentRepository;
import mallang_trip.backend.domains.article.repository.ReplyRepository;
import mallang_trip.backend.domains.article.repository.ArticleDibsRepository;
import mallang_trip.backend.domains.article.repository.ArticleRepository;
import mallang_trip.backend.domains.global.io.BaseException;
import mallang_trip.backend.domains.global.io.BaseResponseStatus;
import mallang_trip.backend.domains.article.dto.CommentResponse;
import mallang_trip.backend.domains.article.dto.MyCommentResponse;
import mallang_trip.backend.domains.article.dto.ReplyResponse;
import mallang_trip.backend.domains.article.entity.Article;
import mallang_trip.backend.domains.article.dto.ArticleBriefResponse;
import mallang_trip.backend.domains.article.dto.ArticleDetailsResponse;
import mallang_trip.backend.domains.article.dto.ArticleIdResponse;
import mallang_trip.backend.domains.article.dto.ArticleRequest;
import mallang_trip.backend.domains.article.entity.ArticleDibs;
import mallang_trip.backend.domains.article.entity.Comment;
import mallang_trip.backend.domains.article.entity.Reply;
import mallang_trip.backend.domains.party.entity.Party;
import mallang_trip.backend.domains.user.entity.User;
import mallang_trip.backend.domains.party.repository.PartyRepository;
import mallang_trip.backend.domains.admin.service.SuspensionService;
import mallang_trip.backend.domains.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserService userService;
    private final PartyRepository partyRepository;
    private final ArticleDibsRepository articleDibsRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final ArticleNotificationService articleNotificationService;
    private final SuspensionService suspensionService;

    /** 게시글 작성 */
    public ArticleIdResponse createArticle(ArticleRequest request) {
        User user = userService.getCurrentUser();
        if(suspensionService.isSuspending(user)){
            throw new BaseException(SUSPENDING);
        }
        Party party = request.getPartyId() == null ? null
            : partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new BaseException(Not_Found));
        Article article = Article.builder()
            .user(user)
            .type(ArticleType.from(request.getType()))
            .title(request.getTitle())
            .content(request.getContent())
            .party(party)
            .images(request.getImages())
            .build();
        return ArticleIdResponse.builder()
            .articleId(articleRepository.save(article).getId())
            .build();
    }

    /** 게시글 수정 */
    public void changeArticle(Long articleId, ArticleRequest request) {
        Article article = articleRepository.findByDeletedAndId(false, articleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        User user = userService.getCurrentUser();
        // 권한 CHECK
        if(suspensionService.isSuspending(user)){
            throw new BaseException(SUSPENDING);
        }
        if (!user.equals(article.getUser())) {
            throw new BaseException(BaseResponseStatus.Forbidden);
        }
        // 수정
        Party party = request.getPartyId() == null ? null
            : partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new BaseException(Not_Found));
        article.setParty(party);
        article.setType(ArticleType.from(request.getType()));
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setImages(request.getImages());
    }

    /** 게시글 삭제 */
    public void deleteArticle(Long articleId) {
        Article article = articleRepository.findByDeletedAndId(false, articleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 CHECK
        User user = userService.getCurrentUser();
        if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(article.getUser())) {
            throw new BaseException(BaseResponseStatus.Forbidden);
        }
        article.setDeleted(true);
    }

    /** 게시글 상세보기 */
    public ArticleDetailsResponse getArticleDetails(Long articleId) {
        Article article = articleRepository.findByDeletedAndId(false, articleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        return ArticleDetailsResponse.builder()
            .articleId(article.getId())
            .partyId(article.getParty() == null ? null : article.getParty().getId())
            .partyName(article.getParty() == null ? null : article.getParty().getCourse().getName())
            .userId(article.getUser().getId())
            .nickname(article.getUser().getNickname())
            .profileImg(article.getUser().getProfileImage())
            .type(article.getType())
            .title(article.getTitle())
            .content(article.getContent())
            .images(article.getImages())
            .comments(getComments(article))
            .commentsCount(getCommentsCount(article))
            .dibs(checkArticleDibs(article))
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .build();
    }

    /** 키워드 검색 */
    public Page<ArticleBriefResponse> getArticlesByKeyword(String keyword, Pageable pageable) {
        Page<Article> articles = articleRepository.findByDeletedAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByUpdatedAtDesc(
            false, keyword, keyword, pageable);
        List<ArticleBriefResponse> responses = articles.stream()
            .map(article -> ArticleBriefResponse.of(article, getCommentsCount(article)))
            .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, articles.getTotalElements());
    }

    /** 카테고리 별 조회 */
    public Page<ArticleBriefResponse> getArticlesByType(String type, Pageable pageable) {
        Page<Article> articles =
            type.equals("all") ? articleRepository.findByDeletedOrderByUpdatedAtDesc(false, pageable)
                : articleRepository.findByDeletedAndTypeOrderByUpdatedAtDesc(false, ArticleType.from(type),
                    pageable);
        List<ArticleBriefResponse> responses = articles.stream()
            .map(article -> ArticleBriefResponse.of(article, getCommentsCount(article)))
            .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, articles.getTotalElements());
    }

    /** 내가 작성한 글 조회 */
    public Page<ArticleBriefResponse> getMyArticles(Pageable pageable) {
        User user = userService.getCurrentUser();
        Page<Article> articles = articleRepository.findByDeletedAndUserOrderByUpdatedAtDesc(
            false, user, pageable);
        List<ArticleBriefResponse> responses = articles.stream()
            .map(article -> ArticleBriefResponse.of(article, getCommentsCount(article)))
            .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, articles.getTotalElements());
    }

    /** 댓글 단 게시글 조회 */
    public Page<MyCommentResponse> getMyComments(Pageable pageable) {
        User user = userService.getCurrentUser();
        // 댓글 조회
        List<MyCommentResponse> comments = commentRepository.findByDeletedAndUser(false, user).stream()
            .map(comment -> MyCommentResponse.of(comment.getArticle(), comment.getContent(),
                comment.getCreatedAt(), getCommentsCount(comment.getArticle())))
            .collect(Collectors.toList());
        // 대댓글 조회
        List<MyCommentResponse> replies = replyRepository.findByDeletedAndUser(false, user).stream()
            .map(reply -> {
                Article article = reply.getComment().getArticle();
                return MyCommentResponse.of(article, reply.getContent(), reply.getCreatedAt(), getCommentsCount(article));
            })
            .collect(Collectors.toList());
        // 댓글 + 대댓글
        List<MyCommentResponse> responses = new ArrayList<>();
        responses.addAll(comments);
        responses.addAll(replies);
        Collections.sort(responses, Comparator.comparing(MyCommentResponse::getCreatedAt).reversed());
        // List -> PageImpl
        int start = (int)pageable.getOffset();
        int end = (start + pageable.getPageSize()) > responses.size() ? responses.size() : (start + pageable.getPageSize());
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    /** 게시글 찜하기 */
    public void createArticleDibs(Long articleId) {
        Article article = articleRepository.findByDeletedAndId(false, articleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 이미 찜한 경우
        if (checkArticleDibs(article)) {
            return;
        }
        articleDibsRepository.save(ArticleDibs.builder()
            .article(article)
            .user(userService.getCurrentUser())
            .build());
    }

    /** 게시글 찜 취소 */
    public void deleteArticleDibs(Long articleId) {
        Article article = articleRepository.findByDeletedAndId(false, articleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 찜한 여행지 아닐 때
        if (!checkArticleDibs(article)) {
            return;
        }
        articleDibsRepository.deleteByArticleAndUser(article, userService.getCurrentUser());
    }

    /** 찜 여부 확인 */
    private boolean checkArticleDibs(Article article) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return false;
        }
        return articleDibsRepository.existsByArticleAndUser(article, user);
    }

    /** 댓글 작성 */
    public void createComment(Long articleId, String content) {
        Article article = articleRepository.findByDeletedAndId(false, articleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        User user = userService.getCurrentUser();
        if(suspensionService.isSuspending(user)){
            throw new BaseException(SUSPENDING);
        }
        Comment comment = Comment.builder()
            .article(article)
            .user(user)
            .content(content)
            .build();
        commentRepository.save(comment);
        articleNotificationService.newComment(article);
    }

    /** 대댓글 작성 */
    public void createReply(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new BaseException(Not_Found));
        User user = userService.getCurrentUser();
        if(suspensionService.isSuspending(user)){
            throw new BaseException(SUSPENDING);
        }
        Reply reply = Reply.builder()
            .comment(comment)
            .user(user)
            .content(content)
            .build();
        replyRepository.save(reply);
        articleNotificationService.newReply(comment);
    }

    /** 댓글 삭제 */
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 CHECK
        User user = userService.getCurrentUser();
        if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(comment.getUser())) {
            throw new BaseException(Forbidden);
        }
        commentRepository.delete(comment);
    }

    /** 대댓글 삭제 */
    public void deleteReply(Long replyId) {
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 CHECK
        User user = userService.getCurrentUser();
        if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(reply.getUser())) {
            throw new BaseException(Forbidden);
        }
        replyRepository.delete(reply);
    }

    /** 댓글 + 대댓글 조회 */
    private List<CommentResponse> getComments(Article article) {
        return commentRepository.findByArticle(article).stream()
            .map(comment -> {
                CommentResponse response = CommentResponse.of(comment);
                response.setReplies(getReplies(comment));
                return response;
            })
            .collect(Collectors.toList());
    }

    /** 대댓글 조회 */
    private List<ReplyResponse> getReplies(Comment comment) {
        return replyRepository.findByComment(comment).stream()
            .map(ReplyResponse::of)
            .collect(Collectors.toList());
    }

    /** 댓글 수 조회 */
    private Integer getCommentsCount(Article article) {
        int count = 0;
        List<Comment> comments = commentRepository.findByArticle(article);
        for (Comment comment : comments) {
            count += 1 + replyRepository.countByComment(comment);
        }
        return count;
    }

}
