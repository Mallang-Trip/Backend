package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.course.CourseDayResponse;
import mallang_trip.backend.domain.dto.course.CourseDetailsResponse;
import mallang_trip.backend.domain.dto.course.CourseBriefResponse;
import mallang_trip.backend.domain.dto.course.CourseRequest;
import mallang_trip.backend.domain.dto.destination.DestinationResponse;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.course.CourseDay;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.course.CourseDayRepository;
import mallang_trip.backend.repository.course.CourseRepository;
import mallang_trip.backend.repository.destination.DestinationRepository;
import mallang_trip.backend.service.user.UserService;
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

    /** 코스 복사 */
    public Course copyCourse(Course course) {
        List<String> images = course.getImages().stream()
            .collect(Collectors.toList());
        Course newCourse = courseRepository.save(Course.builder()
            .owner(userService.getCurrentUser())
            .images(images)
            .totalDays(course.getTotalDays())
            .name(course.getName())
            .capacity(course.getCapacity())
            .totalPrice(course.getTotalPrice())
            .build());
        copyCourseDays(course, newCourse);

        return newCourse;
    }

    /** 기존 Course의 CourseDays를 새로운 Course로 복사 */
    private void copyCourseDays(Course course, Course newCourse) {
        courseDayRepository.findAllByCourse(course)
            .forEach(courseDay -> {
                List<Long> destinations = courseDay.getDestinations().stream()
                    .collect(Collectors.toList());
                courseDayRepository.save(CourseDay.builder()
                    .course(newCourse)
                    .day(courseDay.getDay())
                    .startTime(courseDay.getStartTime())
                    .endTime(courseDay.getEndTime())
                    .hours(courseDay.getHours())
                    .price(courseDay.getPrice())
                    .destinations(destinations)
                    .build());
            });
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
