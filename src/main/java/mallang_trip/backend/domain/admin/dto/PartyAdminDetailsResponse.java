package mallang_trip.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.course.dto.CourseDetailsResponse;
import mallang_trip.backend.domain.party.constant.PartyStatus;
import mallang_trip.backend.domain.party.dto.PartyMemberResponse;
import mallang_trip.backend.domain.party.dto.PartyProposalResponse;
import mallang_trip.backend.domain.reservation.dto.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class PartyAdminDetailsResponse {

    private Long partyId;
    private Boolean myParty;
    private Boolean dibs;
    private PartyStatus partyStatus;
    private Long driverId;
    private String driverName;
    private Boolean driverReady;
    private Integer capacity;
    private Integer headcount;
    private String region;
    private LocalDate startDate;
    private LocalDate endDate;
    private CourseDetailsResponse course;
    private String content;
    private List<PartyMemberResponse> members;
    private Boolean proposalExists;
    private PartyProposalResponse proposal;
    private ReservationResponse reservation;
    private Boolean monopoly;
    private Boolean promotion; // 프로모션코드 사용자 존재 여부
}
