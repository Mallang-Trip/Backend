package mallang_trip.backend.domains.course.service;

import static mallang_trip.backend.domains.global.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.domains.global.io.BaseResponseStatus.Unauthorized;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.course.repository.CourseDayRepository;
import mallang_trip.backend.domains.course.repository.CourseRepository;
import mallang_trip.backend.domains.global.io.BaseException;
import mallang_trip.backend.domains.course.dto.CourseDayResponse;
import mallang_trip.backend.domains.course.dto.CourseDetailsResponse;
import mallang_trip.backend.domains.course.dto.CourseBriefResponse;
import mallang_trip.backend.domains.course.dto.CourseRequest;
import mallang_trip.backend.domains.destination.dto.DestinationResponse;
import mallang_trip.backend.domains.course.entity.Course;
import mallang_trip.backend.domains.user.entity.User;
import mallang_trip.backend.domains.destination.repository.DestinationRepository;
import mallang_trip.backend.domains.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final UserService userService;
    private final CourseRepository courseRepository;
    private final CourseDayRepository courseDayRepository;
    private final DestinationRepository destinationRepository;

    /** 코스 생성 */
    public Course createCourse(CourseRequest request) {
        Course course = courseRepository.save(Course.builder()
            .owner(userService.getCurrentUser())
            .images(request.getImages())
            .totalDays(request.getTotalDays())
            .name(request.getName())
            .capacity(request.getCapacity())
            .totalPrice(request.getTotalPrice())
            .build());
        request.getDays().forEach(day -> courseDayRepository.save(day.toCourseDay(course)));

        return course;
    }

    /** (드라이버) 내 코스 수정 */
    public void changeCourseByDriver(Long courseId, CourseRequest request){
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 내 코스가 아닌 경우
        if(!course.getOwner().equals(userService.getCurrentUser())){
            throw new BaseException(Unauthorized);
        }
        changeCourse(course, request);
    }

    /** 코스 수정  */
    public Course changeCourse(Course course, CourseRequest request) {
        // 코스 정보 변경
        course.setOwner(userService.getCurrentUser());
        course.setImages(request.getImages());
        course.setTotalDays(request.getTotalDays());
        course.setTotalPrice(request.getTotalPrice());
        course.setName(request.getName());
        course.setCapacity(request.getCapacity());
        // courseDay 삭제 후 재생성
        courseDayRepository.deleteAllByCourse(course);
        request.getDays().forEach(day -> courseDayRepository.save(day.toCourseDay(course)));

        return course;
    }

    /** 코스 상세 조회 by id */
    public CourseDetailsResponse getCourseDetails(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BaseException(Not_Found));
        return getCourseDetails(course);
    }

    /** 코스 상세 조회 */
    public CourseDetailsResponse getCourseDetails(Course course){
        List<CourseDayResponse> courseDayResponses = courseDayRepository.findAllByCourse(course)
            .stream()
            .map(courseDay -> {
                List<DestinationResponse> destinations = courseDay.getDestinations().stream()
                    .map(destinationId -> destinationRepository.findById(destinationId)
                        .orElseThrow(() -> new BaseException(Not_Found))
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
            })
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

    /** 코스 삭제 by id */
    public void deleteCourse(Long courseId){
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 코스 생성자가 아닌 경우
        if(!course.getOwner().equals(userService.getCurrentUser())){
            throw new BaseException(Unauthorized);
        }
        deleteCourse(course);
    }

    /** 코스 삭제 */
    private void deleteCourse(Course course) {
        courseRepository.delete(course);
    }

    /** 드라이버의 코스 목록 조회 */
    public List<CourseBriefResponse> getCourseNames(User user) {
        List<CourseBriefResponse> responses =
            courseRepository.findAllByOwnerAndDeleted(user, false)
                .stream()
                .map(CourseBriefResponse::of)
                .collect(Collectors.toList());
        return responses;
    }
}
