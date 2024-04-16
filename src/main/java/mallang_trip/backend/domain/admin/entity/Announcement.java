package mallang_trip.backend.domain.admin.entity;

import java.util.List;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mallang_trip.backend.domain.admin.constant.AnnouncementType;
import mallang_trip.backend.domain.admin.dto.AnnouncementRequest;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.global.entity.BaseEntity;

@Entity
@Table
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Announcement extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	private AnnouncementType type;

	@ElementCollection
	@OrderColumn
	private List<String> images;

	public void modify(AnnouncementRequest request){
		this.title=request.getTitle();
		this.content=request.getContent();
		this.images=request.getImages();
		this.type=request.getType();
	}
}
