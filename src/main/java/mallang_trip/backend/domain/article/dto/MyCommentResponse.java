package mallang_trip.backend.domain.article.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.user.entity.User;

@Getter
@Builder
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

    /**
     * MyCommentResponse 객체를 생성하는 메소드입니다.
     *
     * @param article 댓글이나 답글이 달린 Article 객체
     * @param content 댓글이나 답글의 내용 값
     * @param createdAt 댓글이나 답글의 생성시간 값
     * @param commentsCount 댓글이나 답글이 달린 Article의 댓글과 답글의 총 개수
     * @return 생성된 MyCommentResponse 객체
     */
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

