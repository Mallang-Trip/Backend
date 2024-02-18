package mallang_trip.backend.domains.user.service;

import static mallang_trip.backend.domains.global.io.BaseResponseStatus.ONGOING_PARTY_EXISTS;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.party.constant.ProposalStatus;
import mallang_trip.backend.domains.party.constant.ProposalType;
import mallang_trip.backend.domains.global.io.BaseException;
import mallang_trip.backend.domains.user.entity.User;
import mallang_trip.backend.domains.party.repository.PartyProposalRepository;
import mallang_trip.backend.domains.party.repository.PartyRepository;
import mallang_trip.backend.domains.chat.service.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWithdrawalService {

	private final UserService userService;
	private final ChatService chatService;
	private final PartyRepository partyRepository;
	private final PartyProposalRepository partyProposalRepository;

	public static final String UNKNOWN_USER = "알 수 없음";
	public static final String EMPTY_STRING = "";

	/**
	 * 회원탈퇴
	 */
	public void withdrawal() {
		User user = userService.getCurrentUser();
		if (isOngoingPartyExists(user)) {
			throw new BaseException(ONGOING_PARTY_EXISTS);
		}
		// TODO: 대금 지급이 이루어지지 않았을 경우 exception handling
		// TODO: 결제 정보 저장
		deletePersonalInformation(user);
		chatService.leaveAllChat(user);
	}

	/**
	 * 진행 중이거나 가입 신청중인 파티가 있는지 유무
	 */
	private Boolean isOngoingPartyExists(User user) {
		if (partyRepository.isOngoingPartyExists(user.getId())
			|| partyProposalRepository.existsByProposerAndTypeAndStatus(user,
			ProposalType.JOIN_WITH_COURSE_CHANGE, ProposalStatus.WAITING)) {
			return true;
		} else return false;
	}

	private void deletePersonalInformation(User user){
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
		user.setDeleted(true);
	}

}
