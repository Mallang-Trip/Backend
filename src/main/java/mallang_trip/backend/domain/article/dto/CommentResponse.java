package mallang_trip.backend.domain.article.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.article.entity.Comment;
import mallang_trip.backend.domain.user.entity.User;

@Getter
@Builder
public class CommentResponse {

    private Long commentId;
    private Long userId;
    private String nickname;
    private String profileImg;
    private String content;
    private List<ReplyResponse> replies;
    private LocalDateTime createdAt;
    private Boolean deleted;

    /**
     * Comment 객체로 CommentResponse 객체를 생성합니다.
     * <p>
     * soft delete 처리된 답글의 경우, content = "[삭제된 댓글]" 로 설정합니다.
     *
     * @param comment 사용할 Comment 객체
     * @param replies 댓글 하위에 생성된 답글들의 정보를 담은 List<ReplyResponse> 객체
     * @return 생성된 CommentResponse 객체
     */
    public static CommentResponse of(Comment comment, List<ReplyResponse> replies) {
        User user = comment.getUser();
        String content = comment.getDeleted() ? "[삭제된 댓글]" : comment.getContent();
        return CommentResponse.builder()
            .commentId(comment.getId())
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImg(user.getProfileImage())
            .content(content)
            .replies(replies)
            .createdAt(comment.getCreatedAt())
            .deleted(comment.getDeleted())
            .build();
    }
}
