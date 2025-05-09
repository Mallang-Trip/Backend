package mallang_trip.backend.domain.party.service;

import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.CANNOT_FOUND_PARTY;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.party.dto.PartyBriefResponse;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyDibs;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import mallang_trip.backend.domain.party.repository.PartyDibsRepository;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyDibsService {

	private final CurrentUserService currentUserService;
	private final PartyDibsRepository partyDibsRepository;
	private final PartyRepository partyRepository;

	/**
	 * 파티 찜하기
	 * @param partyId 파티 id
	 */
	public void createPartyDibs(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		if (checkPartyDibs(party)) {
			return;
		}
		partyDibsRepository.save(PartyDibs.builder()
			.party(party)
			.user(currentUserService.getCurrentUser())
			.build());
	}

	/**
	 * 파티 찜 취소
	 */
	public void deletePartyDibs(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		if (!checkPartyDibs(party)) {
			return;
		}
		partyDibsRepository.deleteByPartyAndUser(party, currentUserService.getCurrentUser());
	}

	/**
	 * 현재 유저 찜한 파티 목록 조회
	 */
	public List<PartyBriefResponse> getMyPartyDibs() {
		return partyDibsRepository.findAllByUserOrderByUpdatedAtDesc(currentUserService.getCurrentUser())
			.stream()
			.map(dibs -> PartyBriefResponse.of(dibs.getParty()))
			.collect(Collectors.toList());
	}

	/**
	 * 현재 유저 파티 찜 여부 확인
	 */
	public boolean checkPartyDibs(Party party) {
		User user = currentUserService.getCurrentUser();
		if (user == null) {
			return false;
		}
		return partyDibsRepository.existsByPartyAndUser(party, user);
	}
}
