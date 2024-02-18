package mallang_trip.backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.payment.entity.Card;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private String issuerCode;
    private String acquirerCode;
    private String number;
    private String cardType;
    private String ownerType;

    public static CardResponse of(Card card){
        return CardResponse.builder()
            .issuerCode(card.getIssuerCode())
            .acquirerCode(card.getAcquirerCode())
            .number(card.getNumber())
            .cardType(card.getCardType())
            .ownerType(card.getOwnerType())
            .build();
    }
}
