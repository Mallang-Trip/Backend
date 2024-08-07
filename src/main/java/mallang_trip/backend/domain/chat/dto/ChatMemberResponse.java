package mallang_trip.backend.domain.chat.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.chat.entity.ChatMember;
import mallang_trip.backend.domain.user.entity.User;

@Getter
@Builder
public class ChatMemberResponse {

	private Long userId;
	private String nickname;
	private String profileImg;
	private String introduction;
	private Boolean deleted;
	private LocalDate createdAt;
	private Boolean isMyParty;

	public static ChatMemberResponse of(ChatMember member, boolean isMyParty){
		User user = member.getUser();
		return ChatMemberResponse.builder()
			.userId(user.getId())
			.nickname(user.getNickname())
			.profileImg(user.getProfileImage())
			.introduction(user.getIntroduction())
			.deleted(user.getDeleted())
			.createdAt(user.getCreatedAt().toLocalDate())
			.isMyParty(isMyParty)
			.build();
	}
}
