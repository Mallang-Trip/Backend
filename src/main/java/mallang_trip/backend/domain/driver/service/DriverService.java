package mallang_trip.backend.domain.driver.service;

import static mallang_trip.backend.domain.driver.constant.DriverStatus.ACCEPTED;
import static mallang_trip.backend.domain.driver.constant.DriverStatus.CANCELED;
import static mallang_trip.backend.domain.driver.constant.DriverStatus.REFUSED;
import static mallang_trip.backend.domain.driver.constant.DriverStatus.WAITING;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.CANNOT_FOUND_DRIVER;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Not_Found;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.driver.constant.DriverStatus;
import mallang_trip.backend.domain.party.constant.PartyStatus;
import mallang_trip.backend.domain.user.constant.Role;
import mallang_trip.backend.domain.driver.repository.DriverPriceRepository;
import mallang_trip.backend.domain.driver.repository.DriverRepository;
import mallang_trip.backend.domain.global.io.BaseException;
import mallang_trip.backend.domain.driver.dto.ChangeDriverProfileRequest;
import mallang_trip.backend.domain.driver.dto.DriverBriefResponse;
import mallang_trip.backend.domain.driver.dto.DriverDetailsResponse;
import mallang_trip.backend.domain.driver.dto.DriverPriceRequest;
import mallang_trip.backend.domain.driver.dto.DriverPriceResponse;
import mallang_trip.backend.domain.driver.dto.DriverRegistrationRequest;
import mallang_trip.backend.domain.driver.dto.DriverRegistrationResponse;
import mallang_trip.backend.domain.driver.dto.MyDriverProfileResponse;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import mallang_trip.backend.domain.course.service.CourseService;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverService {

	private final UserService userService;
	private final DriverReviewService driverReviewService;
	private final CourseService courseService;
	private final DriverRepository driverRepository;
	private final DriverPriceRepository driverPriceRepository;
	private final PartyRepository partyRepository;

	/**
	 * 드라이버 전환 신청
	 */
	public void registerDriver(DriverRegistrationRequest request) {
		User currentUser = userService.getCurrentUser();
		// 이미 신청 정보가 있을 경우
		if (driverRepository.existsById(currentUser.getId())) {
			throw new BaseException(Conflict);
		}
		// 신청 정보 저장
		Driver driver = driverRepository.save(request.toDriver(currentUser));
		// 가격 정보 저장
		setPrice(driver, request.getPrices());
	}

	/**
	 * 드라이버 전환 신청 수정 or 재신청
	 */
	public void changeDriverRegistration(DriverRegistrationRequest request) {
		Driver driver = getCurrentDriver();
		// 이미 수락된 경우
		if (driver.getStatus().equals(ACCEPTED)) {
			throw new BaseException(Not_Found);
		}
		// 정보 수정
		driver.changeRegistration(request);
		setPrice(driver, request.getPrices());
	}

	/**
	 * 드라이버 전환 신청 취소
	 */
	public void cancelDriverRegistration() {
		Driver driver = getCurrentDriver();
		// 신청중이 아닐 경우
		if (!driver.getStatus().equals(WAITING)) {
			throw new BaseException(Not_Found);
		}
		// 신청 취소
		driver.changeStatus(CANCELED);
	}

	/**
	 * 현재 유저 드라이버 신청 상태 확인
	 */
	public DriverRegistrationResponse getMyDriverRegistration() {
		Driver driver = getCurrentDriver();
		return DriverRegistrationResponse.of(driver, getDriverPrice(driver));
	}

	/**
	 * (관리자) 승인 대기중인 드라이버 조회
	 */
	public List<DriverRegistrationResponse> getDriverRegistrationList() {
		List<DriverRegistrationResponse> responses =
			driverRepository.findAllByStatus(DriverStatus.WAITING)
				.stream()
				.map(driver -> DriverRegistrationResponse.of(driver, getDriverPrice(driver)))
				.collect(Collectors.toList());
		return responses;
	}

	/**
	 * (관리자) 드라이버 수락 or 거절
	 */
	public void acceptDriverRegistration(Long driverId, Boolean accept) {
		Driver driver = driverRepository.findById(driverId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
		// 이미 거절되거나 수락된 경우
		if (!driver.getStatus().equals(WAITING)) {
			throw new BaseException(Forbidden);
		}
		// 수락 or 거절 처리
		if (accept) {
			driver.changeStatus(ACCEPTED);
			driver.getUser().setRole(Role.ROLE_DRIVER);
		} else {
			driver.changeStatus(REFUSED);
		}
	}

	/**
	 * 드라이버 프로필 변경
	 */
	public void changeProfile(ChangeDriverProfileRequest request) {
		User user = userService.getCurrentUser();
		Driver driver = getCurrentDriver();

		driver.changeProfile(request);
		setPrice(driver, request.getPrices());
		user.setProfileImage(request.getProfileImg());
		user.setPhoneNumber(request.getPhoneNumber());
	}

	/**
	 * 가격 설정: 기존 가격 정보 삭제 후 재생성
	 */
	private void setPrice(Driver driver, List<DriverPriceRequest> requests) {
		driverPriceRepository.deleteAllByDriver(driver);
		requests.stream()
			.forEach(request -> driverPriceRepository.save(request.toDriverPrice(driver)));
	}

	/**
	 * 드라이버 내 프로필 조회
	 */
	public MyDriverProfileResponse getMyDriverProfile() {
		Driver driver = getCurrentDriver();
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
			.prices(getDriverPrice(driver))
			.courses(courseService.getDriversCourses(user))
			.status(driver.getStatus())
			.build();
	}

	/**
	 * 드라이버 프로필 조회
	 */
	public DriverDetailsResponse getDriverDetails(Long driverId) {
		Driver driver = driverRepository.findByIdAndStatus(driverId, ACCEPTED)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
		User user = driver.getUser();
		return DriverDetailsResponse.builder()
			.driverId(driver.getId())
			.name(user.getName())
			.profileImg(user.getProfileImage())
			.reservationCount(getReservationCount(driver))
			.avgRate(driverReviewService.getAvgRate(driver))
			.introduction(driver.getIntroduction())
			.region(driver.getRegion())
			.reviews(driverReviewService.get(driver))
			.courses(courseService.getDriversCourses(user))
			.build();
	}

	/**
	 * 예약 가능한 드라이버 조회
	 */
	public List<DriverBriefResponse> getPossibleDriver(String region, Integer headcount,
		String startDate) {
		return driverRepository.findAllByRegionAndStatus(region, ACCEPTED)
			.stream()
			.filter(driver -> driver.getVehicleCapacity() >= headcount)
			.filter(driver -> isDatePossible(driver, startDate))
			.map(DriverBriefResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 특정 날짜 예약가능 여부 판별
	 */
	public Boolean isDatePossible(Driver driver, String startDate) {
		LocalDate date = LocalDate.parse(startDate);
		if (partyRepository.existsValidPartyByDriverAndStartDate(driver.getId(), startDate) // 진행중인 파티가 있는지 CHECK
			|| driver.getHoliday().contains(date) || // 휴일인지 CHECK
			driver.getWeeklyHoliday().contains(date.getDayOfWeek())) { // 주휴일인지 CHECK
			return false;
		}
		return true;
	}

	/**
	 * 현재 드라이버 조회
	 */
	public Driver getCurrentDriver() {
		Driver driver = driverRepository.findByUser(userService.getCurrentUser())
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
		return driver;
	}

	/**
	 * 드라이버 가격 조회
	 */
	private List<DriverPriceResponse> getDriverPrice(Driver driver) {
		return driverPriceRepository.findAllByDriver((driver)).stream()
			.map(DriverPriceResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 드라이버의 FINISHED PARTY 개수 조회
	 */
	private Integer getReservationCount(Driver driver){
		return partyRepository.countByDriverAndStatus(driver, PartyStatus.FINISHED);
	}
}
