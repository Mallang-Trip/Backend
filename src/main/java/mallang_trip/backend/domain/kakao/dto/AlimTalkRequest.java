package mallang_trip.backend.domain.kakao.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate;

@Getter
@Builder
public class AlimTalkRequest {

	private String plusFriendId;
	private String templateCode;
	private Messages messages;

	@Getter
	@Builder
	public class Messages{
		private String to;
		private String content;
	}

	public static AlimTalkRequest of(AlimTalkTemplate template, String to, String content){
		Messages messages = Messages.builder()
			.to(to)
			.content(content)
			.build();
		return AlimTalkRequest.builder()
			.plusFriendId("@말랑트립")
			.templateCode(template.getTemplateCode())
			.messages(messages)
			.build();
	}
}
