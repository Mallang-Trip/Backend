package mallang_trip.backend.domain.dto.admin;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.AnnouncementType;
import mallang_trip.backend.domain.entity.admin.Announcement;

@Getter
@Builder
public class AnnouncementDetailsResponse {

	private Long announcementId;
	private String title;
	private String content;
	private List<String> images;
	private AnnouncementType type;
	private LocalDateTime createdAt;

	public static AnnouncementDetailsResponse of(Announcement announcement){
		return AnnouncementDetailsResponse.builder()
			.announcementId(announcement.getId())
			.title(announcement.getTitle())
			.content(announcement.getContent())
			.images(announcement.getImages())
			.type(announcement.getType())
			.createdAt(announcement.getCreatedAt())
			.build();
	}
}
