package mallang_trip.backend.config.s3;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "File Upload API")
@RequiredArgsConstructor
@RestController
public class FileController {
    private final AwsS3Uploader awsS3Uploader;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile multipartFile) throws BaseException {
        String fileName = awsS3Uploader.upload(multipartFile, "test");
        return fileName;
    }
}