package mallang_trip.backend.service.party;

import static mallang_trip.backend.constant.PartyStatus.RECRUITING;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dto.party.PartyBriefResponse;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.repository.party.PartyRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PartySearchService {

    private final PartyRepository partyRepository;

    /**
     * 모집중인 파티 검색 :
     * region == "all" -> 지역 전체 검색.
     */
    public List<PartyBriefResponse> searchRecruitingParties(String region, Integer headcount,
        String startDate, String endDate, Integer maxPrice) {
        List<Party> parties = region.equals("all") ? partyRepository.findByStatus(RECRUITING)
            : partyRepository.findByRegionAndStatus(region, RECRUITING);
        return parties.stream()
            .filter(party -> party.isHeadcountAvailable(headcount))
            .filter(party -> party.checkDate(startDate, endDate))
            .filter(party -> party.checkMaxPrice(maxPrice))
            .map(PartyBriefResponse::of)
            .collect(Collectors.toList());
    }
}
