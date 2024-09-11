package mallang_trip.backend.domain.party.service;

import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.ALREADY_PARTY_MEMBER;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.NOT_PARTY_MEMBER;
import static mallang_trip.backend.domain.reservation.constant.UserPromotionCodeStatus.TRY;
import static mallang_trip.backend.domain.reservation.constant.UserPromotionCodeStatus.USE;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.reservation.entity.UserPromotionCode;
import mallang_trip.backend.domain.reservation.repository.PromotionCodeRepository;
import mallang_trip.backend.domain.reservation.repository.UserPromotionCodeRepository;
import mallang_trip.backend.domain.reservation.service.PromotionService;
import mallang_trip.backend.domain.user.constant.Role;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.party.dto.PartyMemberCompanionRequest;
import mallang_trip.backend.domain.party.dto.PartyMemberCompanionResponse;
import mallang_trip.backend.domain.party.dto.PartyMemberResponse;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.party.entity.PartyMemberCompanion;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyMemberCompanionRepository;
import mallang_trip.backend.domain.party.repository.PartyMemberRepository;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyMemberService {

    private final CurrentUserService currentUserService;
    private final PartyMemberRepository partyMemberRepository;
    private final PartyMemberCompanionRepository partyMemberCompanionRepository;
    private final UserPromotionCodeRepository userPromotionCodeRepository;

    /**
     * 파티 멤버 상세 조회
     */
    public List<PartyMemberResponse> getMembersDetails(Party party) {
        return partyMemberRepository.findByParty(party).stream()
            .map(member -> toPartyMemberResponse(member))
            .collect(Collectors.toList());
    }

    /**
     * PartyMember -> PartyMemberResponse
     */
    private PartyMemberResponse toPartyMemberResponse(PartyMember member) {
        List<PartyMemberCompanionResponse> companions = partyMemberCompanionRepository.findByMember(
                member).stream()
            .map(PartyMemberCompanionResponse::of)
            .collect(Collectors.toList());
        return PartyMemberResponse.of(member, companions);
    }

    /**
     * 파티 멤버 조회
     */
    public List<PartyMember> getMembers(Party party) {
        return partyMemberRepository.findByParty(party);
    }

    /**
     * 드라이버, 파티 멤버 조회
     */
    public List<User> getMembersAndDriver(Party party) {
        List<User> users = getMembers(party).stream()
            .map(PartyMember::getUser)
            .collect(Collectors.toList());
        users.add(party.getDriver().getUser());
        return users;
    }

    /**
     * 파티 멤버 추가 및 파티 headcount 증가
     */
    public PartyMember createMember(Party party, User user, Integer headcount,
        List<PartyMemberCompanionRequest> requests,Long userPromotionCodeId) {
        if (partyMemberRepository.existsByPartyAndUser(party, user)) {
            throw new BaseException(ALREADY_PARTY_MEMBER);
        }
        System.out.println(userPromotionCodeId);
        Optional<UserPromotionCode> userPromotionCode = userPromotionCodeRepository.findByIdAndStatus(userPromotionCodeId, TRY);
        if(userPromotionCode.isPresent()){
            userPromotionCode.get().changeStatus(USE);
            userPromotionCode.get().getCode().use();
        }

        party.increaseHeadcount(headcount);
        PartyMember member = partyMemberRepository.save(PartyMember.builder()
            .party(party)
            .user(user)
            .headcount(headcount)
            .userPromotionCode(userPromotionCode.orElse(null))
            .build());
        createCompanion(member, requests);
        return member;
    }

    /**
     * 파티 멤버 동행자 추가
     */
    private void createCompanion(PartyMember member, List<PartyMemberCompanionRequest> requests) {
        if (requests == null) {
            return;
        }
        requests.stream().forEach(request -> {
            partyMemberCompanionRepository.save(PartyMemberCompanion.builder()
                .member(member)
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build());
        });
    }

    /**
     * 현재 유저 파티 멤버 삭제, 파티 인원 감소
     */
    public void deleteMemberAndDecreaseHeadcount(Party party, PartyMember member) {
        party.setHeadcount(party.getHeadcount() - member.getHeadcount());
        partyMemberRepository.delete(member);
    }

    /**
     * 파티 레디 or 취소
     */
    public boolean setReady(Party party, Boolean ready) {
        Role role = currentUserService.getCurrentUser().getRole();
        if (role.equals(Role.ROLE_DRIVER)) {
            return setReadyByDriver(party, ready);
        }
        if (role.equals(Role.ROLE_USER)) {
            return setReadyByMember(party, ready);
        }
        return false;
    }

    /**
     * (드라이버) 파티 레디 or 취소
     */
    public boolean setReadyByDriver(Party party, Boolean ready) {
        if (!party.getDriver().getUser().equals(currentUserService.getCurrentUser())) {
            throw new BaseException(NOT_PARTY_MEMBER);
        }
        if(party.getDriverReady().equals(ready)){
            return false;
        }
        party.setDriverReady(ready);
        return true;
    }

    /**
     * (멤버) 파티 레디 or 취소
     */
    public boolean setReadyByMember(Party party, Boolean ready) {
        PartyMember member = partyMemberRepository.findByPartyAndUser(party,
                currentUserService.getCurrentUser())
            .orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
        if(member.getReady().equals(ready)){
            return false;
        }
        member.setReady(ready);
        return true;
    }

    /**
     * 파티 전원 레디 or 레디 취소 처리
     */
    public void setReadyAllMembers(Party party, Boolean ready) {
        getMembers(party).stream()
            .forEach(member -> member.setReady(ready));
        party.setDriverReady(ready);
    }


    /**
     * 파티 전원 레디 확인
     */
    public Boolean isEveryoneReady(Party party) {
        if (!party.getDriverReady()) {
            return false;
        }
        return partyMemberRepository.isEveryoneReady(party.getId());
    }

    /**
     * 파티 총 인원 수 조회
     */
    public Integer getTotalHeadcount(Party party) {
        int headcount = 0;
        for (PartyMember member : getMembers(party)) {
            headcount += member.getHeadcount();
        }
        return headcount;
    }

    /**
     * 마지막 멤버인지 확인
     */
    public Boolean isLastMember(Party party) {
        if (getMembers(party).size() == 1) {
            return true;
        } else {
            return false;
        }
    }
}
