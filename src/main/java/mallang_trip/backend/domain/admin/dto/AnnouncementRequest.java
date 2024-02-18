package mallang_trip.backend.domain.admin.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.AnnouncementType;

@Builder
@Getter
public class AnnouncementRequest {

	private String title;
	private String content;
	private List<String> images;
	private AnnouncementType type;
}
