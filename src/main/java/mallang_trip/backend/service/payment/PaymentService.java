package mallang_trip.backend.service.payment;

import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_USER;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.entity.payment.Payment;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.payment.PaymentRepository;
import mallang_trip.backend.repository.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public void save(String code, String customerKey){
        User user = userRepository.findByCustomerKey(customerKey)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        Payment payment = Payment.builder()
            .user(user)
            .code(code)
            .build();
    }
}
