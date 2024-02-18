package mallang_trip.backend.domains.chat.service;

import static mallang_trip.backend.domains.chat.constant.ChatType.IMAGE;
import static mallang_trip.backend.domains.chat.constant.ChatType.INFO;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.chat.dto.ChatMessageResponse;
import mallang_trip.backend.domains.chat.entity.ChatMessage;
import mallang_trip.backend.domains.chat.entity.ChatRoom;
import mallang_trip.backend.domains.user.entity.User;
import mallang_trip.backend.domains.chat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;

	/**
	 * 유저 기준, 채팅방의 마지막 메시지 조회 (INFO 제외)
	 */
	public ChatMessage getLastMessage(ChatRoom room, User user) {
		return chatMessageRepository.getLastMessage(room.getId(), user.getId());
	}

	/**
	 * 유저 기준, 두 채팅방에서 마지막 메시지 조회 (INFO 제외)
	 */
	public ChatMessage getLastMessageOfTwoChatRoom(ChatRoom first, ChatRoom second, User user) {
		ChatMessage firstMessage = getLastMessage(first, user);
		ChatMessage secondMessage = getLastMessage(second, user);
		if (firstMessage == null & secondMessage == null) {
			return null;
		} else if (firstMessage == null) {
			return secondMessage;
		} else if (secondMessage == null) {
			return firstMessage;
		} else {
			return firstMessage.getCreatedAt().isAfter(secondMessage.getCreatedAt()) ?
				firstMessage : secondMessage;
		}
	}

	/**
	 * LastMessage 의 content 조회
	 */
	public String getContentOfLastMessage(ChatMessage lastMessage) {
		if (lastMessage == null) {
			return "";
		} else if (lastMessage.getType().equals(IMAGE)) {
			return "사진";
		} else {
			return lastMessage.getContent();
		}
	}

	/**
	 * 채팅 초대 메시지 생성 및 저장
	 */
	public ChatMessage createInviteMessage(User inviter, List<User> users, ChatRoom room) {
		List<String> nicknames = users.stream()
			.map(user -> user.getNickname())
			.collect(Collectors.toList());
		String message =
			inviter.getNickname() + "님이 " + String.join(", ", nicknames) + "님을 초대했습니다.";
		return chatMessageRepository.save(ChatMessage.builder()
			.user(inviter)
			.chatRoom(room)
			.type(INFO)
			.content(message)
			.build());
	}

	/**
	 * 유저의 채팅방 가입 이후의 모든 메시지 조회
	 */
	public List<ChatMessageResponse> getChatMessages(ChatRoom room, User user) {
		return chatMessageRepository.findByChatRoomAndUser(room.getId(), user.getId()).stream()
			.map(ChatMessageResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 채팅 나가기 메시지 생성 및 저장
	 */
	public ChatMessage createLeaveMessage(User user, ChatRoom room) {
		String message = user.getNickname() + "님이 나갔습니다.";
		return chatMessageRepository.save(ChatMessage.builder()
			.user(user)
			.chatRoom(room)
			.type(INFO)
			.content(message)
			.build());
	}

	/**
	 * 입장 메시지 생성 및 저장
	 */
	public ChatMessage createEnterMessage(User user, ChatRoom room) {
		String message = user.getNickname() + "님이 참여했습니다.";
		return chatMessageRepository.save(ChatMessage.builder()
			.user(user)
			.chatRoom(room)
			.type(INFO)
			.content(message)
			.build());
	}

	/**
	 * 전체 메시지 조회
	 */
	public List<ChatMessage> getEntireMessages(ChatRoom room){
		return chatMessageRepository.findByChatRoom(room);
	}
}
