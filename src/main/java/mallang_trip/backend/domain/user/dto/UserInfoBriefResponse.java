package mallang_trip.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.user.entity.User;

import java.time.LocalDateTime;
@Getter
@Builder
public class UserInfoBriefResponse {

    private Long id;
    private String loginId;
    private String userNickname;
    private Integer duration;
    private LocalDateTime createdAt;

    private String Introduction;

    private String profileImg;

    public static UserInfoBriefResponse of(User user, Integer duration){
        return UserInfoBriefResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .userNickname(user.getNickname())
                .duration(duration)
                .profileImg(user.getProfileImage())
                .Introduction(user.getIntroduction())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
