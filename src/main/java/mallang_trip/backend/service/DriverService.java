package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.DriverStatus;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.driver.ChangeBankAccountRequest;
import mallang_trip.backend.domain.dto.driver.ChangePriceRequest;
import mallang_trip.backend.domain.dto.driver.ChangeVehicleRequest;
import mallang_trip.backend.domain.dto.driver.DriverBriefResponse;
import mallang_trip.backend.domain.dto.driver.DriverDetailsResponse;
import mallang_trip.backend.domain.dto.driver.DriverPriceRequest;
import mallang_trip.backend.domain.dto.driver.DriverPriceResponse;
import mallang_trip.backend.domain.dto.driver.DriverRegistrationRequest;
import mallang_trip.backend.domain.dto.driver.DriverRegistrationResponse;
import mallang_trip.backend.domain.dto.driver.DriverReviewRequest;
import mallang_trip.backend.domain.dto.driver.DriverReviewResponse;
import mallang_trip.backend.domain.dto.driver.HolidayRequest;
import mallang_trip.backend.domain.dto.driver.HolidayResponse;
import mallang_trip.backend.domain.dto.driver.MyDriverProfileResponse;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.driver.DriverPrice;
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
		Driver driver = driverRepository.save(request.toDriver(userService.getCurrentUser()));
		request.getPrices().forEach(driverPriceRequest ->
			driverPriceRepository.save(driverPriceRequest.toDriverPrice(driver))
		);
	}

	// 내 드라이버 신청 상태 확인
	public DriverRegistrationResponse getMyDriverRegistration() {
		Driver driver = getCurrentDriver();
		return DriverRegistrationResponse.of(driver, getDriverPrice(driver));
	}

	// 드라이버 신청 정보 수정 or 재신청
	public void reapplyDriver(DriverRegistrationRequest request) {
		driverRepository.delete(getCurrentDriver());
		Driver driver = driverRepository.save(request.toDriver(userService.getCurrentUser()));
		request.getPrices().forEach(driverPriceRequest ->
			driverPriceRepository.save(driverPriceRequest.toDriverPrice(driver))
		);
	}

	// 승인 대기중인 드라이버 조회 (관리자 권한 필요)
	public List<DriverRegistrationResponse> getDriverRegistrationList() {
		List<DriverRegistrationResponse> responses = driverRepository
			.findAllByStatus(DriverStatus.WAITING)
			.stream()
			.map(driver -> DriverRegistrationResponse.of(driver, getDriverPrice(driver)))
			.collect(Collectors.toList());
		return responses;
	}

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
		List<DayOfWeek> holidays = request.getHolidays().stream()
			.map(DayOfWeek::valueOf)
			.collect(Collectors.toList());
		driver.setWeeklyHoliday(holidays);
	}

	// 휴일 설정
	public void setHoliday(HolidayRequest request) {
		Driver driver = getCurrentDriver();
		List<LocalDate> holidays = request.getHolidays()
			.stream()
			.map(LocalDate::parse)
			.collect(Collectors.toList());
		driver.setHoliday(holidays);
	}

	// 정기 휴일 조회
	public HolidayResponse getWeeklyHoliday(Long id) {
		Driver driver = driverRepository.findById(id)
			.orElseThrow(() -> new BaseException(Not_Found));
		return HolidayResponse.ofWeek(driver.getWeeklyHoliday());
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
	public void changeBankAccount(ChangeBankAccountRequest request) {
		Driver driver = getCurrentDriver();
		driver.setBank(request.getBank());
		driver.setAccountHolder(request.getAccountHolder());
		driver.setAccountNumber(request.getAccountNumber());
	}

	// 차종 변경
	public void changeVehicle(ChangeVehicleRequest request) {
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
			.prices(getDriverPrice(driver))
			.courses(courseService.getCourseName(user))
			.status(driver.getStatus())
			.build();
	}

	// 드라이버 리뷰 등록
	public void createDriverReview(Long driverId, DriverReviewRequest request) {
		Driver driver = driverRepository.findById(driverId)
			.orElseThrow(() -> new BaseException(Not_Found));
		User user = userService.getCurrentUser();
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
		if (userService.getCurrentUser().getId() != review.getUser().getId()) {
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
		if (userService.getCurrentUser().getId() != review.getUser().getId()) {
			throw new BaseException(Unauthorized);
		}
		driverReviewRepository.delete(review);
	}

	// 드라이버 정보 조회
	// reservation count 추가 필요
	public DriverDetailsResponse getDriverDetails(Long driverId) {
		Driver driver = driverRepository.findById(driverId)
			.orElseThrow(() -> new BaseException(Not_Found));
		User user = driver.getUser();
		List<DriverReviewResponse> reviewResponses = driverReviewRepository
			.findAllByDriver(driver)
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
			.courses(courseService.getCourseName(user))
			.build();
	}

	// 지역으로 드라이버 검색
	public List<DriverBriefResponse> getDriversByRegion(String region) {
		List<DriverBriefResponse> responses = driverRepository
			.findAllByRegion(region)
			.stream()
			.map(DriverBriefResponse::of)
			.collect(Collectors.toList());

		return responses;
	}

	// 가능한 드라이버 조회
	public List<DriverBriefResponse> getPossibleDriver(String region, Integer headcount, String startDate) {
		return driverRepository.findAllByRegion(region).stream()
			.filter(driver -> driver.getVehicleCapacity() >= headcount)
			.filter(driver -> isDatePossible(driver, startDate))
			.map(DriverBriefResponse::of)
			.collect(Collectors.toList());
	}
	private Boolean isDatePossible(Driver driver, String startDate){
		LocalDate date = LocalDate.parse(startDate);
		if(partyRepository.findValidPartyByDriver(driver.getId()).contains(date)){
			return false;
		}
		if(driver.getHoliday().contains(date)){
			return false;
		}
		if(driver.getWeeklyHoliday().contains(date.getDayOfWeek())){
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
		List<DriverPrice> prices = driverPriceRepository.findAllByDriver(driver);
		List<DriverPriceResponse> priceResponses = new ArrayList<>();
		for (DriverPrice driverPrice : prices) {
			priceResponses.add(DriverPriceResponse.of(driverPrice));
		}
		return priceResponses;
	}
}
