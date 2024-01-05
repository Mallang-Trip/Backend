package mallang_trip.backend.service.party;

import static mallang_trip.backend.constant.PartyStatus.CANCELED_BY_EXPIRATION;
import static mallang_trip.backend.constant.PartyStatus.DAY_OF_TRAVEL;
import static mallang_trip.backend.constant.PartyStatus.FINISHED;
import static mallang_trip.backend.constant.PartyStatus.SEALED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.repository.party.PartyProposalRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyScheduler {

	private final PartyRepository partyRepository;
	private final PartyProposalRepository partyProposalRepository;
	private final PartyProposalService partyProposalService;

	/**
	 * 24시간 초과된 제안 만료 (1분마다 반복 실행)
	 */
	@Scheduled(fixedDelay = 60000)
	public void expireProposal() {
		partyProposalRepository.findExpiredProposal(LocalDateTime.now().minusDays(1))
			.stream()
			.forEach(proposal -> partyProposalService.handleExpiredProposal(proposal));
	}

	/**
	 * 매일 0시에 파티 STATUS 업데이트
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void expireParty() {
		String today = LocalDate.now().toString();

		handleExpiredRecruitingParty(today);
		handleExpiredWaitingCourseChangeApprovalParty(today);
		handleDayOfTravelParty(today);
		handleFinishedParty(today);
	}

	/**
	 * 파티원 모집 중 여행 당일이 된 파티 처리
	 */
	private void handleExpiredRecruitingParty(String today) {
		partyRepository.findExpiredRecruitingParties(today).stream()
			.forEach(party -> party.setStatus(CANCELED_BY_EXPIRATION));
	}

	/**
	 * 코스 변경 제안 중 여행 당일이 된 파티 처리
	 */
	private void handleExpiredWaitingCourseChangeApprovalParty(String today) {
		partyRepository.findExpiredWaitingCourseChangeApprovalParties(today).stream()
			.forEach(party -> cancelCourseChangeProposalAndSeal(party));
	}

	/**
	 * 진행중인 코스 변경 제안을 종료하고 SEALED 상태로 변경
	 */
	private void cancelCourseChangeProposalAndSeal(Party party) {
		partyProposalService.expireWaitingProposalByParty(party);
		party.setStatus(SEALED);
	}

	/**
	 * 예약된 파티 중 여행 당일이 된 파티 처리
	 */
	private void handleDayOfTravelParty(String today){
		partyRepository.findDayOfTravelParties(today).stream()
			.forEach(party -> party.setStatus(DAY_OF_TRAVEL));
	}

	/**
	 * 여행 완료된 파티 처리
	 */
	private void handleFinishedParty(String today){
		partyRepository.findFinishedParties(today).stream()
			.forEach(party -> party.setStatus(FINISHED));
	}
}
