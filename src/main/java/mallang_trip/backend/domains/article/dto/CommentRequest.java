package mallang_trip.backend.domains.article.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CommentRequest {

    @NotNull
    private String content;
}
