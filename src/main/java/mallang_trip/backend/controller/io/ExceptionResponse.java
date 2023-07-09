package mallang_trip.backend.controller.io;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ExceptionResponse {
    private final String message;

    @Builder
    public ExceptionResponse(String message) {
        this.message = message;
    }
}
