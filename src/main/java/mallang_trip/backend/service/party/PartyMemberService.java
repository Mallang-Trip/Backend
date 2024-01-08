package mallang_trip.backend.service.party;

import static mallang_trip.backend.controller.io.BaseResponseStatus.ALREADY_PARTY_MEMBER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_PARTY_MEMBER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.party.PartyMemberResponse;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.party.PartyMemberRepository;
import mallang_trip.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyMemberService {

    private final UserService userService;
    private final PartyMemberRepository partyMemberRepository;

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
     * 파티 멤버 추가 및 파티 headcount 증가
     */
    public PartyMember createMember(Party party, User user, Integer headcount) {
        if (partyMemberRepository.existsByPartyAndUser(party, user)) {
            throw new BaseException(ALREADY_PARTY_MEMBER);
        }
        party.increaseHeadcount(headcount);
        return partyMemberRepository.save(PartyMember.builder()
            .party(party)
            .user(user)
            .headcount(headcount)
            .build());
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
    public void setReady(Party party, Boolean ready){
        Role role = userService.getCurrentUser().getRole();
        if (role.equals(Role.ROLE_DRIVER)) {
            setReadyByDriver(party, ready);
        }
        if (role.equals(Role.ROLE_USER)) {
            setReadyByMember(party, ready);
        }
    }

    /**
     * (드라이버) 파티 레디 or 취소
     */
    public void setReadyByDriver(Party party, Boolean ready){
        if(!party.getDriver().getUser().equals(userService.getCurrentUser())){
            throw new BaseException(NOT_PARTY_MEMBER);
        }
        party.setDriverReady(ready);
    }

    /**
     * (멤버) 파티 레디 or 취소
     */
    public void setReadyByMember(Party party, Boolean ready) {
        PartyMember member = partyMemberRepository.findByPartyAndUser(party,
                userService.getCurrentUser())
            .orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
        member.setReady(ready);
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
        if(!party.getDriverReady()){
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
