package mallang_trip.backend.service;

import static mallang_trip.backend.constant.DriverStatus.ACCEPTED;
import static mallang_trip.backend.constant.DriverStatus.REFUSED_OR_CANCELED;
import static mallang_trip.backend.constant.DriverStatus.WAITING;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.DriverStatus;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.driver.ChangeDriverProfileRequest;
import mallang_trip.backend.domain.dto.driver.DriverBriefResponse;
import mallang_trip.backend.domain.dto.driver.DriverDetailsResponse;
import mallang_trip.backend.domain.dto.driver.DriverPriceRequest;
import mallang_trip.backend.domain.dto.driver.DriverPriceResponse;
import mallang_trip.backend.domain.dto.driver.DriverRegistrationRequest;
import mallang_trip.backend.domain.dto.driver.DriverRegistrationResponse;
import mallang_trip.backend.domain.dto.driver.DriverReviewRequest;
import mallang_trip.backend.domain.dto.driver.DriverReviewResponse;
import mallang_trip.backend.domain.dto.driver.MyDriverProfileResponse;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.driver.DriverReview;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.driver.DriverPriceRepository;
import mallang_trip.backend.repository.driver.DriverRepository;
import mallang_trip.backend.repository.driver.DriverReviewRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverService {

    private final UserService userService;
    private final CourseService courseService;
    private final DriverRepository driverRepository;
    private final DriverPriceRepository driverPriceRepository;
    private final DriverReviewRepository driverReviewRepository;
    private final PartyRepository partyRepository;

    // 드라이버 전환 신청
    public void registerDriver(DriverRegistrationRequest request) {
        // 이미 신청 정보가 있을 경우
        if(driverRepository.existsById(userService.getCurrentUser().getId())){
            throw new BaseException(Conflict);
        }
        Driver driver = driverRepository.save(request.toDriver(userService.getCurrentUser()));
        request.getPrices().forEach(driverPriceRequest ->
            driverPriceRepository.save(driverPriceRequest.toDriverPrice(driver))
        );
    }

    // 드라이버 전환 신청 수정 or 재신청
    public void changeDriverRegistration(DriverRegistrationRequest request) {
        Driver driver = getCurrentDriver();
        // 이미 수락된 경우
        if(driver.getStatus().equals(ACCEPTED)){
            throw new BaseException(Not_Found);
        }
        // 정보 수정
        driver.setVehicleModel(request.getVehicleModel());
        driver.setVehicleCapacity(request.getVehicleCapacity());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setVehicleImg(request.getVehicleImg());
        driver.setRegion(request.getRegion());
        driver.setBank(request.getBank());
        driver.setAccountHolder(request.getAccountHolder());
        driver.setAccountNumber(request.getAccountNumber());
        driver.setDriverLicenceImg(request.getDriverLicenceImg());
        driver.setTaxiLicenceImg(request.getTaxiLicenceImg());
        driver.setInsuranceLicenceImg(request.getInsuranceLicenceImg());
        driver.setIntroduction(request.getIntroduction());
        driver.setStatus(WAITING);
        setPrice(driver, request.getPrices());
    }

    // 드라이버 전환 신청 취소
    public void cancelDriverRegistration() {
        Driver driver = getCurrentDriver();
        // 신청중이 아닐 경우
        if(!driver.getStatus().equals(WAITING)){
            throw new BaseException(Not_Found);
        }
        // 신청 취소
        driver.setStatus(REFUSED_OR_CANCELED);
    }

    // 내 드라이버 신청 상태 확인
    public DriverRegistrationResponse getMyDriverRegistration() {
        Driver driver = getCurrentDriver();
        return DriverRegistrationResponse.of(driver, getDriverPrice(driver));
    }

    // 승인 대기중인 드라이버 조회 (관리자)
    public List<DriverRegistrationResponse> getDriverRegistrationList() {
        List<DriverRegistrationResponse> responses = driverRepository
            .findAllByStatus(DriverStatus.WAITING)
            .stream()
            .map(driver -> DriverRegistrationResponse.of(driver, getDriverPrice(driver)))
            .collect(Collectors.toList());
        return responses;
    }

    // 드라이버 수락 or 거절 (관리자)
    public void acceptDriverRegistration(Long driverId, Boolean accept) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 이미 거절되거나 수락된 경우
        if(!driver.getStatus().equals(WAITING)){
            throw new BaseException(Not_Found);
        }
        if (accept) { // 수락
            driver.setStatus(ACCEPTED);
            driver.getUser().setRole(Role.ROLE_DRIVER);
        } else { // 거절
           driver.setStatus(REFUSED_OR_CANCELED);
        }
    }

    // 드라이버 프로필 변경
    public void changeProfile(ChangeDriverProfileRequest request) {
        User user = userService.getCurrentUser();
        Driver driver = getCurrentDriver();

        user.setProfileImage(request.getProfileImg());
        driver.setRegion(request.getRegion());
        setWeeklyHoliday(driver, request.getWeeklyHolidays());
        setHoliday(driver, request.getHolidays());
        user.setPhoneNumber(request.getPhoneNumber());
        driver.setBank(request.getBank());
        driver.setAccountHolder(request.getAccountHolder());
        driver.setAccountNumber(request.getAccountNumber());
        setPrice(driver, request.getPrices());
        driver.setVehicleImg(request.getVehicleImg());
        driver.setVehicleModel(request.getVehicleModel());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setVehicleCapacity(request.getVehicleCapacity());
        driver.setIntroduction(request.getIntroduction());
    }

    // 정기 휴일 설정
    private void setWeeklyHoliday(Driver driver, List<String> holidays) {
        driver.setWeeklyHoliday(holidays.stream()
            .map(DayOfWeek::valueOf)
            .collect(Collectors.toList()));
    }

    // 휴일 설정
    private void setHoliday(Driver driver, List<String> holidays) {
        driver.setHoliday(holidays.stream()
            .map(LocalDate::parse)
            .collect(Collectors.toList()));
    }

    // 가격 설정
    private void setPrice(Driver driver, List<DriverPriceRequest> requests) {
        driverPriceRepository.deleteAllByDriver(driver);
        requests.stream()
            .forEach(request -> driverPriceRepository.save(request.toDriverPrice(driver)));
    }

    // 드라이버 내 프로필 조회
    public MyDriverProfileResponse getMyDriverProfile() {
        Driver driver = getCurrentDriver();
        User user = userService.getCurrentUser();

        return MyDriverProfileResponse.builder()
            .userId(user.getId())
            .name(user.getName())
            .profileImg(user.getProfileImage())
            .region(driver.getRegion())
            .weeklyHoliday(driver.getWeeklyHoliday())
            .holidays(driver.getHoliday())
            .vehicleImg(driver.getVehicleImg())
            .vehicleModel(driver.getVehicleModel())
            .vehicleNumber(driver.getVehicleNumber())
            .vehicleCapacity(driver.getVehicleCapacity())
            .bank(driver.getBank())
            .accountHolder(driver.getAccountHolder())
            .accountNumber(driver.getAccountNumber())
            .phoneNumber(user.getPhoneNumber())
            .introduction(driver.getIntroduction())
            .prices(getDriverPrice(driver))
            .courses(courseService.getCourseNames(user))
            .status(driver.getStatus())
            .build();
    }

    // 드라이버 프로필 조회
    // reservation count 추가 필요
    public DriverDetailsResponse getDriverDetails(Long driverId) {
        Driver driver = driverRepository.findByIdAndDeletedAndStatus(driverId, false, ACCEPTED);
        if (driver == null) {
            throw new BaseException(Not_Found);
        }
        User user = driver.getUser();
        List<DriverReviewResponse> reviewResponses = driverReviewRepository
            .findAllByDriverOrderByUpdatedAtDesc(driver)
            .stream()
            .map(DriverReviewResponse::of)
            .collect(Collectors.toList());

        return DriverDetailsResponse.builder()
            .driverId(driver.getId())
            .name(user.getName())
            .profileImg(user.getProfileImage())
            .reservationCount(0)
            .avgRate(driverReviewRepository.getAvgRating(driver))
            .introduction(driver.getIntroduction())
            .region(driver.getRegion())
            .reviews(reviewResponses)
            .courses(courseService.getCourseNames(user))
            .build();
    }

    // 드라이버 리뷰 등록
    public void createDriverReview(Long driverId, DriverReviewRequest request) {
        Driver driver = driverRepository.findByIdAndDeletedAndStatus(driverId, false, ACCEPTED);
        if (driver == null) {
            throw new BaseException(Not_Found);
        }
        User user = userService.getCurrentUser();
        // 1인 1리뷰 CHECK
        if (driverReviewRepository.existsByDriverAndUser(driver, user)) {
            throw new BaseException(Conflict);
        }
        driverReviewRepository.save(DriverReview.builder()
            .driver(driver)
            .user(user)
            .rate(request.getRate())
            .content(request.getContent())
            .images(request.getImages())
            .build());
    }

    // 드라이버 리뷰 수정
    public void changeDriverReview(Long reviewId, DriverReviewRequest request) {
        DriverReview review = driverReviewRepository.findById(reviewId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 작성자가 아닐 경우
        if (!userService.getCurrentUser().equals(review.getUser())) {
            throw new BaseException(Unauthorized);
        }
        review.setRate(request.getRate());
        review.setContent(request.getContent());
        review.setImages(request.getImages());
    }

    // 드라이버 리뷰 삭제
    public void deleteDriverReview(Long reviewId) {
        DriverReview review = driverReviewRepository.findById(reviewId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 작성자가 아닐 경우
        if (!userService.getCurrentUser().equals(review.getUser())) {
            throw new BaseException(Unauthorized);
        }
        driverReviewRepository.delete(review);
    }

    // 가능한 드라이버 조회
    public List<DriverBriefResponse> getPossibleDriver(String region, Integer headcount,
        String startDate) {
        return driverRepository.findAllByRegionAndDeletedAndStatus(region, false, ACCEPTED)
            .stream()
            .filter(driver -> driver.getVehicleCapacity() >= headcount)
            .filter(driver -> isDatePossible(driver, startDate))
            .map(DriverBriefResponse::of)
            .collect(Collectors.toList());
    }

    private Boolean isDatePossible(Driver driver, String startDate) {
        LocalDate date = LocalDate.parse(startDate);
        // 진행중인 파티가 있는지 CHECK
        if (!partyRepository.findValidPartyByDriverAndStartDate(driver.getId(), startDate)
            .isEmpty()) {
            return false;
        }
        // 휴일 CHECK
        if (driver.getHoliday().contains(date)) {
            return false;
        }
        // 주휴일 CHECK
        if (driver.getWeeklyHoliday().contains(date.getDayOfWeek())) {
            return false;
        }
        return true;
    }

    private Driver getCurrentDriver() {
        Driver driver = driverRepository.findById(userService.getCurrentUser().getId())
            .orElseThrow(() -> new BaseException(Not_Found));
        return driver;
    }

    private List<DriverPriceResponse> getDriverPrice(Driver driver) {
        return driverPriceRepository.findAllByDriver((driver)).stream()
            .map(DriverPriceResponse::of)
            .collect(Collectors.toList());
    }
}
