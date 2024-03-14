package mallang_trip.backend.domain.article.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.article.entity.Article;

@Getter
@Builder
public class ArticleBriefResponse {

    private Long articleId;
    private String nickname;
    private String profileImg;
    private String introduction;
    private String title;
    private String content;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer commentsCount;

    /**
     * Article 객체로 ArticleBriefResponse 객체를 생성합니다.
     *
     * @param article 사용할 Article 객체
     * @param commentsCount Article의 댓글과 답글의 총 개수
     * @return 생성된 ArticleBriefResponse 객체
     */
    public static ArticleBriefResponse of(Article article, Integer commentsCount){
        return ArticleBriefResponse.builder()
            .articleId(article.getId())
            .nickname(article.getUser().getNickname())
            .profileImg(article.getUser().getProfileImage())
            .introduction(article.getUser().getIntroduction())
            .title(article.getTitle())
            .content(article.getContent())
            .image(article.getImages().isEmpty() ? null : article.getImages().get(0))
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .commentsCount(commentsCount)
            .build();
    }
}
