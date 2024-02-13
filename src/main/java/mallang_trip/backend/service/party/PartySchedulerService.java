package mallang_trip.backend.service.party;

import static mallang_trip.backend.constant.PartyStatus.CANCELED_BY_EXPIRATION;
import static mallang_trip.backend.constant.PartyStatus.DAY_OF_TRAVEL;
import static mallang_trip.backend.constant.PartyStatus.FINISHED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.repository.course.CourseDayRepository;
import mallang_trip.backend.repository.party.PartyProposalRepository;
import mallang_trip.backend.repository.party.PartyRepository;
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

	/**
	 * 24시간 초과된 제안 만료 (1분마다 반복 실행)
	 */
	@Scheduled(fixedDelay = 60000)
	public void expireProposal() {
		partyProposalRepository.findExpiredProposal(LocalDateTime.now().minusDays(1).toString())
			.stream()
			.forEach(proposal -> partyProposalService.expireProposal(proposal));
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
				// TODO: discountPrice(위약금) 존재 시 드라이버에게 송금 후 알림
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
	@Scheduled(cron = "0 0 9 * * *")
	public void handleDayBeforeTravelParties(){
		String tomorrow = LocalDate.now().plusDays(1).toString();
		partyRepository.findDayOfTravelParties(tomorrow).stream()
			.forEach(party -> partyNotificationService.dayBeforeTravel(party));
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
