package mallang_trip.backend.domain.course.service;

import static mallang_trip.backend.domain.destination.exception.DestinationExceptionStatus.CANNOT_FOUND_DESTINATION;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.course.dto.CourseDayRequest;
import mallang_trip.backend.domain.course.entity.CourseDay;
import mallang_trip.backend.domain.course.repository.CourseDayRepository;
import mallang_trip.backend.domain.course.repository.CourseRepository;
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
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

	private final CurrentUserService currentUserService;
	private final CourseRepository courseRepository;
	private final CourseDayRepository courseDayRepository;
	private final DestinationRepository destinationRepository;

	/**
	 * 코스 생성
	 */
	public Course createCourse(CourseRequest request) {
		Course course = courseRepository.save(Course.builder()
			.owner(currentUserService.getCurrentUser())
			.images(request.getImages())
			.totalDays(request.getTotalDays())
			.name(request.getName())
			.capacity(request.getCapacity())
			.totalPrice(request.getTotalPrice())
			.build());
		createCourseDays(request.getDays(), course);

		return course;
	}

	/**
	 * CourseDay 생성
	 */
	private void createCourseDays(List<CourseDayRequest> request, Course course) {
		request.forEach(day -> courseDayRepository.save(day.toCourseDay(course)));
	}

	/**
	 * (드라이버) 내 코스 수정
	 */
	public void changeCourseByDriver(Long courseId, CourseRequest request) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new BaseException(Not_Found));
		// 내 코스가 아닌 경우
		if (!course.getOwner().equals(currentUserService.getCurrentUser())) {
			throw new BaseException(Forbidden);
		}
		// 코스 정보 변경
		course.modify(request);
		// 기존 courseDay 삭제 후 재생성
		courseDayRepository.deleteAllByCourse(course);
		createCourseDays(request.getDays(), course);
	}

	/**
	 * 코스 상세 조회 (by course_id)
	 */
	public CourseDetailsResponse getCourseDetails(Long courseId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new BaseException(Not_Found));
		return getCourseDetails(course);
	}

	/**
	 * 코스 상세 조회 (Course -> CourseDetailsResponse)
	 */
	public CourseDetailsResponse getCourseDetails(Course course) {
		List<CourseDayResponse> courseDayResponses = courseDayRepository.findAllByCourse(course)
			.stream()
			.map(courseDay -> courseDayToResponse(courseDay))
			.collect(Collectors.toList());

		return CourseDetailsResponse.builder()
			.courseId(course.getId())
			.images(course.getImages())
			.totalDays(course.getTotalDays())
			.name(course.getName())
			.capacity(course.getCapacity())
			.totalPrice(course.getTotalPrice())
			.discountPrice(course.getDiscountPrice())
			.days(courseDayResponses)
			.build();
	}

	/**
	 * CourseDay -> CourseDayResponse
	 */
	private CourseDayResponse courseDayToResponse(CourseDay courseDay){
		List<DestinationResponse> destinations = courseDay.getDestinations().stream()
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
	 */
	public List<CourseBriefResponse> getDriversCourses(User user) {
		return courseRepository.findAllByOwnerAndDeleted(user, false)
				.stream()
				.map(CourseBriefResponse::of)
				.collect(Collectors.toList());
	}
}
