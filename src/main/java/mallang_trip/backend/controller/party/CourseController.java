package mallang_trip.backend.controller.party;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.course.CourseDetailsResponse;
import mallang_trip.backend.domain.dto.course.CourseIdResponse;
import mallang_trip.backend.domain.dto.course.CourseRequest;
import mallang_trip.backend.service.CourseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Course API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ApiOperation(value = "코스 생성")
    public BaseResponse<CourseIdResponse> createCourse(@RequestBody CourseRequest request)
        throws BaseException {
        return new BaseResponse<>(courseService.createCourse(request));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "코스 상세 조회")
    public BaseResponse<CourseDetailsResponse> getCourseDetails(@PathVariable Long id) throws BaseException{
        return new BaseResponse<>(courseService.getCourseDetails(id));
    }
}
