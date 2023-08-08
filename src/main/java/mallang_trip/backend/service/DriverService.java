package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.DriverStatus;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.constant.Week;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.course.CourseNameResponse;
import mallang_trip.backend.domain.dto.driver.ChangeBankAccountRequest;
import mallang_trip.backend.domain.dto.driver.ChangePriceRequest;
import mallang_trip.backend.domain.dto.driver.ChangeVehicleRequest;
import mallang_trip.backend.domain.dto.driver.DriverPriceRequest;
import mallang_trip.backend.domain.dto.driver.DriverPriceResponse;
import mallang_trip.backend.domain.dto.driver.DriverRegistrationRequest;
import mallang_trip.backend.domain.dto.driver.HolidayRequest;
import mallang_trip.backend.domain.dto.driver.HolidayResponse;
import mallang_trip.backend.domain.dto.driver.MyDriverProfileResponse;
import mallang_trip.backend.domain.entity.user.Driver;
import mallang_trip.backend.domain.entity.user.DriverPrice;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.user.DriverPriceRepository;
import mallang_trip.backend.repository.user.DriverRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverService {

    private final UserService userService;
    private final CourseService courseService;
    private final DriverRepository driverRepository;
    private final DriverPriceRepository driverPriceRepository;

    // 드라이버 전환 신청
    public void registerDriver(DriverRegistrationRequest request) {
        Driver driver = driverRepository.save(request.toDriver(userService.getCurrentUser()));
        for (DriverPriceRequest driverPriceRequest : request.getPrices()) {
            driverPriceRepository.save(driverPriceRequest.toDriverPrice(driver));
        }
    }

    // 드라이버 신청 정보 수정 or 재신청
    public void reapplyDriver(DriverRegistrationRequest request) {
        driverRepository.delete(getCurrentDriver());
        Driver driver = driverRepository.save(request.toDriver(userService.getCurrentUser()));
        for (DriverPriceRequest driverPriceRequest : request.getPrices()) {
            driverPriceRepository.save(driverPriceRequest.toDriverPrice(driver));
        }
    }

    // 승인 대기중인 드라이버 조회 (관리자 권한 필요)

    // 드라이버 수락 or 거절 (관리자 권한 필요)
    public void acceptDriverRegistration(Long driverId, Boolean accept) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new BaseException(Not_Found));
        if (accept) {
            driver.setStatus(DriverStatus.ACCEPTED);
            driver.getUser().setRole(Role.ROLE_DRIVER);
        } else {
            driver.setStatus(DriverStatus.REFUSED);
        }
    }

    // 정기 휴일 설정
    public void setWeeklyHoliday(HolidayRequest request) {
        Driver driver = getCurrentDriver();
        List<Week> holidays = new ArrayList<>();
        for (String holiday : request.getHolidays()) {
            holidays.add(Week.from(holiday));
        }
        driver.setWeeklyHoliday(holidays);
    }

    // 휴일 설정
    public void setHoliday(HolidayRequest request) {
        Driver driver = getCurrentDriver();
        List<LocalDate> holidays = new ArrayList<>();
        for (String holiday : request.getHolidays()) {
            holidays.add(LocalDate.parse(holiday));
        }
        driver.setHoliday(holidays);
    }

    // 정기 휴일 조회
    public HolidayResponse getWeeklyHoliday(Long id) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new BaseException(Not_Found));
        List<Week> weeks = driver.getWeeklyHoliday();
        List<String> holidays = new ArrayList<>();
        weeks.forEach(week -> holidays.add(week.toString()));
        return HolidayResponse.builder()
            .holidays(holidays)
            .build();
    }

    // 휴일 조회
    public HolidayResponse getHoliday(Long id) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new BaseException(Not_Found));
        List<LocalDate> dates = driver.getHoliday();
        List<String> holidays = dates.stream()
            .filter(date -> date.isAfter(LocalDate.now().minusDays(1)))
            .map(LocalDate::toString)
            .collect(Collectors.toList());

        return HolidayResponse.builder()
            .holidays(holidays)
            .build();
    }

    // 활동 지역 변경
    public void setRegion(String region) {
        Driver driver = getCurrentDriver();
        driver.setRegion(region);
    }

    // 계좌 수정
    public void changeBankAccount(ChangeBankAccountRequest request){
        Driver driver = getCurrentDriver();
        driver.setBank(request.getBank());
        driver.setAccountHolder(request.getAccountHolder());
        driver.setAccountNumber(request.getAccountNumber());
    }

    // 차종 변경
    public void changeVehicle(ChangeVehicleRequest request){
        Driver driver = getCurrentDriver();
        driver.setVehicleImg(request.getVehicleImg());
        driver.setVehicleModel(request.getVehicleModel());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setVehicleCapacity(request.getVehicleCapacity());
    }

    // 가격 설정
    public void setPrice(ChangePriceRequest request) {
        Driver driver = getCurrentDriver();
        driverPriceRepository.deleteAllByDriver(driver);
        for (DriverPriceRequest driverPriceRequest : request.getPrices()) {
            driverPriceRepository.save(driverPriceRequest.toDriverPrice(driver));
        }
    }

    // 드라이버 내 프로필 조회
    public MyDriverProfileResponse getMyDriverProfile() {
        Driver driver = getCurrentDriver();
        User user = userService.getCurrentUser();

        List<DriverPrice> prices = driverPriceRepository.findAllByDriver(driver);
        List<DriverPriceResponse> priceResponses = new ArrayList<>();
        for (DriverPrice driverPrice : prices) {
            priceResponses.add(DriverPriceResponse.of(driverPrice));
        }
        List<CourseNameResponse> courses = courseService.getCourseName(user);

        return MyDriverProfileResponse.builder()
            .userId(user.getId())
            .name(user.getName())
            .vehicleImg(driver.getVehicleImg())
            .vehicleModel(driver.getVehicleModel())
            .vehicleNumber(driver.getVehicleNumber())
            .bank(driver.getBank())
            .accountHolder(driver.getAccountHolder())
            .accountNumber(driver.getAccountNumber())
            .region(driver.getRegion())
            .weeklyHoliday(driver.getWeeklyHoliday())
            .prices(priceResponses)
            .courses(courses)
            .status(driver.getStatus())
            .build();
    }

    // 드라이버 정보 조회

    private Driver getCurrentDriver() {
        Driver driver = driverRepository.findById(userService.getCurrentUser().getId())
            .orElseThrow(() -> new BaseException(Not_Found));
        return driver;
    }
}
