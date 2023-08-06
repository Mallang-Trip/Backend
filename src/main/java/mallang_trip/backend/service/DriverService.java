package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.config.s3.AwsS3Uploader;
import mallang_trip.backend.constant.DriverStatus;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.User.DriverRegistrationResponse;
import mallang_trip.backend.domain.dto.User.DriverRegistrationRequest;
import mallang_trip.backend.domain.entity.user.Driver;
import mallang_trip.backend.repository.user.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverService {

    private final UserService userService;
    private final DriverRepository driverRepository;
    private final AwsS3Uploader awsS3Uploader;

    // 드라이버 전환 신청
    public void registerDriver(MultipartFile file, DriverRegistrationRequest request) {
        driverRepository.save(Driver.builder()
            .driver(userService.getCurrentUser())
            .licenceImg(awsS3Uploader.upload(file, "/license"))
            .vehicleType(request.getVehicleType())
            .vehicleNumber(request.getVehicleNumber())
            .vehicleCapacity(request.getVehicleCapacity())
            .regions(request.getRegions())
            .build());
    }

    // 드라이버 신청 대기 리스트 조회
    public List<DriverRegistrationResponse> getWaitingList(){
        List<Driver> list = driverRepository.findAllByStatus(
            DriverStatus.WAITING);
        List<DriverRegistrationResponse> response = new ArrayList<>();
        for(Driver driver : list){
            response.add(DriverRegistrationResponse.of(driver));
        }
        return response;
    }

    // 드라이버 수락 or 거절
    public void acceptDriverRegistration(Long registrationId, Boolean accept){
        Driver registration = driverRepository.findById(registrationId)
            .orElseThrow(() -> new BaseException(Not_Found));
        if(accept){
            registration.setStatus(DriverStatus.ACCEPTED);
        } else {
            registration.setStatus(DriverStatus.REFUSED);
        }
    }

    // 내 신청 상태 확인하기
}
