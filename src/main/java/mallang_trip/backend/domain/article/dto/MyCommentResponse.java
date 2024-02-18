package mallang_trip.backend.domain.article.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.user.entity.User;

@Getter
@Builder
@AllArgsConstructor
public class MyCommentResponse {

    private Long articleId;
    private String profileImg;
    private String nickname;
    private String introduction;
    private String title;
    private String image;
    private String content;
    private Boolean articleDeleted;
    private LocalDateTime createdAt;
    private Integer commentsCount;

    public static MyCommentResponse of(Article article, String content, LocalDateTime createdAt,
        Integer commentsCount) {
        User user = article.getUser();
        return MyCommentResponse.builder()
            .articleId(article.getId())
            .profileImg(user.getProfileImage())
            .nickname(user.getNickname())
            .introduction(user.getIntroduction())
            .title(article.getTitle())
            .image(article.getImages().isEmpty() ? null : article.getImages().get(0))
            .content(content)
            .articleDeleted(article.getDeleted())
            .createdAt(createdAt)
            .commentsCount(commentsCount)
            .build();
    }
}

