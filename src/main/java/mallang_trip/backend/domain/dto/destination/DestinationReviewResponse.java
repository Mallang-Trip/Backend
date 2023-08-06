package mallang_trip.backend.domain.dto.destination;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.destination.DestinationReview;

@Getter
@Builder
public class DestinationReviewResponse {

    private Long reviewId;
    private Long userId;
    private String nickname;
    private String profileImg;
    private String content;
    private Double rate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DestinationReviewResponse of(DestinationReview review){
        return DestinationReviewResponse.builder()
            .reviewId(review.getId())
            .userId(review.getUser().getId())
            .nickname(review.getUser().getNickname())
            .profileImg(review.getUser().getProfileImage())
            .content(review.getContent())
            .rate(review.getRate())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
    }
}
