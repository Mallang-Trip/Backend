package mallang_trip.backend.domain.dto.admin;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.AnnouncementType;
import mallang_trip.backend.domain.entity.admin.Announcement;

@Getter
@Builder
public class AnnouncementBriefResponse {

	private Long announcementId;
	private String title;
	private AnnouncementType type;
	private LocalDateTime createdAt;

	public static AnnouncementBriefResponse of(Announcement announcement){
		return AnnouncementBriefResponse.builder()
			.announcementId(announcement.getId())
			.title(announcement.getTitle())
			.type(announcement.getType())
			.createdAt(announcement.getCreatedAt())
			.build();
	}
}
