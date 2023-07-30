package mallang_trip.backend.domain.dto.comment;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyCommentResponse {

    private Long articleId;
    private String nickname;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}

