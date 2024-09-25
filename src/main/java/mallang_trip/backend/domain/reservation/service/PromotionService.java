package mallang_trip.backend.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.reservation.dto.*;
import mallang_trip.backend.domain.reservation.entity.PromotionCode;
import mallang_trip.backend.domain.reservation.entity.UserPromotionCode;
import mallang_trip.backend.domain.reservation.repository.PromotionCodeRepository;
import mallang_trip.backend.domain.reservation.repository.UserPromotionCodeRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static mallang_trip.backend.domain.reservation.constant.UserPromotionCodeStatus.*;
import static mallang_trip.backend.domain.reservation.exception.PromotionCodeExceptionStatus.*;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {

    private final PromotionCodeRepository promotionCodeRepository;
    private final UserPromotionCodeRepository userPromotionCodeRepository;
    private final CurrentUserService currentUserService;

    /**
     * 프로모션 코드 생성
     */
    public void createPromotionCode(PromotionCodeCreateRequest request) {

        PromotionCode promotionCode = promotionCodeRepository.save(PromotionCode.builder()
                .code(request.getCode())
                .free(request.getFree())
                .discountPrice(request.getDiscountPrice())
                .discountRate(request.getDiscountRate())
                .minimumPrice(request.getMinimumPrice())
                .maximumPrice(request.getMaximumPrice())
                .maximumDiscountPrice(request.getMaximumDiscountPrice())
                .endDate(LocalDate.parse(request.getEndDate()))
                .count(request.getCount())
                .build());
    }

    /**
     * 프로모션 코드 전체 조회
     */
    public List<PromotionCodeResponse> getPromotionCodes() {
        return promotionCodeRepository.findAll().stream()
                .map(PromotionCodeResponse::of)
                .collect(Collectors.toList());
    }

    /**
     * 프로모션 코드 수정
     */
    public void updatePromotionCode(Long id, PromotionCodeCreateRequest request) {
        PromotionCode promotionCode = promotionCodeRepository.findById(id)
                .orElseThrow(() -> new BaseException(Not_Found));

        promotionCode.modify(request);
    }

    /**
     * 프로모션 코드 삭제
     *
     */
    public void deletePromotionCode(Long id) {
        PromotionCode promotionCode = promotionCodeRepository.findById(id)
                .orElseThrow(() -> new BaseException(Not_Found));

        promotionCodeRepository.delete(promotionCode);
    }

    /**
     * 프로모션 코드 사용 가능 여부 확인
     */
    public String checkPromotionCode(PromotionCodeCheckRequest request) {
        Optional<PromotionCode> promotionCode = promotionCodeRepository.findByCode(request.getCode());


        if (promotionCode.isEmpty()) {
            throw new BaseException(PROMOTION_CODE_NOT_FOUND);
        }

        if (promotionCode.get().getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(PROMOTION_CODE_EXPIRED);
        }

        if (!promotionCode.get().isAvailable()) {
            throw new BaseException(PROMOTION_CODE_NOT_AVAILABLE);
        }

        if (promotionCode.get().getMinimumPrice() > request.getPrice()) {
            throw new BaseException(PROMOTION_CODE_PRICE_NOT_MATCH);
        }

        if (promotionCode.get().getMaximumPrice() < request.getPrice()) {
            throw new BaseException(PROMOTION_CODE_PRICE_NOT_MATCH);
        }
        Optional<UserPromotionCode> userPromotionCode = userPromotionCodeRepository.findByUserAndCodeAndStatus(currentUserService.getCurrentUser(), promotionCode.get(),USE);

        if (userPromotionCode.isPresent()) {
            throw new BaseException(PROMOTION_CODE_ALREADY_USED);
        }

        return "사용 가능한 코드입니다.";
    }

    /**
     * 프로모션 코드 사용
     */
    public PromotionCodeUseIdResponse usePromotionCode(PromotionCodeUseRequest request) {
        Optional<PromotionCode> promotionCode = promotionCodeRepository.findByCode(request.getCode());

        if (promotionCode.isEmpty()) {
            throw new BaseException(PROMOTION_CODE_NOT_FOUND);
        }

        if (promotionCode.get().getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(PROMOTION_CODE_EXPIRED);
        }

        if (!promotionCode.get().isAvailable()) {
            throw new BaseException(PROMOTION_CODE_NOT_AVAILABLE);
        }

        Optional<UserPromotionCode> usedPromotionCode = userPromotionCodeRepository.findByUserAndCodeAndStatus(currentUserService.getCurrentUser(), promotionCode.get(),USE);

        if (usedPromotionCode.isPresent()) {
            throw new BaseException(PROMOTION_CODE_ALREADY_USED);
        }

        Optional<UserPromotionCode> alreadyTried = userPromotionCodeRepository.findByUserAndCodeAndStatus(currentUserService.getCurrentUser(), promotionCode.get(),TRY);

        if(alreadyTried.isPresent()) {
            return PromotionCodeUseIdResponse.builder()
                .id(alreadyTried.get().getId())
                .build();
        }

        UserPromotionCode userPromotionCode= userPromotionCodeRepository.save(UserPromotionCode.builder()
                .user(currentUserService.getCurrentUser())
                .code(promotionCode.get())
                .status(TRY)
                .build());

        return PromotionCodeUseIdResponse.builder()
                .id(userPromotionCode.getId())
                .build();
    }

    /**
     * 프로모션 코드 사용 취소
     */
    public void cancelPromotionCode(Long id) {
        UserPromotionCode userPromotionCode = userPromotionCodeRepository.findById(id)
                .orElseThrow(() -> new BaseException(Not_Found));

        if(userPromotionCode.getUser() != currentUserService.getCurrentUser()){
            throw new BaseException(Forbidden);
        }
        userPromotionCode.getCode().cancel();
        userPromotionCode.changeStatus(CANCEL);
    }

}
