package mallang_trip.backend.domain.payple.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardRequest {

	@NotBlank
	@ApiModelProperty(value = "user_id", required = true)
	private String userId;

	@NotBlank
	@ApiModelProperty(value = "빌링키", required = true)
	private String billingKey;

	@NotBlank
	@ApiModelProperty(value = "카드명", required = true)
	private String cardName;

	@NotBlank
	@ApiModelProperty(value = "카드 번호", required = true)
	private String cardNumber;

}
