package mallang_trip.backend.domain.course.entity;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.course.dto.CourseRequest;
import mallang_trip.backend.global.entity.BaseEntity;
import mallang_trip.backend.domain.user.entity.User;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE course SET deleted = true WHERE id = ?")
public class Course extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@ElementCollection
	@OrderColumn
	private List<String> images;

	@Column(name = "total_days")
	private Integer totalDays;

	@Column
	private String name;

	@Column
	private Integer capacity;

	@Column
	private String region;

	@Column(name = "total_price")
	private Integer totalPrice;

	@Column(name = "discount_price")
	@Builder.Default()
	private Integer discountPrice = 0;

	public void increaseDiscountPrice(Integer amount) {
		this.discountPrice += amount;
	}

	public void modify(CourseRequest request){
		this.images = request.getImages();
		this.totalDays = request.getTotalDays();
		this.totalPrice = request.getTotalPrice();
		this.name = request.getName();
		this.region = request.getRegion();
		this.capacity = request.getCapacity();
	}
}
