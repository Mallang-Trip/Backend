package mallang_trip.backend.domain.dto.comment;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.Reply;

@Getter
@Builder
public class ReplyResponse {

    private Long userId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private Boolean deleted;

    public static ReplyResponse of(Reply reply) {
        return ReplyResponse.builder()
            .userId(reply.getUser().getId())
            .nickname(reply.getUser().getNickname())
            .content(reply.getContent())
            .createdAt(reply.getCreatedAt())
            .deleted(reply.getDeleted())
            .build();
    }
}
