package mallang_trip.backend.domain.dto.admin;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.admin.Suspension;

@Getter
@Builder
public class SuspendingUserResponse {

	private Long userId;
	private String userNickname;
	private int duration;
	private LocalDateTime createdAt;

	public static SuspendingUserResponse of(Suspension suspension){
		return SuspendingUserResponse.builder()
			.userId(suspension.getUser().getId())
			.userNickname(suspension.getUser().getNickname())
			.duration(suspension.getDuration())
			.createdAt(suspension.getCreatedAt())
			.build();
	}
}