package mallang_trip.backend.domain.course.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.swagger.annotations.*;

import javax.validation.Valid;

import io.swagger.annotations.ApiImplicitParam;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.course.dto.CourseSearchCondition;
import mallang_trip.backend.domain.course.service.CourseService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.course.dto.CourseDetailsResponse;
import mallang_trip.backend.domain.course.dto.CourseIdResponse;
import mallang_trip.backend.domain.course.dto.CourseRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Course API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {

	private final CourseService courseService;
	
	@PostMapping
	@ApiOperation(value = "(드라이버)코스 생성")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<CourseIdResponse> createCourse(@RequestBody @Valid CourseRequest request)
		throws BaseException {
		return new BaseResponse<>(CourseIdResponse.of(courseService.createCourse(request)));
	}


	@GetMapping("/list")
	@ApiOperation(value = "코스 전체 조회")
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	public BaseResponse<List<CourseDetailsResponse>> getCourse(
		@RequestParam String region,
		@RequestParam Integer headcount,
		@RequestParam Integer maxPrice) throws UnsupportedEncodingException {

		region = URLDecoder.decode(region, StandardCharsets.UTF_8);
		CourseSearchCondition condition = new CourseSearchCondition(region, headcount, maxPrice);

		return new BaseResponse<>(courseService.getCourseList(condition));
	}

	@PutMapping("/{course_id}")
	@ApiOperation(value = "(드라이버)코스 수정")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
			@ApiImplicitParam(name = "course_id", value = "course_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
			@ApiResponse(code = 404, message = "코스를 찾을 수 없습니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<String> changeCourse(@PathVariable(value = "course_id") Long courseId,
		@RequestBody @Valid CourseRequest request) throws BaseException {
		courseService.changeCourseByDriver(courseId, request);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/{course_id}")
	@ApiOperation(value = "(드라이버)코스 삭제")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
			@ApiImplicitParam(name = "course_id", value = "course_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "삭제 권한이 없는 사용자입니다."),
			@ApiResponse(code = 404, message = "코스를 찾을 수 없습니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<String> deleteCourse(@PathVariable(value = "course_id") Long courseId)
		throws BaseException {
		courseService.deleteCourse(courseId);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/{course_id}")
	@ApiOperation(value = "코스 상세 조회")
	@ApiImplicitParam(name = "course_id", value = "course_id", required = true, paramType = "path", dataTypeClass = Long.class)
	@ApiResponses({
			@ApiResponse(code = 404, message = "코스를 찾을 수 없습니다.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<CourseDetailsResponse> getCourseDetails(
		@PathVariable(value = "course_id") Long courseId) throws BaseException {
		return new BaseResponse<>(courseService.getCourseDetails(courseId));
	}
}
