package mallang_trip.backend.domain.identification.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.identification.dto.ImpUidResponse;
import mallang_trip.backend.domain.identification.dto.UserIdentificationRequest;
import mallang_trip.backend.domain.identification.service.IdentificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Identification API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/identification")
public class IdentificationController {

    private final IdentificationService identificationService;

    @ApiOperation(value = "본인인증 인증번호 요청")
    @PostMapping
    @PreAuthorize("permitAll()") // anyone
    public BaseResponse<ImpUidResponse> request(@RequestBody @Valid UserIdentificationRequest request)
        throws BaseException {
        return new BaseResponse<>(identificationService.request(request));
    }


    @ApiOperation(value = "본인인증 인증번호 확인")
    @PostMapping("/confirm")
    @PreAuthorize("permitAll()") // anyone
    public BaseResponse<ImpUidResponse> confirm(@RequestParam String impUid, @RequestParam String otp)
        throws BaseException {
        return new BaseResponse<>(identificationService.confirm(impUid, otp));
    }
}
