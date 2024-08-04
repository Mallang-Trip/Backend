package mallang_trip.backend.domain.dreamSecurity.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dreamSecurity.dto.IdentificationResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentificationResultService {

	private static final long EXPIRATION_TIME_MINUTES = 10;

	private final RedisTemplate<String, Object> redisTemplate;

	// 저장
	public void saveIdentificationResult(String key, IdentificationResult identificationResult) {
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		valueOps.set(key, identificationResult, EXPIRATION_TIME_MINUTES, TimeUnit.MINUTES);
	}

	// 조회
	public IdentificationResult getIdentificationResult(String key) {
		ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
		return (IdentificationResult) valueOps.get(key);
	}
}
