package mallang_trip.backend.domain.party.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.party.dto.PartyBriefResponse;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyHistory;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyHistoryRepository;
import mallang_trip.backend.domain.user.service.UserService;
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
		if(user == null){
			return;
		}
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
