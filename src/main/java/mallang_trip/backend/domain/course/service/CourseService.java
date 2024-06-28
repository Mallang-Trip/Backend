package mallang_trip.backend.domain.course.service;

import static mallang_trip.backend.domain.destination.exception.DestinationExceptionStatus.CANNOT_FOUND_DESTINATION;
import static mallang_trip.backend.domain.region.exception.RegionException.REGION_NOT_FOUND;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.course.dto.CourseDayRequest;
import mallang_trip.backend.domain.course.entity.CourseDay;
import mallang_trip.backend.domain.course.repository.CourseDayRepository;
import mallang_trip.backend.domain.course.repository.CourseRepository;
import mallang_trip.backend.domain.region.repository.RegionRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.course.dto.CourseDayResponse;
import mallang_trip.backend.domain.course.dto.CourseDetailsResponse;
import mallang_trip.backend.domain.course.dto.CourseBriefResponse;
import mallang_trip.backend.domain.course.dto.CourseRequest;
import mallang_trip.backend.domain.destination.dto.DestinationResponse;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.destination.repository.DestinationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

	private final CurrentUserService currentUserService;
	private final CourseRepository courseRepository;
	private final CourseDayRepository courseDayRepository;
	private final DestinationRepository destinationRepository;
	private final RegionRepository regionRepository;

	/**
	 * (드라이버) 코스 생성
	 *
	 * @param request 코스 생성 요청
	 *                images: 코스 이미지 URL 배열
	 *                totalDays: 총 일수
	 *                name: 코스 이름
	 *                capacity: 최대 수용인원
	 *                region: 지역 이름
	 *                totalPrice: 총 가격
	 *                days: 코스 일자 배열
	 * @return 생성된 코스
	 * @throws BaseException Unauthorized 유저
	 */
	public Course createCourse(CourseRequest request) {
		if(!regionRepository.existsByName(request.getRegion())){
			throw new BaseException(REGION_NOT_FOUND);
		}

		Course course = courseRepository.save(Course.builder()
			.owner(currentUserService.getCurrentUser())
			.images(request.getImages())
			.totalDays(request.getTotalDays())
			.name(request.getName())
			.capacity(request.getCapacity())
			.region(request.getRegion())
			.totalPrice(request.getTotalPrice())
			.build());
		createCourseDays(request.getDays(), course);

		return course;
	}

	/**
	 * CourseDay 생성
	 */
	public void createCourseDays(List<CourseDayRequest> request, Course course) {
		request.forEach(day -> courseDayRepository.save(day.toCourseDay(course)));
	}

	/**
	 * (드라이버) 내 코스 수정
	 * @param courseId 코스 ID
	 *                 images: 코스 이미지 URL 배열
	 *                 totalDays: 총 일수
	 *                 name: 코스 이름
	 *                 capacity: 최대 수용인원
	 *                 region: 지역
	 *                 totalPrice: 총 가격
	 *                 days: 코스 일자 배열
	 *
	 * @throws BaseException Not_Found 코스를 찾을 수 없는 경우
	 * @throws BaseException Forbidden 코스의 소유자가 아닌 경우
	 */
	public void changeCourseByDriver(Long courseId, CourseRequest request) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new BaseException(Not_Found));
		// 내 코스가 아닌 경우
		if (!course.getOwner().equals(currentUserService.getCurrentUser())) {
			throw new BaseException(Forbidden);
		}
		// 지역 확인
		if(!regionRepository.existsByName(request.getRegion())){
			throw new BaseException(REGION_NOT_FOUND);
		}
		// 코스 정보 변경
		course.modify(request);
		// 기존 courseDay 삭제 후 재생성
		courseDayRepository.deleteAllByCourse(course);
		createCourseDays(request.getDays(), course);
	}

	/**
	 * 코스 상세 조회 (by course_id)
	 * @param courseId 코스 ID
	 * @return 코스 상세 정보
	 * @throws BaseException Not_Found 코스를 찾을 수 없는 경우
	 */
	public CourseDetailsResponse getCourseDetails(Long courseId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new BaseException(Not_Found));
		return getCourseDetails(course);
	}

	/**
	 * 코스 상세 조회 (Course -> CourseDetailsResponse)
	 * @param course 코스
	 * @return 코스 상세 정보
	 */
	public CourseDetailsResponse getCourseDetails(Course course) {
		List<CourseDayResponse> courseDayResponses = courseDayRepository.findAllByCourse(course)
			.stream()
			.map(this::courseDayToResponse)
			.collect(Collectors.toList());

		return CourseDetailsResponse.builder()
			.courseId(course.getId())
			.images(course.getImages())
			.totalDays(course.getTotalDays())
			.name(course.getName())
			.capacity(course.getCapacity())
			.region(course.getRegion())
			.totalPrice(course.getTotalPrice())
			.discountPrice(course.getDiscountPrice())
			.days(courseDayResponses)
			.build();
	}

	/**
	 * CourseDay -> CourseDayResponse
	 */
	private CourseDayResponse courseDayToResponse(CourseDay courseDay){
		List<DestinationResponse> destinations = courseDay.getDestinations().stream() // 병렬 처리
			.map(destinationId ->
				destinationRepository.findById(destinationId)
				.orElseThrow(() -> new BaseException(CANNOT_FOUND_DESTINATION))
			)
			.map(DestinationResponse::of)
			.collect(Collectors.toList());

		return CourseDayResponse.builder()
			.day(courseDay.getDay())
			.startTime(courseDay.getStartTime().toString())
			.endTime(courseDay.getEndTime().toString())
			.hours(courseDay.getHours())
			.price(courseDay.getPrice())
			.destinations(destinations)
			.build();
	}

	/**
	 * 코스 삭제 (by course_id)
	 * @param courseId 코스 ID
	 * @throws BaseException Not_Found 코스를 찾을 수 없는 경우
	 * @throws BaseException Forbidden 코스의 소유자가 아닌 경우
	 * @throws BaseException Forbidden 코스가 삭제된 경우
	 */
	public void deleteCourse(Long courseId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new BaseException(Not_Found));
		// 코스 생성자가 아닌 경우
		if (!course.getOwner().equals(currentUserService.getCurrentUser())) {
			throw new BaseException(Forbidden);
		}
		// 삭제
		courseRepository.delete(course);
	}

	/**
	 * 드라이버의 코스 목록 조회
	 * @param user 유저
	 * @return 코스 목록
	 */
	public List<CourseBriefResponse> getDriversCourses(User user) {
		return courseRepository.findAllByOwnerAndDeleted(user, false)
				.stream()
				.map(CourseBriefResponse::of)
				.collect(Collectors.toList());
	}

	/**
	 * 코스의 시작 시간 ~ 종료 시간 조회
	 *
	 * @param course 조회할 Course 객체
	 * @return HH:MM ~ HH:MM 형식
	 */
	public String getStartAndEndTime(Course course){
		List<CourseDay> courseDays = courseDayRepository.findAllByCourse(course);
		if(courseDays == null || courseDays.isEmpty()){
			return null;
		}

		LocalTime startTime = courseDays.get(0).getStartTime();
		LocalTime endTime = courseDays.get(0).getEndTime();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

		String content = new StringBuilder()
			.append(startTime.format(formatter))
			.append(" ~ ")
			.append(endTime.format(formatter))
			.toString();

		return content;
	}
}
