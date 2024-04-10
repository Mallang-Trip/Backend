package mallang_trip.backend.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.ObjectionStatus;
import mallang_trip.backend.domain.admin.entity.Objection;


@Getter
@Builder
public class ObjectionBriefResponse {

    private Long objectionId;
    private String objectorNickname;
    private ObjectionStatus status;
    private LocalDateTime createdAt;

    public static ObjectionBriefResponse of(Objection objection){
        return ObjectionBriefResponse.builder()
                .objectionId(objection.getId())
                .objectorNickname(objection.getObjector().getNickname())
                .status(objection.getStatus())
                .createdAt(objection.getCreatedAt())
                .build();
    }
}
