package mallang_trip.backend.domain.dto.comment;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.community.Reply;
import mallang_trip.backend.domain.entity.user.User;

@Getter
@Builder
public class ReplyResponse {

    private Long userId;
    private String nickname;
    private String profileImg;
    private String content;
    private LocalDateTime createdAt;
    private Boolean deleted;

    public static ReplyResponse of(Reply reply) {
        User user = reply.getUser();
        String content = reply.getDeleted() ? "[삭제된 댓글]" : reply.getContent();
        return ReplyResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImg(user.getProfileImage())
            .content(content)
            .createdAt(reply.getCreatedAt())
            .deleted(reply.getDeleted())
            .build();
    }
}
