package mallang_trip.backend.domain.dreamSecurity.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdentificationResult {

	private String userName;
	private String ci;
	private String di;
	private String userPhone;
	private LocalDate userBirthday;
	private String userGender;
	private String userNation;
}
