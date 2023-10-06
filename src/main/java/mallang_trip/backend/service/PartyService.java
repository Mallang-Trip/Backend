package mallang_trip.backend.service;

import static mallang_trip.backend.constant.AgreementStatus.ACCEPT;
import static mallang_trip.backend.constant.AgreementStatus.WAITING;
import static mallang_trip.backend.constant.PartyStatus.COURSE_CHANGE_APPROVAL_WAITING;
import static mallang_trip.backend.constant.PartyStatus.DRIVER_REFUSED;
import static mallang_trip.backend.constant.PartyStatus.JOIN_APPROVAL_WAITING;
import static mallang_trip.backend.constant.PartyStatus.MONOPOLIZED;
import static mallang_trip.backend.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.constant.PartyStatus.RECRUIT_COMPLETED;
import static mallang_trip.backend.constant.ProposalStatus.ACCEPTED;
import static mallang_trip.backend.constant.ProposalStatus.CANCELED;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_CHANGE_COURSE;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.EXCEED_PARTY_CAPACITY;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.PARTY_NOT_RECRUITING;
import static mallang_trip.backend.controller.io.BaseResponseStatus.PROPOSAL_END;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.AgreementStatus;
import mallang_trip.backend.constant.PartyStatus;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.constant.ProposalType;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.Party.JoinPartyRequest;
import mallang_trip.backend.domain.dto.Party.PartyAgreementResponse;
import mallang_trip.backend.domain.dto.Party.PartyBriefResponse;
import mallang_trip.backend.domain.dto.Party.PartyDetailsResponse;
import mallang_trip.backend.domain.dto.Party.PartyIdResponse;
import mallang_trip.backend.domain.dto.Party.PartyMemberResponse;
import mallang_trip.backend.domain.dto.Party.PartyRequest;
import mallang_trip.backend.domain.dto.Party.ProposalResponse;
import mallang_trip.backend.domain.dto.course.CourseRequest;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyAgreement;
import mallang_trip.backend.domain.entity.party.PartyMembers;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.repository.driver.DriverRepository;
import mallang_trip.backend.repository.course.CourseRepository;
import mallang_trip.backend.repository.party.PartyAgreementRepository;
import mallang_trip.backend.repository.party.PartyMembersRepository;
import mallang_trip.backend.repository.party.PartyProposalRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyService {

	private final UserService userService;
	private final CourseService courseService;
	private final CourseRepository courseRepository;
	private final DriverRepository driverRepository;
	private final PartyRepository partyRepository;
	private final PartyMembersRepository partyMembersRepository;
	private final PartyProposalRepository partyProposalRepository;
	private final PartyAgreementRepository partyAgreementRepository;

	// 파티 생성 신청
	public PartyIdResponse createParty(PartyRequest request) {
		Driver driver = driverRepository.findById(request.getDriverId())
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		// 코스 변경 유무 Check
		Course course;
		if (request.getChangeCourse()) {
			course = courseService.createCourse(request.getNewCourse());
		} else {
			course = courseService.copyCourse(
				courseRepository.findById(request.getCourseId())
					.orElseThrow(() -> new BaseException(Not_Found)));
		}

		Party party = partyRepository.save(Party.builder()
			.driver(driver)
			.course(course)
			.region(driver.getRegion())
			.capacity(driver.getVehicleCapacity())
			.headcount(request.getHeadcount())
			.startDate(LocalDate.parse(request.getStartDate()))
			.endDate(LocalDate.parse(request.getEndDate()))
			.build());

		partyMembersRepository.save(PartyMembers.builder()
			.party(party)
			.user(userService.getCurrentUser())
			.headcount(request.getHeadcount())
			.build());

		return PartyIdResponse.builder()
			.partyId(party.getId())
			.build();
	}

	// (드라이버) 파티 생성 수락 or 거절
	public void acceptCreateParty(Long partyId, Boolean accept) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(Not_Found));
		if (!party.getDriver().getUser().equals(userService.getCurrentUser())) {
			throw new BaseException(Unauthorized);
		}
		if (accept) {
			party.setStatus(RECRUITING);
			// 알림 전송
		} else {
			party.setStatus(DRIVER_REFUSED);
			// 알림 전송
		}
	}

	// 파티 가입 신청
	public void joinParty(JoinPartyRequest request) {
		// Exception Check
		Party party = partyRepository.findById(request.getPartyId())
			.orElseThrow(() -> new BaseException(Not_Found));
		if (!party.getStatus().equals(RECRUITING)) {
			throw new BaseException(PARTY_NOT_RECRUITING);
		}
		if (request.getHeadcount() + party.getHeadcount() > party.getCapacity()) {
			throw new BaseException(EXCEED_PARTY_CAPACITY);
		}

		party.setPrevStatus(RECRUITING);
		if (request.getChangeCourse()) {
			joinPartyWithCourseChange(request, party);
		} else {
			joinPartyWithoutCourseChange(request, party);
		}
	}

	// 코스 변경 제안
	public void proposeCourseChange(Long partyId, CourseRequest request) {
		User user = userService.getCurrentUser();
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(Not_Found));
		Course course = courseService.createCourse(request);

		// Exception Check
		PartyStatus partyStatus = party.getStatus();
		if (!(partyStatus.equals(RECRUITING) || partyStatus.equals(MONOPOLIZED)
			|| partyStatus.equals(
			RECRUIT_COMPLETED))) {
			throw new BaseException(CANNOT_CHANGE_COURSE);
		}
		if (!isMyParty(party)) {
			throw new BaseException(Unauthorized);
		}

		party.setPrevStatus(party.getStatus());

		PartyProposal proposal = partyProposalRepository.save(PartyProposal.builder()
			.course(course)
			.party(party)
			.proposer(user)
			.type(ProposalType.COURSE_CHANGE)
			.build());

		partyMembersRepository.findByParty(party)
			.forEach(member -> {
				AgreementStatus status = WAITING;
				if (member.getUser().equals(user)) {
					status = ACCEPT;
				}
				partyAgreementRepository.save(PartyAgreement.builder()
					.proposal(proposal)
					.members(member)
					.status(status)
					.build());
			});

		party.setStatus(PartyStatus.COURSE_CHANGE_APPROVAL_WAITING);
	}

	// 제안 수락 or 거절
	public void acceptProposal(Long proposalId, Boolean accept) {
		User user = userService.getCurrentUser();
		PartyProposal proposal = partyProposalRepository.findById(proposalId)
			.orElseThrow(() -> new BaseException(Not_Found));
		//
		// proposal status check
		//
		if (user.getRole().equals(Role.ROLE_DRIVER)) {
			acceptProposalByDriver(proposal, accept);
		} else {
			acceptProposalByUser(proposal, accept);
		}
	}

	// 모집중인 파티 조회 By 지역, 인원수, 날짜
	public List<PartyBriefResponse> findParties(String region, Integer headcount,
		String startDate, String endDate, Integer maxPrice) {
		List<Party> parties;
		if(region.equals("all")){
			parties = partyRepository.findByStatus(RECRUITING);
		} else {
			parties = partyRepository.findByRegionAndStatus(region, RECRUITING);
		}
		return parties.stream()
			.filter(party -> party.checkHeadcount(headcount))
			.filter(party -> party.checkDate(startDate, endDate))
			.filter(party -> party.checkMaxPrice(maxPrice))
			.map(PartyBriefResponse::of)
			.collect(Collectors.toList());
	}


	// 내 파티 목록 조회
	public List<PartyBriefResponse> getMyParties() {
		User user = userService.getCurrentUser();
		if (user.getRole().equals(Role.ROLE_DRIVER)) {
			return getMyPartiesByDriver(user);
		} else {
			return getMyPartiesByUser(user);
		}
	}

	// 파티 상세조회
	public PartyDetailsResponse getPartyDetails(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(Not_Found));
		return getPartyDetails(party, isMyParty(party));
	}

	// 내가 신청한 파티 목록조회
	public List<PartyBriefResponse> getMyProposingParties() {
		List<Party> parties = partyProposalRepository.findByProposer(userService.getCurrentUser())
			.stream()
			.map(proposal -> proposal.getParty())
			.collect(Collectors.toList());
		return parties
			.stream()
			.map(PartyBriefResponse::of)
			.collect(Collectors.toList());
	}

	// 내가 신청한 파티 상세조회
	public PartyDetailsResponse getMyProposingPartyDetails(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(Not_Found));
		return getPartyDetails(party, true);
	}

	// 코스변경 다시 제안하기
	public void reProposeCourseChange(Long proposalId, CourseRequest request) {
		PartyProposal proposal = partyProposalRepository.findById(proposalId)
			.orElseThrow(() -> new BaseException(Not_Found));
		cancelProposal(proposalId);
		proposeCourseChange(proposal.getParty().getId(), request);
	}

	// 가입신청 다시 하기
	public void reProposeJoinParty(Long proposalId, JoinPartyRequest request) {
		cancelProposal(proposalId);
		joinParty(request);
	}

	// 제안 취소
	public void cancelProposal(Long proposalId) {
		PartyProposal proposal = partyProposalRepository.findById(proposalId)
			.orElseThrow(() -> new BaseException(Not_Found));
		Party party = proposal.getParty();

		proposal.setStatus(CANCELED);
		party.setStatus(party.getPrevStatus());
	}

	// 파티 나가기

	private void joinPartyWithoutCourseChange(JoinPartyRequest request, Party party) {
		User user = userService.getCurrentUser();
		PartyProposal proposal = partyProposalRepository.save(PartyProposal.builder()
			.course(party.getCourse())
			.party(party)
			.proposer(user)
			.headcount(request.getHeadcount())
			.content(request.getContent())
			.type(ProposalType.JOIN)
			.build());

		partyMembersRepository.findByParty(party)
			.forEach(member -> {
				partyAgreementRepository.save(PartyAgreement.builder()
					.proposal(proposal)
					.members(member)
					.build());
			});

		party.setStatus(JOIN_APPROVAL_WAITING);
	}

	private void joinPartyWithCourseChange(JoinPartyRequest request, Party party) {
		User user = userService.getCurrentUser();
		Course course = courseService.createCourse(request.getNewCourse());

		PartyProposal proposal = partyProposalRepository.save(PartyProposal.builder()
			.course(course)
			.party(party)
			.proposer(user)
			.headcount(request.getHeadcount())
			.content(request.getContent())
			.type(ProposalType.JOIN_WITH_COURSE_CHANGE)
			.build());

		partyMembersRepository.findByParty(party)
			.forEach(member -> {
				partyAgreementRepository.save(PartyAgreement.builder()
					.proposal(proposal)
					.members(member)
					.build());
			});

		party.setStatus(JOIN_APPROVAL_WAITING);
	}

	private void acceptProposalByUser(PartyProposal proposal, Boolean accept) {
		User user = userService.getCurrentUser();
		PartyMembers members = partyMembersRepository.findByPartyAndUser(proposal.getParty(), user);
		PartyAgreement agreement = partyAgreementRepository.findByMembersAndProposal(members,
			proposal);
		if (proposal.getType().equals(ProposalType.COURSE_CHANGE)) {
			acceptCourseChange(agreement, accept);
		} else {
			acceptPartyJoin(agreement, accept);
		}
	}

	private void acceptProposalByDriver(PartyProposal proposal, Boolean accept) {
		if (proposal.getType().equals(ProposalType.COURSE_CHANGE)) {
			acceptCourseChangeByDriver(proposal, accept);
		} else {
			acceptPartyJoinByDriver(proposal, accept);
		}
	}

	private List<PartyBriefResponse> getMyPartiesByUser(User user) {
		List<Party> parties = partyMembersRepository.findByUser(user)
			.stream()
			.map(partyMembers -> partyMembers.getParty())
			.collect(Collectors.toList());
		return parties.stream().map(PartyBriefResponse::of).collect(Collectors.toList());
	}

	private List<PartyBriefResponse> getMyPartiesByDriver(User user) {
		Driver driver = driverRepository.findById(user.getId())
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		return partyRepository.findByDriver(driver)
			.stream()
			.map(PartyBriefResponse::of)
			.collect(Collectors.toList());
	}

	private void acceptPartyJoin(PartyAgreement agreement, boolean accept) {
		PartyProposal proposal = agreement.getProposal();
		PartyStatus partyStatus = proposal.getParty().getStatus();
		if (!partyStatus.equals(JOIN_APPROVAL_WAITING)) {
			throw new BaseException(PROPOSAL_END);
		}
		if (accept) {
			agreement.setStatus(ACCEPT);
			if (isUnanimity(proposal)) {
				addMember(proposal);
			}
		} else {
			refuseProposal(proposal);
		}
	}

	private void acceptCourseChange(PartyAgreement agreement, boolean accept) {
		PartyProposal proposal = agreement.getProposal();
		PartyStatus partyStatus = proposal.getParty().getStatus();
		if (!partyStatus.equals(COURSE_CHANGE_APPROVAL_WAITING)) {
			throw new BaseException(PROPOSAL_END);
		}
		if (accept) {
			agreement.setStatus(ACCEPT);
			if (isUnanimity(proposal)) {
				changeCourse(proposal);
			}
		} else {
			refuseProposal(proposal);
		}
	}

	private void acceptPartyJoinByDriver(PartyProposal proposal, boolean accept) {
		PartyStatus partyStatus = proposal.getParty().getStatus();
		if (!partyStatus.equals(JOIN_APPROVAL_WAITING)) {
			throw new BaseException(PROPOSAL_END);
		}
		if (accept) {
			proposal.setDriverAgreement(ACCEPT);
			if (isUnanimity(proposal)) {
				addMember(proposal);
			}
		} else {
			refuseProposal(proposal);
		}
	}

	private void acceptCourseChangeByDriver(PartyProposal proposal, boolean accept) {
		PartyStatus partyStatus = proposal.getParty().getStatus();
		if (!partyStatus.equals(COURSE_CHANGE_APPROVAL_WAITING)) {
			throw new BaseException(PROPOSAL_END);
		}
		if (accept) {
			proposal.setDriverAgreement(ACCEPT);
			if (isUnanimity(proposal)) {
				changeCourse(proposal);
			}
		} else {
			refuseProposal(proposal);
		}
	}

	// 가입 신청 모두 동의했을 때
	private void addMember(PartyProposal proposal) {
		Party party = proposal.getParty();
		// proposal status 변경
		proposal.setStatus(ACCEPTED);
		// 멤버 추가
		partyMembersRepository.save(PartyMembers.builder()
			.party(party)
			.user(proposal.getProposer())
			.headcount(proposal.getHeadcount())
			.build());
		// party headcount 추가
		party.setHeadcount(party.getHeadcount() + proposal.getHeadcount());
		// 코스 변경
		party.setCourse(proposal.getCourse());
		// Party status 변경
		if (party.getCapacity() == party.getHeadcount()) {
			party.setStatus(RECRUIT_COMPLETED);
		} else {
			party.setStatus(RECRUITING);
		}
	}

	// 코스 변경 모두 동의했을 때
	private void changeCourse(PartyProposal proposal) {
		Party party = proposal.getParty();

		proposal.setStatus(ACCEPTED);
		party.setCourse(proposal.getCourse());
		party.setStatus(RECRUITING);
	}

	// 제안 거절 시
	private void refuseProposal(PartyProposal proposal) {
		proposal.setStatus(ProposalStatus.REFUSED);
		proposal.getParty().setStatus(RECRUITING);
		partyAgreementRepository.deleteByProposal(proposal);
		//거절 알림 전송
	}

	private PartyDetailsResponse getPartyDetails(Party party, Boolean isMyParty) {
		PartyDetailsResponse response = PartyDetailsResponse.builder()
			.partyId(party.getId())
			.myParty(true)
			.partyStatus(party.getStatus())
			.driverId(party.getDriver().getId())
			.driverName(party.getDriver().getUser().getName())
			.capacity(party.getCapacity())
			.headcount(party.getHeadcount())
			.region(party.getRegion())
			.startDate(party.getStartDate())
			.endDate(party.getEndDate())
			.course(courseService.getCourseDetails(party.getCourse()))
			.content(party.getContent())
			.proposalExist(isProposalExist(party))
			.build();
		// 내가 속한 파티일 경우: 멤버, 제안 정보 추가
		if (isMyParty) {
			List<PartyMemberResponse> members = partyMembersRepository.findByParty(party)
				.stream()
				.map(PartyMemberResponse::of)
				.collect(Collectors.toList());
			response.setMembers(members);
			if(isProposalExist(party)) response.setProposal(getProposalDetails(party));
		}
		return response;
	}

	private List<PartyAgreementResponse> getAgreement(PartyProposal proposal) {
		return partyAgreementRepository.findByProposal(proposal)
			.stream()
			.map(PartyAgreementResponse::of)
			.collect(Collectors.toList());
	}

	private ProposalResponse getProposalDetails(Party party) {
		PartyProposal proposal = partyProposalRepository.findByPartyAndStatus(party,
			ProposalStatus.WAITING);
		return ProposalResponse.builder()
			.proposalId(proposal.getId())
			.proposerId(proposal.getProposer().getId())
			.proposerNickname(proposal.getProposer().getNickname())
			.type(proposal.getType())
			.status(proposal.getStatus())
			.driverAgreement(proposal.getDriverAgreement())
			.course(courseService.getCourseDetails(proposal.getCourse()))
			.headcount(proposal.getHeadcount())
			.content(proposal.getContent())
			.memberAgreement(getAgreement(proposal))
			.build();
	}

	private Boolean isMyParty(Party party) {
		if (userService.getCurrentUser().equals(party.getDriver().getUser())) {
			return true;
		}
		return partyMembersRepository.existsByPartyAndUser(party, userService.getCurrentUser());
	}

	private Boolean isProposalExist(Party party) {
		return partyProposalRepository.existsByPartyAndStatus(party, ProposalStatus.WAITING);
	}

	private Boolean isUnanimity(PartyProposal proposal){
		Integer flag = partyProposalRepository.isUnanimity(proposal.getId());
		if(flag == 0) return false;
		else return true;
	}
}
