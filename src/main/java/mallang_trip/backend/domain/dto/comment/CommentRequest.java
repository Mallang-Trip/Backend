package mallang_trip.backend.domain.dto.comment;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentRequest {

    @NotNull
    private String content;
}
