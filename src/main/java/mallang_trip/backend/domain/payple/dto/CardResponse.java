package mallang_trip.backend.domain.payple.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.payple.entity.Card;

@Getter
@Builder
public class CardResponse {

	private Long cardId;
	private String cardNumber;
	private String cardName;

	/**
	 * Card 객체로 CardResponse 객체를 생성합니다.
	 *
	 * @param card 사용할 Card 객체
	 * @return 생성된 CardResponse 객체
	 */
	public static CardResponse of(Card card){
		return CardResponse.builder()
			.cardId(card.getId())
			.cardNumber(card.getCardNumber())
			.cardName(card.getCardName())
			.build();
	}
}
