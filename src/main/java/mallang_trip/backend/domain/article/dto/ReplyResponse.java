package mallang_trip.backend.domain.article.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.article.entity.Reply;
import mallang_trip.backend.domain.user.entity.User;

@Getter
@Builder
public class ReplyResponse {

    private Long replyId;
    private Long userId;
    private String nickname;
    private String profileImg;
    private String content;
    private LocalDateTime createdAt;
    private Boolean deleted;

    /**
     * Reply 객체로 ReplyResponse 객체를 생성합니다.
     * <p>
     * soft delete 처리된 답글의 경우, content = "[삭제된 댓글]" 로 설정합니다.
     *
     * @param reply 사용할 Reply 객체
     * @return 생성된 ReplyResponse 객체
     */
    public static ReplyResponse of(Reply reply) {
        User user = reply.getUser();
        String content = reply.getDeleted() ? "[삭제된 댓글]" : reply.getContent();
        return ReplyResponse.builder()
            .replyId(reply.getId())
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImg(user.getProfileImage())
            .content(content)
            .createdAt(reply.getCreatedAt())
            .deleted(reply.getDeleted())
            .build();
    }
}
