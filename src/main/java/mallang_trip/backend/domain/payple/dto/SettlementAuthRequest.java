package mallang_trip.backend.domain.payple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementAuthRequest {

    private String cst_id;
    private String custKey;
    private String code;
}
