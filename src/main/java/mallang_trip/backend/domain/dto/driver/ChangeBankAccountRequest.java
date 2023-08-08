package mallang_trip.backend.domain.dto.driver;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangeBankAccountRequest {

    private String bank;
    private String accountHolder;
    private String accountNumber;
}
