package mallang_trip.backend.domain.article.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.article.constant.ArticleType;

@Getter
@Builder
public class ArticleDetailsResponse {

    private Long articleId;
    private Long userId;
    private Long partyId;
    private String partyName;
    private String nickname;
    private String profileImg;
    private ArticleType type;
    private String title;
    private String content;
    private List<String> images;
    private Boolean dibs;
    private int commentsCount;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
