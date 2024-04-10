package mallang_trip.backend.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.admin.constant.ObjectionStatus;
import mallang_trip.backend.domain.admin.entity.Objection;


@Getter
@Builder
public class ObjectionDetailsResponse {

    private Long objectionId;
    private Long objectorId;
    private String objectorNickname;
    private String content;
    private ObjectionStatus status;
    private LocalDateTime createdAt;

    public static ObjectionDetailsResponse of(Objection objection){
        return ObjectionDetailsResponse.builder()
                .objectionId(objection.getId())
                .objectorId(objection.getObjector().getId())
                .objectorNickname(objection.getObjector().getNickname())
                .content(objection.getContent())
                .status(objection.getStatus())
                .createdAt(objection.getCreatedAt())
                .build();
    }
}
