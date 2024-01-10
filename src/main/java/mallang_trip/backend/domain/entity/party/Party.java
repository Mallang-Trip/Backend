package mallang_trip.backend.domain.entity.party;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mallang_trip.backend.constant.PartyStatus;
import mallang_trip.backend.domain.entity.BaseEntity;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.user.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE party SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Party extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id", nullable = false)
	private Driver driver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(nullable = false)
	private String region;

	@Column(nullable = false)
	private Integer capacity;

	@Column
	@Builder.Default()
	private Integer headcount = 0;

	@Column
	@Builder.Default()
	private Boolean driverReady = false;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column
	private String content;

	@Enumerated(EnumType.STRING)
	@Builder.Default()
	private PartyStatus status = PartyStatus.WAITING_DRIVER_APPROVAL;

	public void increaseHeadcount(int headcount) {
		this.headcount += headcount;
	}

	/**
	 * 인원 추가 시, 최대 인원 초과 유무 확인
	 */
	public Boolean isHeadcountAvailable(Integer headcount) {
		return this.capacity >= this.headcount + headcount ? true : false;
	}

	/**
	 * 파티 시간이 두 날짜 사이에 있는지 확인
	 */
	public Boolean checkDate(String startDate, String endDate) {
		if (startDate.equals("all") || endDate.equals("all")) {
			return true;
		}
		LocalDate start = LocalDate.parse(startDate);
		LocalDate end = LocalDate.parse(endDate);
		if ((this.startDate.isAfter(start) || this.startDate.isEqual(start))
			&& (this.endDate.isBefore(end) || this.endDate.isEqual(end))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 1인 당 가격이 maxPrice를 넘지 않는 지 확인.
	 */
	public Boolean checkMaxPrice(Integer maxPrice) {
		return this.course.getTotalPrice() / capacity <= maxPrice ? true : false;
	}
}
