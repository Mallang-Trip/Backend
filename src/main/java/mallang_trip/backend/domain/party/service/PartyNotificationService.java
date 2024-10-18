package mallang_trip.backend.domain.party.service;

import static mallang_trip.backend.domain.notification.constant.NotificationType.FEEDBACK;
import static mallang_trip.backend.domain.notification.constant.NotificationType.PARTY;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.mail.service.MailService;
import mallang_trip.backend.domain.notification.entity.Firebase;
import mallang_trip.backend.domain.notification.repository.FirebaseRepository;
import mallang_trip.backend.domain.notification.service.FirebaseService;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.party.entity.PartyProposal;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.notification.service.NotificationService;
import org.apache.commons.fileupload.util.LimitedInputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyNotificationService {

	private final NotificationService notificationService;
	private final PartyMemberService partyMemberService;
	private final MailService mailService;
	private final FirebaseService firebaseService;
	private final FirebaseRepository firebaseRepository;
	private final String MallangTripUrl = "https://mallangtrip.com";

	private String getPartyName(Party party) {
		return party.getCourse().getName();
	}

	/**
	 * 파티 가입 & 제안 관련
	 */
	// 1. 새로운 파티 생성 신청이 들어왔을 경우
	public void newParty(User driver, Party party) {
		String content = new StringBuilder()
			.append("새로운 여행 신청이 존재합니다. 24시간 내에 수락/거절을 선택해주세요.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();
		notificationService.create(driver, content, PARTY, party.getId());
		mailService.sendEmailNotification(driver.getEmail(), driver.getName(), content,"새로운 여행 신청이 존재합니다.",absoluteUrl);
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", driver.getName(), content,"새로운 여행 신청이 존재합니다.",absoluteUrl);

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(driver);
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(),"말랑트립", content, url));
	}

	// 2. 내 파티 생성 신청을 드라이버가 수락했을 경우
	public void creationAccepted(Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("] 신청이 승인되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembers(party).stream()
			.forEach(user ->
					{notificationService.create(user.getUser(), content, PARTY, party.getId());
					mailService.sendEmailNotification(user.getUser().getEmail(), user.getUser().getName(), content,"신청이 승인되었습니다.",absoluteUrl);

					Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user.getUser());
					firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
					});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()){
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 3. 내 파티 생성 신청을 드라이버가 거절했을 경우
	public void creationRefused(Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("] 신청이 거절되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembers(party).stream()
			.forEach(user ->
					{notificationService.create(user.getUser(), content, PARTY, party.getId());
					mailService.sendEmailNotification(user.getUser().getEmail(), user.getUser().getName(), content,"신청이 거절되었습니다.",absoluteUrl);

					Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user.getUser());
					firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
					});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 4. 새로운 파티원이 가입했을 경우
	public void newMember(User joiner, Party party) {
		String content = new StringBuilder()
			.append(joiner.getNickname())
			.append("님이 [")
			.append(getPartyName(party))
			.append("]에 가입했습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.forEach(user ->
					{notificationService.create(user, content, PARTY, party.getId());
					mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"파티에 새로운 멤버가 가입했습니다.",absoluteUrl);

					Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
					firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
					});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 5. 새로운 여행자가 코스 변경 제안과 함께 가입을 신청했을 경우
	public void newJoinRequest(Party party) {
		String content = new StringBuilder()
			.append("신규 여행자가 [")
			.append(party.getCourse().getName())
			.append("] 참여 승인을 기다리고 있습니다. 24시간 내에 수락/거절을 선택해주세요.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());
				mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"신규 여행자가 참여 승인을 기다리고 있습니다.",absoluteUrl);

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			}
			);

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 6. 외부인 변경 제안으로 코스 제안이 만장일치 수락되었을 경우
	// 6-1. 신규 가입자
	public void joinAccepted(User joiner, Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("] 변경안이 승인되어 가입 완료되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		notificationService.create(joiner, content, PARTY, party.getId());

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(joiner);
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content, url));
	}

	// 6-2. 기존 파티원
	public void joinAcceptedAndCourseChanged(User joiner, Party party) {
		String content = new StringBuilder()
			.append(joiner.getNickname())
			.append("님의 [")
			.append(getPartyName(party))
			.append("] 변경안이 승인되어 가입 완료되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 7. 외부인 변경 제안으로 파티 가입이 거절되거나 24시간 초과되었을 경우
	public void joinRefused(PartyProposal proposal) {
		joinRefusedToJoiner(proposal);
		joinRefusedToMembers(proposal);
	}

	// 7-1. 신규 가입자
	private void joinRefusedToJoiner(PartyProposal proposal) {
		String content = new StringBuilder()
			.append("[")
			.append(proposal.getCourse().getName())
			.append("] 변경안 및 가입 신청이 거절되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(proposal.getParty().getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();
		notificationService.create(proposal.getProposer(), content, PARTY,
			proposal.getParty().getId());
		mailService.sendEmailNotification(proposal.getProposer().getEmail(), proposal.getProposer().getName(), content,"변경안 및 가입 신청이 거절되었습니다.",absoluteUrl);
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", proposal.getProposer().getName(), content,"변경안 및 가입 신청이 거절되었습니다.",absoluteUrl);
		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(proposal.getProposer());
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content, url));
	}

	// 7-2. 기존 가입자
	private void joinRefusedToMembers(PartyProposal proposal) {
		Party party = proposal.getParty();
		String content = new StringBuilder()
			.append(proposal.getProposer().getNickname())
			.append("님의 [")
			.append(proposal.getCourse().getName())
			.append("] 변경안 및 가입 신청이 거절되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.forEach(user ->
					{
					notificationService.create(user, content, PARTY, party.getId());
					mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"변경안 및 가입 신청이 거절되었습니다.",absoluteUrl);

					Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
					firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 8. 기존 파티원이 코스 변경을 제안했을 경우
	public void newCourseChange(PartyProposal proposal) {
		Party party = proposal.getParty();
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("] 새로운 코스 변경 제안이 존재합니다. 24시간 내에 수락/거절을 선택해주세요.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.filter(user -> !proposal.getProposer().equals(user))
			.forEach(user -> {notificationService.create(user, content, PARTY, party.getId());
				mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"새로운 코스 변경 제안이 존재합니다.",absoluteUrl);

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", party.getDriver().getUser().getName(), content,"새로운 코스 변경 제안이 존재합니다.",absoluteUrl);

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 9. 기존 파티원 변경 제안이 만장일치로 코스가 변경 승인되었을 경우
	public void courseChangeAccepted(PartyProposal proposal) {
		Party party = proposal.getParty();
		String content = new StringBuilder()
			.append(proposal.getProposer().getNickname())
			.append("님이 제안한 [")
			.append(getPartyName(party))
			.append("]대로 코스가 변경되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 10. 기존 파티원 변경 제안이 거절되거나 24시간 초과되었을 경우
	public void courseChangeRefused(PartyProposal proposal) {
		Party party = proposal.getParty();
		String content = new StringBuilder()
			.append(proposal.getProposer().getNickname())
			.append("님이 제안한 [")
			.append(proposal.getCourse().getName())
			.append("] 코스 변경 신청이 거절되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());
				mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"코스 변경 신청이 거절되었습니다.",absoluteUrl);

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 11. H.003 Overlap에서 ‘보내주세요’ 버튼 클릭한 후 예약 or 제안 가능해졌을 경우

	/**
	 * 파티 탈퇴 & 파토 관련
	 */
	// 1. 파티 멤버가 파티를 탈퇴할 경우
	// 1-1. 마지막 멤버가 아닌 경우
	public void memberQuit(User runner, Party party) {
		String content = new StringBuilder()
			.append(runner.getNickname())
			.append("님이 [")
			.append(getPartyName(party))
			.append("]을/를 탈퇴하여 파티원들의 말랑레디가 전원 OFF로 변경되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.filter(user -> !runner.equals(user))
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());
				mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"말랑레디가 전원 OFF로 변경되었습니다.",absoluteUrl);

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 1-2. 마지막 멤버인 경우
	public void lastMemberQuit(User runner, Party party) {
		String content = new StringBuilder()
			.append(runner.getNickname())
			.append("님이 [")
			.append(getPartyName(party))
			.append("]을/를 탈퇴하였습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		notificationService.create(party.getDriver().getUser(), content, PARTY, party.getId());
		mailService.sendEmailNotification(party.getDriver().getUser().getEmail(), party.getDriver().getUser().getName(), content,"파티를 탈퇴하였습니다.",absoluteUrl);
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", party.getDriver().getUser().getName(), content,"파티를 탈퇴하였습니다.",absoluteUrl);
		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(party.getDriver().getUser());
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content, url));
	}

	// 2. 드라이버가 파티를 탈퇴한 경우
	public void cancelByDriverQuit(Party party) {
		String content = new StringBuilder()
			.append("드라이버의 탈퇴로 [")
			.append(getPartyName(party))
			.append("]이/가 완전히 취소되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembers(party).stream()
			.map(PartyMember::getUser)
			.forEach(user ->
			{
				notificationService.create(user, content, PARTY, party.getId());
				mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"파티가 완전히 취소되었습니다.",absoluteUrl);

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 3. 파티원 전원 탈퇴로 파티가 취소되었을 경우
	public void cancelByAllQuit(Party party) {
		String content = new StringBuilder()
			.append("남은 여행자가 없어 [")
			.append(getPartyName(party))
			.append("]이/가 완전히 취소되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		notificationService.create(party.getDriver().getUser(), content, PARTY, party.getId());
		mailService.sendEmailNotification(party.getDriver().getUser().getEmail(), party.getDriver().getUser().getName(), content,"파티가 완전히 취소되었습니다.",absoluteUrl);
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", party.getDriver().getUser().getName(), content,"파티가 완전히 취소되었습니다.",absoluteUrl);

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(party.getDriver().getUser());
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content, url));
	}

	// 4. 모집기간 만료로 파티가 취소되었을 경우
	public void cancelByExpiration(Party party) {
		String content = new StringBuilder()
			.append("파티가 당일까지 확정되지 않아 [")
			.append(getPartyName(party))
			.append("]이/가 자동 취소되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.forEach(user ->
					{
						notificationService.create(user, content, PARTY, party.getId());
						mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"파티가 자동 취소되었습니다.",absoluteUrl);

						Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
						firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
					});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	/**
	 * 파티 예약 관련
	 */
	// 1. 파티원 모집이 완료되었을 경우
	public void partyFulled(Party party) {
		partyFulledToDriver(party);
		partyFulledToMembers(party);
	}

	// 1-1. 파티원
	private void partyFulledToMembers(Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("]의 인원 4명이 최종 확정되어 결제를 진행합니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembers(party).stream()
			.map(PartyMember::getUser)
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());
				//mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"결제를 진행합니다.");

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 1-2. 드라이버
	private void partyFulledToDriver(Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("]의 인원 4명이 최종 확정되어 파티원들의 결제가 완료되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		notificationService.create(party.getDriver().getUser(), content, PARTY, party.getId());
		mailService.sendEmailNotification(party.getDriver().getUser().getEmail(), party.getDriver().getUser().getName(), content,"파티원들의 결제가 완료되었습니다.",absoluteUrl); // party의 driver에게 이메일
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", party.getDriver().getUser().getName(), content,"파티원들의 결제가 완료되었습니다.",absoluteUrl);

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(party.getDriver().getUser());
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content, url));
	}

	// 2. 파티원 전원이 레디를 완료했을 경우
	public void allReady(Party party) {
		allReadyToDriver(party);
		allReadyToMembers(party);
	}

	// 2-1. 파티원
	private void allReadyToMembers(Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("] 모든 멤버들이 말랑레디를 확정하여 결제를 진행합니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembers(party).stream()
			.map(PartyMember::getUser)
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 2-2. 드라이버
	private void allReadyToDriver(Party party) {
		/* '<고객 이름 : 고객 전화번호 >' ... */
		String customers = partyMemberService.getMembers(party).stream()
			.map(PartyMember::getUser)
			.map(user -> new StringBuilder()
				.append("<")
				.append(user.getName())
				.append(" : ")
				.append(user.getPhoneNumber())
				.append(">")
				.toString())
			.reduce((a, b) -> new StringBuilder()
				.append(a)
				.append(", ")
				.append(b)
				.toString())
			.orElse("");
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("] 모든 멤버들이 말랑레디를 확정하여 파티원들의 결제가 완료되었습니다.")
			.append("고객 정보를 확인해주세요.")
			.append(customers)
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		notificationService.create(party.getDriver().getUser(), content, PARTY, party.getId());
		mailService.sendEmailNotification(party.getDriver().getUser().getEmail(), party.getDriver().getUser().getName(), content,"파티원들의 결제가 완료되었습니다.",absoluteUrl);
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", party.getDriver().getUser().getName(), content,"파티원들의 결제가 완료되었습니다.",absoluteUrl);

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(party.getDriver().getUser());
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content, url));
	}

	/**
	 * 파티 전날, 당일 관련 (미적용)
	 */
	// 1. 여행 전날
	public void dayBeforeTravel(Party party) {
		dayBeforeTravelToDriver(party);
		dayBeforeTravelToMembers(party);
	}

	// 1-1. 파티원
	public void dayBeforeTravelToMembers(Party party) {
		Driver driver = party.getDriver();
		String content = new StringBuilder()
			.append("드라이버의 정보를 확인해주세요. 드라이버 [")
			.append(driver.getUser().getName())
			.append("], 연락처 [")
			.append(driver.getUser().getPhoneNumber())
			.append("], 차종 [")
			.append(driver.getVehicleModel())
			.append("], [")
			.append(driver.getVehicleNumber())
			.append("]")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembers(party).stream()
			.map(PartyMember::getUser)
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());
				mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"드라이버의 정보를 확인해주세요.",absoluteUrl);
				mailService.sendEmailNotification("mallangtrip@mallangtrip.com", user.getName(), content,"드라이버의 정보를 확인해주세요.",absoluteUrl);
				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 1-2. 드라이버
	private void dayBeforeTravelToDriver(Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("]의 여행자 명단을 확인하고 대면할 때 승객의 신분증을 확인하여 신원을 철저히 검증해주세요.")
			.toString();
		notificationService.create(party.getDriver().getUser(), content, PARTY, party.getId());
		mailService.sendEmailNotification(party.getDriver().getUser().getEmail(), party.getDriver().getUser().getName(), content,"여행자를 확인해주세요!",MallangTripUrl);	// => 노션에 ??로 적혀있음

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(party.getDriver().getUser());
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content,"/"));	// => 노션에 ??로 적혀있음
	}

	// 2. 여행 당일 시작 시간
	public void startTravel(Party party) {
		startTravelToDriver(party);
		startTravelToMember(party);
	}

	// 2-1. 여행자
	private void startTravelToMember(Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("] 이 시작되었습니다! 즐겁고 안전한 말랑트립 되세요 :)")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembers(party).stream()
			.map(PartyMember::getUser)
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());
				mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"여행이 시작되었습니다!",absoluteUrl);

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 2-2. 드라이버
	private void startTravelToDriver(Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("] 이 시작되었습니다! ")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();
		notificationService.create(party.getDriver().getUser(), content, PARTY, party.getId());
		mailService.sendEmailNotification(party.getDriver().getUser().getEmail(), party.getDriver().getUser().getName(), content,"여행이 시작되었습니다!",absoluteUrl);
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", party.getDriver().getUser().getName(), content,"여행이 시작되었습니다!",absoluteUrl);

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(party.getDriver().getUser());
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content, url));
	}

	// 3. 여행 다음 날
	public void dayAfterTravel(Party party) {
		String content = new StringBuilder()
			.append("[")
			.append(getPartyName(party))
			.append("]은 어떠셨나요? 말랑트립의 성장과 발전을 위해 피드백을 남겨주신다면 감사드리겠습니다 :) ")
			.toString();
		String url = new StringBuilder()
				.append("/driver/profile/")
				.append(party.getDriver().getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.forEach(user ->
					{
						notificationService.create(user, content, FEEDBACK, null);
						mailService.sendEmailNotification(user.getEmail(), user.getName(), content,"여행이 끝났습니다. 피드백을 남겨주세요!",absoluteUrl);

						Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
						firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
					});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	/**
	 * 파티 예약 취소 관련
	 */
	// 1. 예약 취소 -> 위약금이 발생하지 않은 경우, 일부 위약금이 발생한 경우
	public void cancelReservation(User runner, Party party){
		cancelReservationToDriver(runner, party);
		cancelReservationToMember(runner, party);
	}

	// 1-1. 여행자
	private void cancelReservationToMember(User runner, Party party){
		String content = new StringBuilder()
			.append(runner.getNickname())
			.append("님의 [")
			.append(getPartyName(party))
			.append("] 예약 취소로 영업일 기준 3일 내에 예약금 전액이 환불되며 파티원을 재모집합니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembers(party).stream()
			.map(PartyMember::getUser)
			.filter(user -> !runner.equals(user))
			.forEach(user -> {
				notificationService.create(user, content, PARTY, party.getId());

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 1-2. 드라이버
	private void cancelReservationToDriver(User runner, Party party){
		String content = new StringBuilder()
			.append(runner.getNickname())
			.append("님의 [")
			.append(getPartyName(party))
			.append("] 예약 취소로 파티원을 재모집합니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();
		notificationService.create(party.getDriver().getUser(), content, PARTY, party.getId());
		mailService.sendEmailNotification(party.getDriver().getUser().getEmail(), party.getDriver().getUser().getName(), content,"파티원을 재모집합니다.",absoluteUrl);
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", party.getDriver().getUser().getName(), content,"파티원을 재모집합니다.",absoluteUrl);

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(party.getDriver().getUser());
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content, url));
	}

	// 2. 예약 취소 -> 전액 위약금이 발생한 경우
	public void cancelReservationWithFullPenalty(User runner, Party party){
		String content = new StringBuilder()
			.append(runner.getNickname())
			.append("님이 [")
			.append(getPartyName(party))
			.append("] 예약을 취소하였습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembersAndDriver(party).stream()
			.forEach(user ->{
				notificationService.create(user, content, PARTY, party.getId());

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 3. 드라이버 취소로 인한 파티 취소 알림
	public void cancelReservationByDriver(Party party){
		String content = new StringBuilder()
			.append("드라이버의 예약 취소로 [")
			.append(getPartyName(party))
			.append("]이/가 취소되었습니다. 영업일 3일 내로 전액 환불될 예정입니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();

		List<String> firebaseTokens = new ArrayList<>();

		partyMemberService.getMembers(party).stream()
			.map(PartyMember::getUser)
			.forEach(user ->{
				notificationService.create(user, content, PARTY, party.getId());

				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
				firebase.ifPresent(f -> firebaseTokens.addAll(f.getTokens()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()) {
			firebaseService.sendPushMessage(firebaseTokens, "말랑트립", content, url);
		}
	}

	// 4. 전원 예약 취소
	public void cancelReservationByLastMember(User runner, Party party){
		String content = new StringBuilder()
			.append(runner.getNickname())
			.append("님이 [")
			.append(getPartyName(party))
			.append("] 예약을 취소하였습니다. 남은 여행자가 없어 [")
			.append(getPartyName(party))
			.append("]이/가 완전히 취소되었습니다.")
			.toString();
		String url = new StringBuilder()
				.append("/party/detail/")
				.append(party.getId())
				.append("?login_required=true")
				.toString();
		String absoluteUrl = new StringBuilder()
				.append(MallangTripUrl)
				.append(url)
				.toString();
		notificationService.create(party.getDriver().getUser(), content, PARTY, party.getId());
		mailService.sendEmailNotification(party.getDriver().getUser().getEmail(), party.getDriver().getUser().getName(), content,"파티가 완전히 취소되었습니다.",absoluteUrl);
		mailService.sendEmailNotification("mallangtrip@mallangtrip.com", party.getDriver().getUser().getName(), content,"파티가 완전히 취소되었습니다.",absoluteUrl);

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(party.getDriver().getUser());
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content, url));
	}
}
