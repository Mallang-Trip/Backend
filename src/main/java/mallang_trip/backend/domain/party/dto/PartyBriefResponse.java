package mallang_trip.backend.domain.party.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.party.constant.PartyStatus;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.party.entity.Party;

@Getter
@Builder
public class PartyBriefResponse {

    private Long partyId;
    private PartyStatus status;
    private String image;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer price;
    private String driverName;
    private Integer headcount;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PartyBriefResponse of(Party party){
        Course course = party.getCourse();
        return PartyBriefResponse.builder()
            .partyId(party.getId())
            .status(party.getStatus())
            .image(course.getImages().isEmpty() ? null : course.getImages().get(0))
            .name(course.getName())
            .startDate(party.getStartDate())
            .endDate(party.getEndDate())
            .price((course.getTotalPrice() - course.getDiscountPrice()) / party.getCapacity())
            .driverName(party.getDriver().getUser().getName())
            .headcount(party.getHeadcount())
            .capacity(party.getCapacity())
            .createdAt(party.getCreatedAt())
            .updatedAt(party.getUpdatedAt())
            .build();
    }

    public static PartyBriefResponse ofCanceled(Party party, PartyStatus status){
        Course course = party.getCourse();
        return PartyBriefResponse.builder()
            .partyId(party.getId())
            .status(status)
            .image(course.getImages().isEmpty() ? null : course.getImages().get(0))
            .name(course.getName())
            .startDate(party.getStartDate())
            .endDate(party.getEndDate())
            .price((course.getTotalPrice() - course.getDiscountPrice()) / party.getCapacity())
            .driverName(party.getDriver().getUser().getName())
            .headcount(party.getHeadcount())
            .capacity(party.getCapacity())
            .createdAt(party.getCreatedAt())
            .updatedAt(party.getUpdatedAt())
            .build();
    }
}
