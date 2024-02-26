package mallang_trip.backend.domain.identification.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Conflict;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.identification.dto.IdentificationRequest;
import mallang_trip.backend.domain.identification.dto.ImpUidResponse;
import mallang_trip.backend.domain.identification.dto.UserIdentificationRequest;
import mallang_trip.backend.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class IdentificationService {

    private final PortOneIdentificationService portOneIdentificationService;
    private final UserRepository userRepository;

    public ImpUidResponse request(UserIdentificationRequest request){
        if(userRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new BaseException(Conflict);
        }

        IdentificationRequest identificationRequest = IdentificationRequest.builder()
            .name(request.getName())
            .phone(request.getPhoneNumber())
            .birth(request.getBirthday())
            .gender_digit(request.getGenderDigit())
            .carrier(request.getCarrier())
            .is_mvno(request.getIsMvno())
            .company("말랑트립")
            .build();

        return ImpUidResponse.builder()
            .ImpUid(portOneIdentificationService.request(identificationRequest))
            .build();
    }

    public ImpUidResponse confirm(String impUid, String otp){
        return ImpUidResponse.builder()
            .ImpUid(portOneIdentificationService.confirm(impUid, otp))
            .build();
    }
}
