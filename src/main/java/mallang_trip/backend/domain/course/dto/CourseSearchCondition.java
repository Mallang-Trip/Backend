package mallang_trip.backend.domain.course.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CourseSearchCondition {
	private String region;// 지역
	private Integer headcount;// 인원 수
	private Integer maxPrice;// 최대 가격

	public CourseSearchCondition(String region, Integer headcount, Integer maxPrice) {
		this.region = region;
		this.headcount = headcount;
		this.maxPrice = maxPrice;
	}
}
