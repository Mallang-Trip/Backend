package mallang_trip.backend.domains.driver.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.driver.entity.DriverReview;

@Getter
@Builder
public class DriverReviewResponse {

    private Long reviewId;
    private Long userId;
    private String nickname;
    private String profileImg;
    private String content;
    private Double rate;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DriverReviewResponse of(DriverReview review){
        return DriverReviewResponse.builder()
            .reviewId(review.getId())
            .userId(review.getUser().getId())
            .nickname(review.getUser().getNickname())
            .profileImg(review.getUser().getProfileImage())
            .content(review.getContent())
            .rate(review.getRate())
            .images(review.getImages())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
    }
}
