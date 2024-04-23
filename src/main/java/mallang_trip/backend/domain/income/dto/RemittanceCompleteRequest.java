package mallang_trip.backend.domain.income.dto;

import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RemittanceCompleteRequest {

	@ApiModelProperty(value = "거래 일자(YYYY-MM-DD)", required = true)
	private LocalDate remittedAt;

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
