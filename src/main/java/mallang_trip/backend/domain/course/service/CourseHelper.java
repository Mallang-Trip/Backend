package mallang_trip.backend.domain.course.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.repository.PartyRepository;

@Service
@Slf4j(topic = "PARTY_HELPER")
@RequiredArgsConstructor
public class CourseHelper {
	private final PartyRepository partyRepository;

	/**
	 * 해당 코스를 운전하는 드라이버 엔티티를 반환하는 함수
	 * @param course 코스 정보
	 * @return 드라이버 엔티티
	 */
	@Cacheable(value = "getDriverIdByCourse", key = "#course.id")
	public Long getDriverIdByCourse(Course course){
		Party party = partyRepository.findByCourseId(course.getId()).get();


		/*드라이버가 임의로 파티를 만든 경우*/
		if(party == null){
			log.info("드라이버가 만든 코스. course id: {}, driver id: {}", course.getId(), course.getOwner().getId());
			return course.getOwner().getId();
		}

		if(party.getDeleted()) {
			throw new RuntimeException("유효하지 않은 파티입니다.");
		}
		return party.getDriver().getId();
	}
}
