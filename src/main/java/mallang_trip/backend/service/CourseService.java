package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.course.CourseDayResponse;
import mallang_trip.backend.domain.dto.course.CourseDetailsResponse;
import mallang_trip.backend.domain.dto.course.CourseIdResponse;
import mallang_trip.backend.domain.dto.course.CourseNameResponse;
import mallang_trip.backend.domain.dto.course.CourseRequest;
import mallang_trip.backend.domain.dto.course.CourseDayRequest;
import mallang_trip.backend.domain.dto.course.DestinationResponse;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.course.CourseDay;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.course.CourseDayRepository;
import mallang_trip.backend.repository.course.CourseRepository;
import mallang_trip.backend.repository.destination.DestinationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final UserService userService;
    private final CourseRepository courseRepository;
    private final CourseDayRepository courseDayRepository;
    private final DestinationRepository destinationRepository;

    // 코스 생성
    public CourseIdResponse createCourse(CourseRequest request) {
        Course course = courseRepository.save(Course.builder()
            .owner(userService.getCurrentUser())
            .images(request.getImages())
            .totalDays(request.getTotalDays())
            .name(request.getName())
            .capacity(request.getCapacity())
            .totalPrice(request.getTotalPrice())
            .build());
        request.getDays().forEach(day -> courseDayRepository.save(day.toCourseDay(course)));

        return CourseIdResponse.builder()
            .courseId(course.getId())
            .build();
    }

    // 코스 수정 (기존 코스 삭제 -> 새 코스 생성)
    public CourseIdResponse changeCourse(Long courseId, CourseRequest request) {
        deleteCourse(courseId);
        return createCourse(request);
    }

    // 코스 상세 조회
    public CourseDetailsResponse getCourseDetails(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BaseException(Not_Found));

        List<CourseDayResponse> courseDayResponses = courseDayRepository.findAllByCourse(course)
            .stream()
            .map(courseDay -> {
                List<DestinationResponse> destinations = courseDay.getDestinations().stream()
                    .map(destinationId -> destinationRepository.findById(destinationId)
                        .orElseThrow(() -> new BaseException(Not_Found))
                    )
                    .map(destination -> DestinationResponse.builder()
                        .destinationId(destination.getId())
                        .name(destination.getName())
                        .address(destination.getAddress())
                        .build()
                    )
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
            .days(courseDayResponses)
            .build();
    }

    // 코스 복사
    public CourseIdResponse copyCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BaseException(Not_Found));
        Course newCourse = courseRepository.save(Course.builder()
            .owner(userService.getCurrentUser())
            .images(course.getImages())
            .totalDays(course.getTotalDays())
            .name(course.getName())
            .capacity(course.getCapacity())
            .totalPrice(course.getTotalPrice())
            .build());
        copyCourseDay(course, newCourse);

        return CourseIdResponse.builder()
            .courseId(newCourse.getId())
            .build();
    }

    public void copyCourseDay(Course course, Course newCourse) {
        courseDayRepository.findAllByCourse(course)
            .forEach(courseDay -> {
                courseDayRepository.save(CourseDay.builder()
                    .course(newCourse)
                    .day(courseDay.getDay())
                    .startTime(courseDay.getStartTime())
                    .endTime(courseDay.getEndTime())
                    .hours(courseDay.getHours())
                    .price(courseDay.getPrice())
                    .destinations(courseDay.getDestinations())
                    .build());
            });
    }

    // 코스 삭제
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BaseException(Not_Found));
        courseRepository.delete(course);
    }

    // 드라이버의 코스 목록 조회
    public List<CourseNameResponse> getCourseName(User user) {
        List<CourseNameResponse> responses =
            courseRepository.findAllByOwnerAndDeleted(user, false)
                .stream()
                .map(CourseNameResponse::of)
                .collect(Collectors.toList());
        return responses;
    }
}
