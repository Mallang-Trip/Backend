package mallang_trip.backend.domain.dto.user;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.user.User;

@Getter
@Builder
public class UserBriefResponse {

    private Long userId;
    private String nickname;
    private String profileImg;
    private String introduction;
    private Boolean deleted;
    //private Boolean suspended;

    public static UserBriefResponse of(User user){
        return UserBriefResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImg(user.getProfileImage())
            .introduction(user.getIntroduction())
            .deleted(user.getDeleted())
            .build();
    }
}
