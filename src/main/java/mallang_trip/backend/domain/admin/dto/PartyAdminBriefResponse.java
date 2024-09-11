package mallang_trip.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.party.constant.PartyStatus;
import mallang_trip.backend.domain.party.dto.PartyBriefResponse;
import mallang_trip.backend.domain.party.entity.Party;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PartyAdminBriefResponse {

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
    private Boolean promotion; // 프로모션코드 사용자 존재 여부

    public static PartyAdminBriefResponse of(Party party,Boolean promotion){
        Course course = party.getCourse();
        return PartyAdminBriefResponse.builder()
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
                .promotion(promotion)
                .build();
    }
}
