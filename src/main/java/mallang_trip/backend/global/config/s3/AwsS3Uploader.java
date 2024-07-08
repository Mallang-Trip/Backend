package mallang_trip.backend.global.config.s3;

import static mallang_trip.backend.global.io.BaseResponseStatus.FILE_CONVERT_ERROR;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AwsS3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    public String upload(MultipartFile multipartFile, String dirName) throws BaseException, IOException {
        File uploadFile = convert(multipartFile)        // 파일 생성
                .orElseThrow(() -> new BaseException(FILE_CONVERT_ERROR));

        // 파일 용량이 10MB 이상일 경우 압축
        if(uploadFile.length() > 10 * 1024 * 1024) {
            // 파일 압축
            BufferedImage image = ImageIO.read(uploadFile);
            BufferedImage newImage = image;
            if (image.getHeight() > 1000 || image.getWidth() > 1000) {
                // 이미지 크기가 1000x1000 이상일 경우 리사이징
                // 비율 유지
                int newWidth = 1000;
                int newHeight = (int) Math.round(image.getHeight() * (newWidth / (double) image.getWidth()));
                newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);  // 새 이미지 생성
                Graphics2D g2d = newImage.createGraphics(); // 그래픽 객체 생성
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // 이미지 품질 설정 (높은 품질)
                g2d.drawImage(image, 0, 0, newWidth, newHeight, null); // 이미지 그리기 (리사이징)
                g2d.dispose();  // 그래픽 객체 해제
            }

            // 압축
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)){
                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                if(!writers.hasNext()) {
                    throw new IOException("No writers found");
                }
                ImageWriter writer = writers.next();
                writer.setOutput(ios);

                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.5f); // 압축률 설정 (0.5)

                writer.write(null, new IIOImage(newImage, null, null), param);
                writer.dispose();
            }

            // 압축된 이미지 파일 생성
            try(FileOutputStream fos = new FileOutputStream(uploadFile)) {
                os.writeTo(fos); // 압축된 이미지를 원래 파일에 덮어쓰기
            }
        }
        return upload(uploadFile, dirName);
    }

    private String upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);    // s3로 업로드
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    // 1. 로컬에 파일생성
    private Optional<File> convert(MultipartFile file) throws BaseException {
        File convertFile = new File(file.getOriginalFilename());
        try {
            if (convertFile.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                    fos.write(file.getBytes());
                }
                return Optional.of(convertFile);
            }
        } catch (IOException e) {
            throw new BaseException(FILE_CONVERT_ERROR);
        }

        return Optional.empty();
    }

    // 2. S3에 파일업로드
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        log.info("File Upload : {}", fileName);
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // 3. 로컬에 생성된 파일삭제
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("File delete success");
            return;
        }
        log.info("File delete fail");
    }


    public void delete(String fileName) {
        log.info("File Delete : {}", fileName);
        amazonS3Client.deleteObject(bucket, fileName);
    }
}
