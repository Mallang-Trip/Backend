package mallang_trip.backend.domain.dto.comment;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import mallang_trip.backend.domain.entity.Comment;

@Getter
@Setter
@Builder
public class CommentResponse {

    private Long userId;
    private String nickname;
    private String content;
    private List<ReplyResponse> replies;
    private LocalDateTime createdAt;
    private Boolean deleted;

    public static CommentResponse of(Comment comment) {
        return CommentResponse.builder()
            .userId(comment.getUser().getId())
            .nickname(comment.getUser().getNickname())
            .content(comment.getContent())
            .createdAt(comment.getCreatedAt())
            .deleted(comment.getDeleted())
            .build();
    }
}
