package mallang_trip.backend.domain.dto.article;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.ArticleType;
import mallang_trip.backend.domain.dto.comment.CommentResponse;

@Getter
@Builder
public class ArticleDetailsResponse {

    private Long articleId;
    private Long userId;
    private Long partyId;
    private String partyName;
    private String userNickname;
    private String userProfileImage;
    private ArticleType type;
    private String title;
    private String content;
    private Boolean dibs;
    private int commentsCount;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
