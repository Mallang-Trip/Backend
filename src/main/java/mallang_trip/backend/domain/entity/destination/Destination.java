package mallang_trip.backend.domain.entity.destination;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.constant.DestinationType;
import mallang_trip.backend.domain.dto.destination.DestinationRequest;
import mallang_trip.backend.domain.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE destination SET deleted = true WHERE id = ?")
public class Destination extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private String address;

	@Column
	private Double lon;

	@Column
	private Double lat;

	@Column
	private String content;

	@ElementCollection
	@OrderColumn
	private List<String> images;

	@Enumerated(EnumType.STRING)
	private DestinationType type;

	@Column
	@Builder.Default()
	private Integer views = 0;

	public void increaseViews() {
		this.views++;
	}

	public void change(DestinationRequest request){
		this.name = request.getName();
		this.address = request.getAddress();
		this.lon = request.getLon();
		this.lat = request.getLat();
		this.content = request.getContent();
		this.images = request.getImages();
	}
}
