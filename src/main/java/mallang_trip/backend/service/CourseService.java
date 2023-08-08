package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.ArrayList;
import java.util.List;
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
import mallang_trip.backend.domain.entity.party.Course;
import mallang_trip.backend.domain.entity.party.CourseDay;
import mallang_trip.backend.domain.entity.destination.Destination;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.party.CourseDayRepository;
import mallang_trip.backend.repository.party.CourseRepository;
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

        for (CourseDayRequest day : request.getDays()) {
            courseDayRepository.save(day.toCourseDay(course));
        }

        return CourseIdResponse.builder()
            .courseId(course.getId())
            .build();
    }

    // 코스 상세 조회
    public CourseDetailsResponse getCourseDetails(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BaseException(Not_Found));

        List<CourseDay> courseDays = courseDayRepository.findAllByCourse(course);
        List<CourseDayResponse> courseDayResponses = new ArrayList<>();

        for (CourseDay courseDay : courseDays) {
            List<Long> destinationIds = courseDay.getDestinations();
            List<DestinationResponse> destinations = new ArrayList<>();
            for (Long destinationId : destinationIds) {
                Destination destination = destinationRepository.findById(destinationId)
                    .orElseThrow(() -> new BaseException(Not_Found));
                destinations.add(DestinationResponse.builder()
                    .destinationId(destination.getId())
                    .name(destination.getName())
                    .address(destination.getAddress())
                    .build());
            }
            courseDayResponses.add(CourseDayResponse.builder()
                .day(courseDay.getDay())
                .startTime(courseDay.getStartTime().toString())
                .endTime(courseDay.getEndTime().toString())
                .hours(courseDay.getHours())
                .price(courseDay.getPrice())
                .destinations(destinations)
                .build());
        }

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

    // 드라이버의 코스 목록 조회
    public List<CourseNameResponse> getCourseName(User user){
        List<Course> courses = courseRepository.findAllByOwner(user);
        List<CourseNameResponse> responses = new ArrayList<>();
        for(Course course : courses){
            responses.add(CourseNameResponse.of(course));
        }
        return responses;
    }
}
