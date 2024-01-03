package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.ALREADY_PARTY_MEMBER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PARTY;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_PARTY_MEMBER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;
import mallang_trip.backend.domain.dto.party.PartyMemberResponse;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.party.PartyMemberRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyMemberService {

	private final UserService userService;
	private final PartyMemberRepository partyMemberRepository;
	private final PartyRepository partyRepository;

	/**
	 * 파티 멤버 상세 조회
	 */
	public List<PartyMemberResponse> getMembersDetails(Party party) {
		return partyMemberRepository.findByParty(party).stream()
			.map(PartyMemberResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 파티 멤버 조회
	 */
	public List<PartyMember> getMembers(Party party) {
		return partyMemberRepository.findByParty(party);
	}

	/**
	 * 파티 멤버 추가
	 */
	public PartyMember createMember(Party party, User user, Integer headcount) {
		if (partyMemberRepository.existsByPartyAndUser(party, user)) {
			throw new BaseException(ALREADY_PARTY_MEMBER);
		}
		party.setHeadcount(party.getHeadcount() + headcount);
		return partyMemberRepository.save(PartyMember.builder()
			.party(party)
			.user(user)
			.headcount(headcount)
			.build());
	}

	/**
	 * 파티 멤버 삭제
	 */
	public void deleteMember(Party party, PartyMember member) {
		party.setHeadcount(party.getHeadcount() - member.getHeadcount());
		partyMemberRepository.delete(member);
	}

	/**
	 * 현재 유저 파티 레디
	 */
	public void ready(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		PartyMember member = partyMemberRepository.findByPartyAndUser(party,
				userService.getCurrentUser())
			.orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
		member.setReady(true);
	}

	/**
	 * 현재 유저 파티 레디 취소
	 */
	public void cancelReady(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		PartyMember member = partyMemberRepository.findByPartyAndUser(party,
				userService.getCurrentUser())
			.orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
		member.setReady(false);
	}

	/**
	 * 파티 전원 레디 처리
	 */
	public void readyAllMembers(Party party) {
		getMembers(party).stream()
			.forEach(member -> member.setReady(true));
	}

	/**
	 * 파티 전원 레디 취소
	 */
	public void cancelReadyAllMembers(Party party) {
		getMembers(party).stream()
			.forEach(member -> member.setReady(false));
	}

	/**
	 * 파티 멤버 전원 레디 확인
	 */
	public Boolean isEveryoneReady(Party party) {
		return partyMemberRepository.isEveryoneReady(party.getId());
	}

	/** 파티 총 인원 수 조회 */
	public Integer getTotalHeadcount(Party party){
		int headcount = 0;
		for(PartyMember member : getMembers(party)){
			headcount += member.getHeadcount();
		}
		return headcount;
	}
}
