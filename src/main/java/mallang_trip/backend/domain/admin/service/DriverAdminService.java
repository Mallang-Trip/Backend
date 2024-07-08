package mallang_trip.backend.domain.admin.service;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.course.dto.CourseDetailsResponse;
import mallang_trip.backend.domain.course.dto.CourseRequest;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.course.repository.CourseDayRepository;
import mallang_trip.backend.domain.course.repository.CourseRepository;
import mallang_trip.backend.domain.course.service.CourseService;
import mallang_trip.backend.domain.driver.dto.ChangeDriverProfileRequest;
import mallang_trip.backend.domain.driver.dto.DriverRegistrationResponse;
import mallang_trip.backend.domain.driver.dto.MyDriverProfileResponse;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.driver.entity.DriverPrice;
import mallang_trip.backend.domain.driver.repository.DriverPriceRepository;
import mallang_trip.backend.domain.driver.repository.DriverRepository;
import mallang_trip.backend.domain.driver.service.DriverService;
import mallang_trip.backend.domain.party.entity.PartyRegion;
import mallang_trip.backend.domain.party.repository.PartyRegionRepository;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.repository.UserRepository;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static mallang_trip.backend.domain.driver.constant.DriverStatus.ACCEPTED;
import static mallang_trip.backend.domain.driver.constant.DriverStatus.CANCELED;
import static mallang_trip.backend.domain.driver.exception.DriverExceptionStatus.CANNOT_FOUND_DRIVER;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.REGION_NOT_FOUND;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_DRIVER;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_USER;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.global.io.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverAdminService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DriverService driverService;
    private final DriverPriceRepository driverPriceRepository;
    private final PartyRegionRepository partyRegionRepository;
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final CourseDayRepository courseDayRepository;



    /**
     * (관리자) 모든 드라이버 목록 조회
     */
    public List<DriverRegistrationResponse> getDriverList() {
        return driverRepository.findAll().stream()
                .map(d -> DriverRegistrationResponse.of(d, driverService.getDriverPrice(d)))
                .collect(Collectors.toList());
    }

    /**
     * (관리자) 드라이버 신청
     */
    public void registerDriver(Long userId, String region) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        if (driverRepository.existsById(user.getId())) {
            throw new BaseException(Conflict);
        }

        PartyRegion partyRegion = partyRegionRepository.findByRegion(region)
                .orElseThrow(() -> new BaseException(REGION_NOT_FOUND));

        Driver driver = driverRepository.save(Driver.builder()
                .user(user)
                .region(region)
                .vehicleImg("https://mallang-trip-db.s3.ap-northeast-2.amazonaws.com/profile/ed7ac840-86c5-4c14-a5cd-8d8761e080ff1.png")
                .driverLicenceImg("https://mallang-trip-db.s3.ap-northeast-2.amazonaws.com/profile/ed7ac840-86c5-4c14-a5cd-8d8761e080ff1.png")
                .taxiLicenceImg("https://mallang-trip-db.s3.ap-northeast-2.amazonaws.com/profile/ed7ac840-86c5-4c14-a5cd-8d8761e080ff1.png")
                .insuranceLicenceImg("https://mallang-trip-db.s3.ap-northeast-2.amazonaws.com/profile/ed7ac840-86c5-4c14-a5cd-8d8761e080ff1.png")
                .vehicleModel("차량 모델명")
                .vehicleNumber("차량 번호판")
                .vehicleCapacity(4)
                .bank("은행명")
                .accountHolder(user.getName())
                .accountNumber("123412341234")
                .introduction(user.getIntroduction())
                .build());

        List<Integer> hours = List.of(3, 5, 6, 7, 8); // Example hours 3,5,6,7,8
        driverPriceRepository.deleteAllByDriver(driver);
        hours.stream().forEach(h -> {
            driverPriceRepository.save(DriverPrice.builder()
                    .driver(driver)
                    .hours(h)
                    .price(h * 20000) // 시간 당 20000원
                    .build());
        });

        driver.changeStatus(ACCEPTED);
        user.setRole(ROLE_DRIVER);
        user.setRefreshToken(null);

        partyRegion.addCount();

    }

    /**
     * (관리자) 드라이버 신청 취소
     */
    public void cancelDriverRegistration(Long driverId) {
        Driver driver = driverRepository.findByIdAndStatus(driverId, ACCEPTED)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
        PartyRegion partyRegion = partyRegionRepository.findByRegion(driver.getRegion())
                .orElseThrow(() -> new BaseException(REGION_NOT_FOUND));

        driver.changeStatus(CANCELED);
        driver.getUser().setRole(ROLE_USER);
        driver.getUser().setRefreshToken(null);

        driverRepository.delete(driver);
        driverPriceRepository.deleteAllByDriver(driver);

        partyRegion.subCount();
    }

    /**
     * (관리자) 드라이버 프로필 조회
     */
    public MyDriverProfileResponse getDriverProfile(Long driverId) {
        Driver driver = driverRepository.findByIdAndStatus(driverId, ACCEPTED)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
        User user = driver.getUser();

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
                .prices(driverService.getDriverPrice(driver))
                .courses(courseService.getDriversCourses(user))
                .status(driver.getStatus())
                .build();
    }

    /**
     * (관리자) 드라이버 프로필 수정
     */
    public void changeDriverProfile(Long driverId, ChangeDriverProfileRequest request) {
        Driver driver = driverRepository.findByIdAndStatus(driverId, ACCEPTED)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
        User user = driver.getUser();

        driver.changeProfile(request);
        driverService.setPrice(driver, request.getPrices());
        user.setProfileImage(request.getProfileImg());
        user.setPhoneNumber(request.getPhoneNumber());
    }

    /**
     * (관리자) 드라이버 코스 생성
     */
    public Course createCourse(Long driverId, CourseRequest request) {
        Driver driver = driverRepository.findByIdAndStatus(driverId, ACCEPTED)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));

        Course course = courseRepository.save(Course.builder()
                .owner(driver.getUser())
                .images(request.getImages())
                .totalDays(request.getTotalDays())
                .name(request.getName())
                .capacity(request.getCapacity())
                .totalPrice(request.getTotalPrice())
                .build());
        courseService.createCourseDays(request.getDays(), course);

        return course;
    }

    /**
     * (관리자) 드라이버 코스 조회
     */
    public CourseDetailsResponse getCourseDetails(Long driverId, Long courseId) {
        Driver driver = driverRepository.findByIdAndStatus(driverId, ACCEPTED)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BaseException(Not_Found));

        return courseService.getCourseDetails(course);
    }

    /**
     * (관리자) 드라이버 코스 수정
     */
    public void changeCourse(Long driverId, Long courseId, CourseRequest request) {
        Driver driver = driverRepository.findByIdAndStatus(driverId, ACCEPTED)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BaseException(Not_Found));

        if (!course.getOwner().equals(driver.getUser())) {
            throw new BaseException(Forbidden);
        }
        course.modify(request);
        courseDayRepository.deleteAllByCourse(course);
        courseService.createCourseDays(request.getDays(), course);
    }

    /**
     * (관리자) 드라이버 코스 삭제
     */
    public void deleteCourse(Long driverId, Long courseId) {
        Driver driver = driverRepository.findByIdAndStatus(driverId, ACCEPTED)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BaseException(Not_Found));

        if (!course.getOwner().equals(driver.getUser())) {
            throw new BaseException(Forbidden);
        }
        courseRepository.delete(course);
    }
}
