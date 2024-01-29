package mallang_trip.backend.domain.dto.admin;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.AnnouncementType;

@Builder
@Getter
public class AnnouncementRequest {

	private String title;
	private String content;
	private List<String> images;
	private AnnouncementType type;
}
