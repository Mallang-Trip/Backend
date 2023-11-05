package mallang_trip.backend.service;

import java.util.Random;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dao.SmsCertification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SmsService {

    private final SmsCertification smsCertification;

    public void sendCertification(String phoneNumber){
        String code = createCode();
        smsCertification.createSmsCertification(phoneNumber, code);
        // send message with code
    }

    public Boolean verifyCode(String phoneNumber, String code){
        if(smsCertification.hasKey(phoneNumber) && smsCertification.getSmsCertification(phoneNumber).equals(code)){
            smsCertification.removeSmsCertification(phoneNumber);
            return true;
        } else return false;
    }

    private String createCode() {
        StringBuffer code = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            code.append((rnd.nextInt(10)));
        }
        return code.toString();
    }
}
