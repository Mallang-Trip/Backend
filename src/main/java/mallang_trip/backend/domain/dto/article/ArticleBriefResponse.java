package mallang_trip.backend.domain.dto.article;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleBriefResponse {

    private String nickname;
    private String title;
    private String content;
    private Long partyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int commentsCount;

}
