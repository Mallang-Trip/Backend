package mallang_trip.backend.domain.article.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import mallang_trip.backend.domain.article.entity.Comment;
import mallang_trip.backend.domain.user.entity.User;

@Getter
@Setter
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

    public static CommentResponse of(Comment comment) {
        User user = comment.getUser();
        String content = comment.getDeleted() ? "[삭제된 댓글]" : comment.getContent();
        return CommentResponse.builder()
            .commentId(comment.getId())
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImg(user.getProfileImage())
            .content(content)
            .createdAt(comment.getCreatedAt())
            .deleted(comment.getDeleted())
            .build();
    }
}
