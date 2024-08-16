package mallang_trip.backend.domain.user.service;


import static mallang_trip.backend.domain.party.constant.DriverPenaltyStatus.PENALTY_EXISTS;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.ONGOING_PARTY_EXISTS;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_DRIVER;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_USER;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.PAYMENT_FAILED_EXISTS;
import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.driver.constant.DriverStatus;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.driver.service.DriverService;
import mallang_trip.backend.domain.party.constant.ProposalStatus;
import mallang_trip.backend.domain.party.constant.ProposalType;
import mallang_trip.backend.domain.reservation.repository.ReservationRepository;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyProposalRepository;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import mallang_trip.backend.domain.chat.service.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWithdrawalService {

	private final CurrentUserService currentUserService;
	private final ChatService chatService;
	private final PartyRepository partyRepository;
	private final DriverService driverService;
	private final PartyProposalRepository partyProposalRepository;
	private final ReservationRepository reservationRepository;

	public static final String UNKNOWN_USER = "알 수 없음";
	public static final String EMPTY_STRING = "";

	/**
	 * 회원탈퇴
	 */
	public void withdrawal() {
		User user = currentUserService.getCurrentUser();
		if (user.getRole().equals(ROLE_USER)) {
			withdrawalByUser(user);
		} else if (user.getRole().equals(ROLE_DRIVER)) {
			withdrawalByDriver();
		} else {
			throw new BaseException(Bad_Request);
		}
	}

	/**
	 * 일반 유저 회원탈퇴
	 */
	public void withdrawalByUser(User user) {
		// 진행중인 파티가 존재할 경우
		if (isOngoingPartyExists(user)) {
			throw new BaseException(ONGOING_PARTY_EXISTS);
		}
		// 미납금 또는 위약금이 존재할 경우
		if (reservationRepository.isPaymentFailedExistsByUser(user.getId())
			|| reservationRepository.isPenaltyExistsByUser(user.getId())) {
			throw new BaseException(PAYMENT_FAILED_EXISTS);
		}
		deletePersonalInformation(user);
		chatService.leaveAllChat(user);
	}

	/**
	 * 드라이버 회원탈퇴
	 */
	public void withdrawalByDriver() {
		Driver driver = driverService.getCurrentDriver();
		// 진행중인 파티가 존재할 경우
		if (partyRepository.isOngoingPartyExistsByDriver(driver.getId())) {
			throw new BaseException(ONGOING_PARTY_EXISTS);
		}
		// 미납 위약금이 존재할 경우
		if (partyRepository.existsByDriverAndDriverPenaltyStatus(driver, PENALTY_EXISTS)) {
			throw new BaseException(PAYMENT_FAILED_EXISTS);
		}
		driver.changeStatus(DriverStatus.CANCELED);
		deletePersonalInformation(driver.getUser());
		chatService.leaveAllChat(driver.getUser());
	}

	/**
	 * 진행 중이거나 가입 신청중인 파티가 있는지 유무
	 */
	private boolean isOngoingPartyExists(User user) {
		return partyRepository.isOngoingPartyExists(user.getId())
			|| partyProposalRepository.existsByProposerAndTypeAndStatus(user,
			ProposalType.JOIN_WITH_COURSE_CHANGE, ProposalStatus.WAITING);
	}

	/**
	 * 개인정보 삭제
	 */
	private void deletePersonalInformation(User user) {
		//user.setBirthDay(null);
		user.setEmail(EMPTY_STRING);
		user.setIntroduction(null);
		user.setLoginId(EMPTY_STRING);
		user.setName(UNKNOWN_USER);
		user.setNickname(UNKNOWN_USER);
		user.setPassword(EMPTY_STRING);
		user.setPhoneNumber(EMPTY_STRING);
		user.setProfileImage(null);
		user.setRefreshToken(null);
		user.setDi(null);
		user.setDeleted(true);
	}

}
