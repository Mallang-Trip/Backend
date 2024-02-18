package mallang_trip.backend.domains.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.admin.constant.AnnouncementType;
import mallang_trip.backend.domains.admin.entity.Announcement;

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
