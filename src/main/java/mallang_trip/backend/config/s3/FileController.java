package mallang_trip.backend.config.s3;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "Image Upload API")
@RequiredArgsConstructor
@RestController
public class FileController {

    private final AwsS3Uploader awsS3Uploader;

    @PostMapping("/upload/{dir}")
    @ApiOperation(value = "이미지 업로드")
    public String upload(@RequestParam("file") MultipartFile multipartFile,
        @PathVariable String type) throws BaseException {
        String fileName = awsS3Uploader.upload(multipartFile, type);
        return fileName;
    }
}