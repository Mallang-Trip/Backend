package mallang_trip.backend.domain.income.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RemittanceCompleteRequest {

	@NotBlank
	@ApiModelProperty(value = "송금 은행", required = true)
	private String senderBank;

	@NotBlank
	@ApiModelProperty(value = "수취 은행", required = true)
	private String receiverBank;

	@NotBlank
	@ApiModelProperty(value = "수취 계좌번호", required = true)
	private String receiverAccountNumber;
}
