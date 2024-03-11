package mallang_trip.backend.domain.user.service;


import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.ONGOING_PARTY_EXISTS;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.party.constant.ProposalStatus;
import mallang_trip.backend.domain.party.constant.ProposalType;
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
	private final PartyProposalRepository partyProposalRepository;

	public static final String UNKNOWN_USER = "알 수 없음";
	public static final String EMPTY_STRING = "";

	/**
	 * 회원탈퇴
	 */
	public void withdrawal() {
		User user = currentUserService.getCurrentUser();
		if (isOngoingPartyExists(user)) {
			throw new BaseException(ONGOING_PARTY_EXISTS);
		}
		// TODO: 대금 지급이 이루어지지 않았을 경우 exception handling
		// TODO: 결제 정보 저장
		deletePersonalInformation(user);
		chatService.leaveAllChat(user);
		user.setRefreshToken(null);
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
