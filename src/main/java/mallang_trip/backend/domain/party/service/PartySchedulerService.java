package mallang_trip.backend.domain.party.service;

import static mallang_trip.backend.domain.income.constant.IncomeType.PARTY_INCOME;
import static mallang_trip.backend.domain.income.constant.IncomeType.PENALTY_INCOME;
import static mallang_trip.backend.domain.party.constant.PartyStatus.CANCELED_BY_DRIVER_REFUSED;
import static mallang_trip.backend.domain.party.constant.PartyStatus.CANCELED_BY_EXPIRATION;
import static mallang_trip.backend.domain.party.constant.PartyStatus.DAY_OF_TRAVEL;
import static mallang_trip.backend.domain.party.constant.PartyStatus.FINISHED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.income.service.IncomeService;
import mallang_trip.backend.domain.kakao.service.AlimTalkService;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.course.repository.CourseDayRepository;
import mallang_trip.backend.domain.party.repository.PartyProposalRepository;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartySchedulerService {

	private final PartyRepository partyRepository;
	private final PartyProposalRepository partyProposalRepository;
	private final PartyProposalService partyProposalService;
	private final PartyNotificationService partyNotificationService;
	private final CourseDayRepository courseDayRepository;
	private final IncomeService incomeService;
	private final AlimTalkService alimTalkService;

	/**
	 * 24시간 초과된 제안 만료 (1분마다 반복 실행)
	 */
	@Scheduled(fixedDelay = 60000)
	public void expireProposal() {
		String yesterday = LocalDateTime.now().minusDays(1).toString();

		partyProposalRepository.findExpiredProposal(yesterday)
			.stream()
			.forEach(proposal -> partyProposalService.expireProposal(proposal));

		partyRepository.findExpiredWaitingDriverApprovalParties(yesterday)
			.stream()
			.forEach(party -> {
				party.setStatus(CANCELED_BY_DRIVER_REFUSED);
				partyNotificationService.creationRefused(party);
			});
	}

	/**
	 * 매일 0시에 파티 STATUS 업데이트
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void expireParty() {
		String today = LocalDate.now().toString();

		handleExpiredRecruitingParty(today);
		handleDayOfTravelParty(today);
		FinishingParties(today);
	}

	/**
	 * 파티원 모집 중 여행 당일이 된 파티 처리
	 */
	private void handleExpiredRecruitingParty(String today) {
		partyRepository.findExpiredRecruitingParties(today).stream()
			.forEach(party -> {
				partyProposalService.expireWaitingProposalByParty(party);
				party.setStatus(CANCELED_BY_EXPIRATION);
				partyNotificationService.cancelByExpiration(party);
				// 위약금 수익 등록
				incomeService.createPenaltyIncome(party);
			});
	}

	/**
	 * 예약된 파티 중 여행 당일이 된 파티 처리
	 */
	private void handleDayOfTravelParty(String today) {
		partyRepository.findDayOfTravelParties(today).stream()
			.forEach(party -> {
				partyProposalService.expireWaitingProposalByParty(party);
				party.setStatus(DAY_OF_TRAVEL);
			});
	}

	/**
	 * 여행 완료된 파티 처리
	 */
	private void FinishingParties(String today){
		partyRepository.findFinishingParties(today).stream()
			.forEach(party -> party.setStatus(FINISHED));
	}

	/**
	 * 여행 시작 전날 알림 전송 (매일 18시 실행)
	 */
	@Scheduled(cron = "0 0 18 * * *")
	public void handleDayBeforeTravelParties(){
		String tomorrow = LocalDate.now().plusDays(1).toString();
		partyRepository.findDayOfTravelParties(tomorrow).stream()
			.forEach(party -> {
				partyNotificationService.dayBeforeTravel(party);
				alimTalkService.sendTravelerListAlimTalk(party);
				incomeService.create(party, PARTY_INCOME, party.getCourse().getTotalPrice());
			});
	}

	/**
	 * 여행 당일 시작 시간 알림 전송 (1시간마다 실행)
	 */
	@Scheduled(cron = "0 0 0/1 * * *")
	public void handleStartingParties(){
		partyRepository.findByStatus(DAY_OF_TRAVEL).stream()
			.filter(party -> isStartTime(party))
			.forEach(party -> partyNotificationService.startTravel(party));
	}

	/**
	 * 파티의 시작 시간이 현재 시간과 일치하는지 확인
	 */
	private boolean isStartTime(Party party) {
		return courseDayRepository.findByCourseAndDay(party.getCourse(), 1)
			.map(courseDay -> courseDay.getStartTime().getHour() == LocalTime.now().getHour())
			.orElse(false);
	}

	/**
	 * 여행이 끝난 다음날 알림 전송 (매일 09시 실행)
	 */
	@Scheduled(cron = "0 0 9 * * *")
	public void handleFinishedParties(){
		String yesterday = LocalDate.now().minusDays(1).toString();
		partyRepository.findFinishedParties(yesterday).stream()
			.forEach(party -> partyNotificationService.dayAfterTravel(party));
	}

}
