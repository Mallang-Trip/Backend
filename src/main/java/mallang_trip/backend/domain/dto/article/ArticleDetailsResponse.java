package mallang_trip.backend.domain.dto.article;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleDetailsResponse {

    private Long articleId;
    private Long userId;
    private Long partyId;
    private String userNickname;
    private String type;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int commentCount;
}
