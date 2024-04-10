package mallang_trip.backend.domain.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.dto.ObjectionBriefResponse;
import mallang_trip.backend.domain.admin.dto.ObjectionDetailsResponse;
import mallang_trip.backend.domain.admin.dto.ObjectionRequest;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags={"Objection API","Admin API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/objection")
public class ObjectionController {

    // private final ObjectionService objectionService;

    /**
     * 이의제기 목록 조회
     * @return List<ObjectionBriefResponse>
     * @throws BaseException
     */
    @GetMapping
    @ApiOperation(value = "(관리자)이의제기 목록 조회")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public BaseResponse<List<ObjectionBriefResponse>> getObjections() throws BaseException {
        //return new BaseResponse<>(objectionService.getObjections());
    }

    /**
     * 이의제기 상세 조회
     * @return ObjectionDetailsResponse
     * @throws BaseException
     */
    @GetMapping("/{objection_id}")
    @ApiOperation(value = "(관리자)이의제기 상세 조회")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public BaseResponse<ObjectionDetailsResponse> viewObjection(@PathVariable(value = "objection_id") Long objectionId) throws BaseException {
        //return new BaseResponse<>(objectionService.viewObjection(objectionId));
    }

    /**
     * 이의제기 처리하기
     * @return String
     * @throws BaseException
     */
    @PutMapping("/{objection_id}")
    @ApiOperation(value = "(관리자)이의제기 처리하기")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public BaseResponse<String> complete(@PathVariable(value = "objection_id") Long objectionId) throws BaseException {
        //objectionService.complete(objectionId);
        return new BaseResponse<>("성공");
    }

    /**
     * 고객 이의제기하기
     * @return String
     * @throws BaseException
     */
    @PostMapping
    @ApiOperation(value = "이의제기하기")
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<String> objection(
        @RequestBody ObjectionRequest request) throws BaseException {
        //objectionService.create(request);
        return new BaseResponse<>("성공");
    }
}
