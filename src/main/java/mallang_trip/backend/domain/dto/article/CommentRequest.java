package mallang_trip.backend.domain.dto.article;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
public class CommentRequest {

    @NotNull
    private String content;
}
