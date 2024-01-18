package mallang_trip.backend.service.party;

import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PARTY;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.party.PartyBriefResponse;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyDibs;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.party.PartyDibsRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import mallang_trip.backend.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyDibsService {

	private final UserService userService;
	private final PartyDibsRepository partyDibsRepository;
	private final PartyRepository partyRepository;

	/**
	 * 파티 찜하기
	 */
	public void createPartyDibs(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		if (checkPartyDibs(party)) {
			return;
		}
		partyDibsRepository.save(PartyDibs.builder()
			.party(party)
			.user(userService.getCurrentUser())
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
		partyDibsRepository.deleteByPartyAndUser(party, userService.getCurrentUser());
	}

	/**
	 * 현재 유저 찜한 파티 목록 조회
	 */
	public List<PartyBriefResponse> getMyPartyDibs() {
		return partyDibsRepository.findAllByUserOrderByUpdatedAtDesc(userService.getCurrentUser())
			.stream()
			.map(dibs -> PartyBriefResponse.of(dibs.getParty()))
			.collect(Collectors.toList());
	}

	/**
	 * 현재 유저 파티 찜 여부 확인
	 */
	public boolean checkPartyDibs(Party party) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return false;
		}
		return partyDibsRepository.existsByPartyAndUser(party, user);
	}
}
