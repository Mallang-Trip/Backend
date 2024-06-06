package mallang_trip.backend.domain.kakao.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate;

@Getter
@Builder
public class AlimTalkRequest {

	private String plusFriendId;
	private String templateCode;
	private List<Message> messages;

	@Getter
	@Builder
	public static class Message {
		private String to;
		private String content;
		private List<Button> buttons;
	}

	@Getter
	@Builder
	public static class Button {
		private String type;
		private String name;
		private String linkMobile;
		private String linkPc;
	}

	public static AlimTalkRequest of(AlimTalkTemplate template, String to, String content, Long partyId){
		Button button = Button.builder()
			.type("WL")
			.name("홈페이지")
			.linkMobile("https://mallangtrip.com/party/detail/" + partyId)
			.linkPc("https://mallangtrip.com/party/detail/" + partyId)
			.build();
		Message message = Message.builder()
			.to(to)
			.content(content)
			.buttons(List.of(button))
			.build();
		return AlimTalkRequest.builder()
			.plusFriendId("@말랑트립")
			.templateCode(template.getTemplateCode())
			.messages(List.of(message))
			.build();
	}
}
