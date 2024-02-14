package mallang_trip.backend.domain.dto.payment;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodsResponse {

    private List<Card> cards;
    private List<Account> accounts;
    private boolean isIdentified;
    private String selectedMethodId;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Card {
        private String id;
        private String alias;
        private String methodKey;
        private String cardName;
        private String cardNumber;
        private String issuerCode;
        private String acquirerCode;
        private String ownerType;
        private String cardType;
        private Integer installmentMinimumAmount;
        private String registeredAt;
        private String status;
        private String icon;
        private String iconUrl;
        private String cardImgUrl;
        private Color color;
        private List<Promotion> promotions;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Account {
        private String id;
        private String methodKey;
        private String accountName;
        private String accountNumber;
        private String alias;
        private String bankCode;
        private String icon;
        private String iconUrl;
        private String registeredAt;
        private String status;
        private Color color;
        private List<Promotion> promotions;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Color {
        private String background;
        private String text;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Promotion {
        private String payType;
        private String type;
        private CardDiscount cardDiscount;
        private CardInterestFree cardInterestFree;
        private CardPoint cardPoint;
        private BankDiscount bankDiscount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardDiscount {
        private String issuerCode;
        private Integer discountAmount;
        private Integer minimumPaymentAmount;
        private Integer maximumPaymentAmount;
        private String currency;
        private String discountCode;
        private String dueDate;
        private Integer balance;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardInterestFree {
        private String issuerCode;
        private String dueDate;
        private List<Integer> installmentFreeMonths;
        private String currency;
        private Integer minimumPaymentAmount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardPoint {
        private String issuerCode;
        private Integer minimumPaymentAmount;
        private String currency;
        private String dueDate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankDiscount {
        private String bankCode;
        private String currency;
        private Integer discountAmount;
        private Integer balance;
        private String discountCode;
        private String dueDate;
        private Integer minimumPaymentAmount;
        private Integer maximumPaymentAmount;
    }
}


