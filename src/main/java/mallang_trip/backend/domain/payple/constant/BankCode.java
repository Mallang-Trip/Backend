package mallang_trip.backend.domain.payple.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankCode {
    KB국민("KB국민","004"),
    전북("전북","012"),
    대구("대구","031"),
    케이뱅크("케이뱅크","089"),
    신한("신한","088"),
    신협("신협","048"),
    수협("수협","007"),
    경남("경남","039"),
    우리("우리","020"),
    IBK기업("IBK기업","003"),
    제주("제주","035"),
    부산("부산","032"),
    토스뱅크("토스뱅크","092"),
    시티("시티","027"),
    우체국("우체국","071"),
    광주("광주","034"),
    카카오뱅크("카카오뱅크","090"),
    NH농협("NH농협","011"),
    새마을금고("새마을금고","045"),
    KDB산업("KDB산업","002"),
    하나("하나","081"),
    SC제일("SC제일","023"),
    저축("저축","050");

    private final String bank;
    private final String code;

    public static String getCode(String bank){
        return BankCode.valueOf(bank).getCode();

    }

}
