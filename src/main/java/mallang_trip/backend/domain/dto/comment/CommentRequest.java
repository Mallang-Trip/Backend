package mallang_trip.backend.domain.dto.comment;

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
