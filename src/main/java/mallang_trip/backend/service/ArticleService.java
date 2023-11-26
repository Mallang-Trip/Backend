package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.ArticleType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;
import mallang_trip.backend.domain.dto.comment.CommentResponse;
import mallang_trip.backend.domain.dto.comment.ReplyResponse;
import mallang_trip.backend.domain.entity.community.Article;
import mallang_trip.backend.domain.dto.article.ArticleBriefResponse;
import mallang_trip.backend.domain.dto.article.ArticleDetailsResponse;
import mallang_trip.backend.domain.dto.article.ArticleIdResponse;
import mallang_trip.backend.domain.dto.article.ArticleRequest;
import mallang_trip.backend.domain.entity.community.ArticleDibs;
import mallang_trip.backend.domain.entity.community.Comment;
import mallang_trip.backend.domain.entity.community.Reply;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.community.ArticleDibsRepository;
import mallang_trip.backend.repository.community.ArticleRepository;
import mallang_trip.backend.repository.community.CommentRepository;
import mallang_trip.backend.repository.community.ReplyRepository;
import mallang_trip.backend.repository.party.PartyRepository;
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
    private final CommentService commentService;
    private final UserService userService;
    private final PartyRepository partyRepository;
    private final ArticleDibsRepository articleDibsRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    // 작성
    public ArticleIdResponse createArticle(ArticleRequest request) {
        Party party = request.getPartyId() == null ? null
            : partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new BaseException(Not_Found));
        Article article = Article.builder()
            .user(userService.getCurrentUser())
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

    // 수정
    public void changeArticle(Long ArticleId, ArticleRequest request) {
        Article article = articleRepository.findById(ArticleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 CHECK
        if (userService.getCurrentUser().equals(article.getUser())) {
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

    // 삭제
    public void deleteArticle(Long ArticleId) {
        Article article = articleRepository.findById(ArticleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 CHECK
        if (userService.getCurrentUser().equals(article.getUser())) {
            throw new BaseException(BaseResponseStatus.Forbidden);
        }
        articleRepository.delete(article);
    }

    // 상세보기
    public ArticleDetailsResponse getArticleDetails(Long ArticleId) {
        Article article = articleRepository.findById(ArticleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        return ArticleDetailsResponse.builder()
            .articleId(article.getId())
            .partyId(article.getParty() == null ? null : article.getParty().getId())
            .partyName(article.getParty() == null ? null : article.getParty().getCourse().getName())
            .userId(article.getUser().getId())
            .userNickname(article.getUser().getNickname())
            .userProfileImage(article.getUser().getProfileImage())
            .type(article.getType())
            .title(article.getTitle())
            .content(article.getContent())
            .comments(getComments(article))
            .commentsCount(getCommentsCount(article))
            .dibs(checkArticleDibs(article))
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .build();
    }

    // 키워드 검색
    public Page<ArticleBriefResponse> getArticlesByKeyword(String keyword, Pageable pageable) {
        Page<Article> articles = articleRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(
            keyword, keyword, pageable);
        List<ArticleBriefResponse> responses = articles.stream()
            .map(article -> ArticleBriefResponse.of(article, getCommentsCount(article)))
            .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, articles.getTotalElements());
    }

    // 카테고리 별 조회
    public Page<ArticleBriefResponse> getArticlesByType(String type, Pageable pageable) {
        Page<Article> articles =
            type.equals("all") ? articleRepository.findAllByOrderByCreatedAtDesc(pageable)
                : articleRepository.findByTypeOrderByCreatedAtDesc(ArticleType.from(type),
                    pageable);
        List<ArticleBriefResponse> responses = articles.stream()
            .map(article -> ArticleBriefResponse.of(article, getCommentsCount(article)))
            .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, articles.getTotalElements());
    }

    // 내가 작성한 글 조회
    public Page<ArticleBriefResponse> getMyArticles(Pageable pageable) {
        User user = userService.getCurrentUser();
        Page<Article> articles = articleRepository.findByUserOrderByCreatedAtDesc(
            user, pageable);
        List<ArticleBriefResponse> responses = articles.stream()
            .map(article -> ArticleBriefResponse.of(article, getCommentsCount(article)))
            .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, articles.getTotalElements());
    }

    // 댓글 단 게시글 조회
/*    public Page<ArticleBriefResponse> getMyComments(Pageable pageable){

    }*/

    // 찜하기
    public void createArticleDibs(Long articleId) {
        Article article = articleRepository.findById(articleId)
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

    // 찜 취소
    public void deleteArticleDibs(Long articleId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 찜한 여행지 아닐 때
        if (!checkArticleDibs(article)) {
            return;
        }
        articleDibsRepository.deleteByArticleAndUser(article, userService.getCurrentUser());
    }

    // 찜 여부 확인
    private boolean checkArticleDibs(Article article) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return false;
        }
        return articleDibsRepository.existsByArticleAndUser(article, user);
    }

    // 댓글 작성
    public void createComment(Long articleId, String content) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new BaseException(Not_Found));
        Comment comment = Comment.builder()
            .article(article)
            .user(userService.getCurrentUser())
            .content(content)
            .build();
        commentRepository.save(comment);
    }

    // 대댓글 작성
    public void createReply(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new BaseException(Not_Found));
        Reply reply = Reply.builder()
            .comment(comment)
            .user(userService.getCurrentUser())
            .content(content)
            .build();
        replyRepository.save(reply);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId){
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 CHECK
        if(!userService.getCurrentUser().equals(comment.getUser())){
            throw new BaseException(Unauthorized);
        }
        commentRepository.delete(comment);
    }

    // 대댓글 삭제
    public void deleteReply(Long replyId){
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 CHECK
        if(!userService.getCurrentUser().equals(reply.getUser())){
            throw new BaseException(Unauthorized);
        }
        replyRepository.delete(reply);
    }

    // 댓글 + 대댓글 조회
    private List<CommentResponse> getComments(Article article){
        return commentRepository.findByArticle(article).stream()
            .map(comment -> {
                CommentResponse response = CommentResponse.of(comment);
                response.setReplies(getReplies(comment));
                return response;
            })
            .collect(Collectors.toList());
    }

    //대댓글 조회
    private List<ReplyResponse> getReplies(Comment comment){
        return replyRepository.findByComment(comment).stream()
            .map(ReplyResponse::of)
            .collect(Collectors.toList());
    }

    // 댓글 수 조회
    private Integer getCommentsCount(Article article){
        int count = 0;
        List<Comment> comments = commentRepository.findByArticle(article);
        for (Comment comment : comments) {
            count += 1 + replyRepository.countByComment(comment);
        }
        return count;
    }

}
