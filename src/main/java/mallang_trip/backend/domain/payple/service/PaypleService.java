package mallang_trip.backend.domain.payple.service;

import static mallang_trip.backend.domain.payple.exception.PaypleExceptionStatus.CANNOT_FOUND_CARD;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.payple.dto.CardRequest;
import mallang_trip.backend.domain.payple.dto.CardResponse;
import mallang_trip.backend.domain.payple.entity.Card;
import mallang_trip.backend.domain.payple.repository.CardRepository;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaypleService {

	private final CurrentUserService currentUserService;
	private final CardRepository cardRepository;

	/**
	 * 카드정보를 저장합니다.
	 * <p>
	 * 기존 등록된 카드가 있다면 삭제 후 새로운 카드를 저장합니다.
	 *
	 * @param request 카드등록 요청 결과가 담긴 CardRequest 객체
	 * @throws BaseException Forbidden 카드등록에 실패했거나 현재 유저와 카드등록 유저가 일치하지 않는 경우 발생하는 예외
	 * @return 카드정보가 담긴 CardResponse 객체
	 */
	public CardResponse register(CardRequest request){
		User currentUser = currentUserService.getCurrentUser();
		if(!currentUser.getId().equals(Long.valueOf(request.getUserId()))){
			throw new BaseException(Forbidden);
		}
		// 기존 카드정보 삭제
		delete(currentUser);
		// 카드정보 저장
		Card card = cardRepository.save(Card.builder()
			.user(currentUser)
			.billingKey(request.getBillingKey())
			.cardNumber(request.getCardNumber())
			.cardName(request.getCardName())
			.build());

		return CardResponse.of(card);
	}

	/**
	 * 현재 유저의 등록된 카드가 존재한다면 카드정보를 삭제(soft delete)합니다.
	 */
	public void delete(){
		delete(currentUserService.getCurrentUser());
	}

	/**
	 * 유저의 등록된 카드가 존재한다면 카드 정보를 삭제(soft delete)합니다.
	 *
	 * @param user 카드 정보를 삭제할 User 객체
	 */
	private void delete(User user){
		cardRepository.findByUser(user)
			.ifPresent(card -> cardRepository.delete(card));
	}

	/**
	 * 현재 유저의 등록된 카드 정보를 조회합니다.
	 *
	 * @throws BaseException CANNOT_FOUND_CARD 등록된 카드가 없는 경우 발생하는 예외
	 * @return 등록된 카드 정보가 담긴 CardResponse 객체
	 */
	public CardResponse get(){
		User currentUser = currentUserService.getCurrentUser();
		Optional<Card> card = cardRepository.findByUser(currentUser);
		if(card.isPresent()){
			return CardResponse.of(card.get());
		} else{
			throw new BaseException(CANNOT_FOUND_CARD);
		}
	}

	/**
	 * 자동결제
	 */

	/**
	 * 결제 재시도(수동결제)
	 */

	/**
	 * 결제 취소
	 */
}
