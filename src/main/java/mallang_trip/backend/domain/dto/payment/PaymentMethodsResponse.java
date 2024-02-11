package mallang_trip.backend.domain.dto.payment;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentMethodsResponse {

    private List<Card> cards;
    private List<Account> accounts;
    private boolean isIdentified;
    private String selectedMethodId;

    @Getter
    @NoArgsConstructor
    public class Card {
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
    class Account {
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
    class Color {
        private String background;
        private String text;
    }

    @Getter
    @NoArgsConstructor
    class Promotion {
        private String payType;
        private String type;
        private CardDiscount cardDiscount; // You can define a class for cardDiscount if needed
        private CardInterestFree cardInterestFree;
        private CardPoint cardPoint; // You can define a class for cardPoint if needed
        private BankDiscount bankDiscount;
    }

    @Getter
    @NoArgsConstructor
    public class CardDiscount {
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
    class CardInterestFree {
        private String issuerCode;
        private String dueDate;
        private List<Integer> installmentFreeMonths;
        private String currency;
        private Integer minimumPaymentAmount;
    }

    @Getter
    @NoArgsConstructor
    public class CardPoint {
        private String issuerCode;
        private Integer minimumPaymentAmount;
        private String currency;
        private String dueDate;
    }

    @Getter
    @NoArgsConstructor
    class BankDiscount {
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


