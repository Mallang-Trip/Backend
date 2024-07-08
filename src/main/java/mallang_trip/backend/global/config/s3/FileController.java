package mallang_trip.backend.global.config.s3;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Api(tags = "Image Upload API")
@RequiredArgsConstructor
@RestController
public class FileController {

    private final AwsS3Uploader awsS3Uploader;

    @PostMapping("/upload/signup")
    @ApiOperation(value = "회원가입 이미지 업로드")
    public String uploadSignup(@RequestParam("file") MultipartFile multipartFile)
            throws BaseException, IOException {
        String fileName = awsS3Uploader.upload(multipartFile, "profile");
        return fileName;
    }

    @PostMapping("/upload/{dir}")
    @ApiOperation(value = "이미지 업로드")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public String upload(@RequestParam("file") MultipartFile multipartFile,
        @PathVariable String dir) throws BaseException, IOException {
        String fileName = awsS3Uploader.upload(multipartFile, dir);
        return fileName;
    }
}