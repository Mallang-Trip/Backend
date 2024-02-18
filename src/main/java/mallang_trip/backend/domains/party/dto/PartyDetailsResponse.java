package mallang_trip.backend.domains.party.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.party.constant.PartyStatus;
import mallang_trip.backend.domains.reservation.dto.ReservationResponse;
import mallang_trip.backend.domains.course.dto.CourseDetailsResponse;

@Getter
@Builder
public class PartyDetailsResponse {

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
}
