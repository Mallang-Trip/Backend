package mallang_trip.backend.repository.redis;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SmsCertification {

    private final String PREFIX = "sms:";
    private final int LIMIT_TIME = 5 * 60; // 유효시간 5분

    private final StringRedisTemplate stringRedisTemplate;

    // 인증번호 저장
    public void createSmsCertification(String phone, String code) {
        stringRedisTemplate.opsForValue()
            .set(PREFIX + phone, code, Duration.ofSeconds(LIMIT_TIME));
    }

    // 인증번호 조회
    public String getSmsCertification(String phone) {
        return stringRedisTemplate.opsForValue().get(PREFIX + phone);
    }

    // 인증번호 연장
    public void extendSmsCertification(String phone){
        stringRedisTemplate.expire(PREFIX + phone, LIMIT_TIME, TimeUnit.SECONDS);
    }

    // 인증번호 삭제
    public void removeSmsCertification(String phone) { // (5)
        stringRedisTemplate.delete(PREFIX + phone);
    }

    // 인증번호 exist
    public boolean hasKey(String phone) {  //(6)
        return stringRedisTemplate.hasKey(PREFIX + phone);
    }
}
