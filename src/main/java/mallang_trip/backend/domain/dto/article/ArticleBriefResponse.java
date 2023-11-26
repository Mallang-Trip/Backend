package mallang_trip.backend.domain.dto.article;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.community.Article;

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

    public static ArticleBriefResponse of(Article article, Integer commentsCount){
        return ArticleBriefResponse.builder()
            .articleId(article.getId())
            .nickname(article.getUser().getNickname())
            .profileImg(article.getUser().getProfileImage())
            .introduction(article.getUser().getIntroduction())
            .title(article.getTitle())
            .content(article.getContent())
            .image(article.getImages() == null ? null : article.getImages().get(0))
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .commentsCount(commentsCount)
            .build();
    }
}
