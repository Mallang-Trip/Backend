package mallang_trip.backend.service.party;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dto.party.PartyBriefResponse;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyHistory;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.party.PartyHistoryRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import mallang_trip.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyHistoryService {

	private final UserService userService;
	private final PartyHistoryRepository partyHistoryRepository;

	/**
	 * 최근 본 파티 저장. 처음 본 파티면 party_history 생성, 이미 본 파티면 updated_at 현재 시간으로 update.
	 */
	public void createPartyHistory(Party party) {
		User user = userService.getCurrentUser();
		PartyHistory history = partyHistoryRepository.findByPartyAndUser(party, user).orElse(null);
		if (history == null) {
			partyHistoryRepository.save(PartyHistory.builder()
				.user(user)
				.party(party)
				.build());
		} else {
			history.setUpdatedAt(LocalDateTime.now());
		}
	}

	/**
	 * 최근 본 파티 조회
	 */
	public List<PartyBriefResponse> getPartyHistory() {
		return partyHistoryRepository.findByUserOrderByUpdatedAtDesc(userService.getCurrentUser())
			.stream()
			.map(partyHistory -> partyHistory.getParty())
			.map(party -> PartyBriefResponse.of(party))
			.collect(Collectors.toList());
	}
}
