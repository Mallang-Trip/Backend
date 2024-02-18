package mallang_trip.backend.domains.course.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.course.service.CourseService;
import mallang_trip.backend.domains.global.io.BaseException;
import mallang_trip.backend.domains.global.io.BaseResponse;
import mallang_trip.backend.domains.course.dto.CourseDetailsResponse;
import mallang_trip.backend.domains.course.dto.CourseIdResponse;
import mallang_trip.backend.domains.course.dto.CourseRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
	@ApiOperation(value = "(드라이버)코스 생성")
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<CourseIdResponse> createCourse(@RequestBody @Valid CourseRequest request)
		throws BaseException {
		return new BaseResponse<>(CourseIdResponse.of(courseService.createCourse(request)));
	}

	@PutMapping("/{course_id}")
	@ApiOperation(value = "(드라이버)코스 수정")
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<String> changeCourse(@PathVariable(value = "course_id") Long id,
		@RequestBody @Valid CourseRequest request)
		throws BaseException {
		courseService.changeCourseByDriver(id, request);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/{course_id}")
	@ApiOperation(value = "(드라이버)코스 삭제")
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<String> deleteCourse(@PathVariable(value = "course_id") Long id)
		throws BaseException {
		courseService.deleteCourse(id);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/{course_id}")
	@ApiOperation(value = "코스 상세 조회")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<CourseDetailsResponse> getCourseDetails(
		@PathVariable(value = "course_id") Long id)
		throws BaseException {
		return new BaseResponse<>(courseService.getCourseDetails(id));
	}
}
