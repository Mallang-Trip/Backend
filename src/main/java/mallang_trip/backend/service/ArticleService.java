package mallang_trip.backend.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.ArticleType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;
import mallang_trip.backend.domain.entity.community.Article;
import mallang_trip.backend.domain.dto.article.ArticleBriefResponse;
import mallang_trip.backend.domain.dto.article.ArticleDetailsResponse;
import mallang_trip.backend.domain.dto.article.ArticleIdResponse;
import mallang_trip.backend.domain.dto.article.ArticleRequest;
import mallang_trip.backend.repository.ArticleRepository;
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

    // 작성
    public ArticleIdResponse createArticle(ArticleRequest request) {
        Article article = Article.builder()
            .user(userService.getCurrentUser())
            .type(ArticleType.from(request.getType()))
            .title(request.getTitle())
            .content(request.getContent())
            .build();
        return ArticleIdResponse.builder()
            .articleId(articleRepository.save(article).getId())
            .build();
    }

    // 수정
    public ArticleIdResponse changeArticle(Long ArticleId, ArticleRequest request) {
        Article article = articleRepository.findById(ArticleId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.Not_Found));
        if (userService.getCurrentUser().getId() != article.getUser().getId()) {
            throw new BaseException(BaseResponseStatus.Forbidden);
        }
        article.setType(ArticleType.from(request.getType()));
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        return ArticleIdResponse.builder()
            .articleId(articleRepository.save(article).getId())
            .build();
    }

    // 삭제
    public void deleteArticle(Long ArticleId) {
        Article article = articleRepository.findById(ArticleId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.Not_Found));
        if (userService.getCurrentUser().getId() != article.getUser().getId()) {
            throw new BaseException(BaseResponseStatus.Forbidden);
        }
        articleRepository.delete(article);
    }

    // 상세보기
    // 파티 정보 추가 필요
    public ArticleDetailsResponse getArticleDetails(Long ArticleId) {
        Article article = articleRepository.findById(ArticleId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.Not_Found));
        return ArticleDetailsResponse.builder()
            .articleId(article.getId())
            //.partyId()
            .userId(article.getUser().getId())
            .userNickname(article.getUser().getNickname())
            .userProfileImage(article.getUser().getProfileImage())
            .type(article.getType().toString())
            .title(article.getTitle())
            .content(article.getContent())
            .comments(commentService.getCommentsAndReplies(article))
            .commentsCount(commentService.getCommentsCount(article))
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .build();
    }

    // 타입 & 키워드 검색
    public Page<ArticleBriefResponse> searchArticles(String type, String keyword,
        Pageable pageable) {
        Page<Article> articleList;
        if (type.equals("all")) {
            articleList = articleRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword, keyword, pageable);
        } else {
            articleList = articleRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndType(
                keyword, keyword, ArticleType.from(type), pageable);
        }
        List<ArticleBriefResponse> responseList = toArticleBriefResponseList(articleList);
        return new PageImpl<>(responseList, pageable, articleList.getTotalElements());
    }

    // 내 글 조회
    public Page<ArticleBriefResponse> getMyArticles(Pageable pageable) {
        Page<Article> articleList = articleRepository.findByUser(userService.getCurrentUser(), pageable);
        List<ArticleBriefResponse> responseList = toArticleBriefResponseList(articleList);
        return new PageImpl<>(responseList, pageable, articleList.getTotalElements());
    }

    private List<ArticleBriefResponse> toArticleBriefResponseList(Page<Article> articleList) {
        List<ArticleBriefResponse> responseList = new ArrayList<>();
        for (Article article : articleList) {
            responseList.add(ArticleBriefResponse.builder()
                .nickname(article.getUser().getNickname())
                .title(article.getTitle())
                .content(article.getContent())
                .commentsCount(commentService.getCommentsCount(article))
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build());
        }
        return responseList;
    }
}
