package mallang_trip.backend;

import static mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate.DRIVER_TRAVELER_LIST;

import java.util.HashMap;
import java.util.Map;
import mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate;
import mallang_trip.backend.domain.kakao.service.AlimTalkService;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class test {

	@Autowired
	private AlimTalkService alimTalkService;

	@Autowired
	private PartyRepository partyRepository;

	@Test
	public void test(){
		Party party = partyRepository.findById(54l).orElse(null);
		alimTalkService.sendTravelerListAlimTalk(party);
	}

}
