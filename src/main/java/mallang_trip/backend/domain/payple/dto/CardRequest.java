package mallang_trip.backend.domain.payple.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardRequest {

	private String PCD_PAY_RST;
	private String PCD_PAY_CODE;
	private String PCD_PAY_MSG;
	private String PCD_PAY_WORK;
	private String PCD_AUTH_KEY;
	private String PCD_PAY_TYPE;
	private String PCD_PAYER_NO;
	private String PCD_PAYER_ID;
	private String PCD_PAYER_NAME;
	private String PCD_PAYER_EMAIL;
	private String PCD_PAYER_HP;
	private String PCD_PAY_CARDNAME;
	private String PCD_PAY_CARDNUM;
	private String PCD_RST_URL;
	private String PCD_USER_DEFINE1;
	private String PCD_USER_DEFINE2;


}
