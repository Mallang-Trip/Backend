package mallang_trip.backend.domain.user.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.user.entity.User;

@Getter
@Builder
public class UserBriefResponse {

    private Long userId;
    private String nickname;
    private String profileImg;
    private String introduction;
    private Boolean deleted;
    private LocalDate createdAt;
    //private Boolean suspended;
    private Integer suspensionDuration;

    public static UserBriefResponse of(User user){
        return UserBriefResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImg(user.getProfileImage())
            .introduction(user.getIntroduction())
            .deleted(user.getDeleted())
            .createdAt(user.getCreatedAt().toLocalDate())
            .build();
    }
    public static UserBriefResponse of(User user, Integer duration) {
        return UserBriefResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImg(user.getProfileImage())
            .introduction(user.getIntroduction())
            .deleted(user.getDeleted())
            .createdAt(user.getCreatedAt().toLocalDate())
            .suspensionDuration(duration)
            .build();
    }
}
