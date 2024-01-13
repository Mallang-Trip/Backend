package mallang_trip.backend.service.chat;

import static mallang_trip.backend.constant.ChatRoomType.COUPLE;
import static mallang_trip.backend.constant.ChatRoomType.GROUP;
import static mallang_trip.backend.constant.ChatRoomType.PARTY_PRIVATE;
import static mallang_trip.backend.constant.ChatRoomType.PARTY_PUBLIC;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_CHATROOM_MEMBER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.ChatRoomType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.chat.ChatRoomBriefResponse;
import mallang_trip.backend.domain.dto.chat.ChatRoomDetailsResponse;
import mallang_trip.backend.domain.dto.user.UserBriefResponse;
import mallang_trip.backend.domain.entity.chat.ChatMessage;
import mallang_trip.backend.domain.entity.chat.ChatRoom;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.chat.ChatMemberRepository;
import mallang_trip.backend.repository.chat.ChatRoomRepository;
import mallang_trip.backend.repository.party.PartyMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

	private final ChatMessageService chatMessageService;
	private final ChatMemberService chatMemberService;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMemberRepository chatMemberRepository;

	/**
	 * 채팅방 생성
	 */
	public ChatRoom createChatRoom(ChatRoomType type, String roomName, Party party) {
		return chatRoomRepository.save(ChatRoom.builder()
			.roomName(roomName)
			.type(type)
			.party(party)
			.build());
	}

	/**
	 * 유저의 채팅방 목록 조회.
	 * 내가 속한 파티의 공용 채팅방은 제외.
	 */
	public List<ChatRoom> getChatRooms(User user) {
		return chatMemberRepository.findByUserAndActive(user, true).stream()
			.map(member -> member.getChatRoom())
			.filter(room -> !isMyPartyPublicChatRoom(room, user))
			.collect(Collectors.toList());
	}

	/**
	 * 내가 속한 파티의 공용 채팅방인지 확인
	 */
	public Boolean isMyPartyPublicChatRoom(ChatRoom room, User user) {
		return room.getType().equals(PARTY_PUBLIC)
			&& chatMemberService.isMyParty(user, room.getParty());
	}

	/**
	 * GROUP ChatRoom 이름 변경
	 */
	public void changeChatRoomName(ChatRoom room, User user, String roomName){
		if (!chatMemberRepository.existsByChatRoomAndUser(room, user)) {
			throw new BaseException(NOT_CHATROOM_MEMBER);
		}
		if (!room.getType().equals(GROUP)) {
			throw new BaseException(Forbidden);
		}
		room.setRoomName(roomName);
	}

	/**
	 * 채팅방 대표 이미지 조회
	 */
	private String getChatRoomImage(ChatRoom room, User user) {
		ChatRoomType type = room.getType();
		if (type.equals(COUPLE)) {
			return chatMemberService.getOtherUserInCoupleChatRoom(room, user).getProfileImage();
		} else if (type.equals(PARTY_PUBLIC) || type.equals(PARTY_PRIVATE)) {
			List<String> images = room.getParty().getCourse().getImages();
			return images == null ? null : images.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 채팅방 이름 조회
	 */
	private String getChatRoomName(ChatRoom room, User user) {
		ChatRoomType type = room.getType();
		if (type.equals(COUPLE)) {
			return chatMemberService.getOtherUserInCoupleChatRoom(room, user).getNickname();
		} else if (type.equals(PARTY_PUBLIC) || type.equals(PARTY_PRIVATE)) {
			return room.getParty().getCourse().getName();
		} else {
			return room.getRoomName();
		}
	}

	/**
	 * PRIVATE ChatRoom에 해당하는 PUBLIC ChatRoom 찾기
	 */
	public ChatRoom getPublicRoomByPrivateRoom(ChatRoom privateRoom) {
		if (privateRoom.getType().equals(PARTY_PRIVATE)) {
			return chatRoomRepository.findByPartyAndType(privateRoom.getParty(), PARTY_PUBLIC)
				.orElse(null);
		} else {
			return null;
		}
	}

	/**
	 * ChatRoom -> ChatRoomBriefResponse 조회
	 */
	public ChatRoomBriefResponse toBriefResponse(ChatRoom chatRoom, User user){
		if(chatRoom.getType().equals(PARTY_PRIVATE)){
			return privateChatRoomToBriefResponse(chatRoom, user);
		} else {
			return notPrivateChatRoomToBriefResponse(chatRoom, user);
		}
	}

	/**
	 * PARTY_PRIVATE 아닌 ChatRoom -> ChatRoomBriefResponse
	 */
	private ChatRoomBriefResponse notPrivateChatRoomToBriefResponse(ChatRoom chatRoom, User user) {
		ChatMessage lastMessage = chatMessageService.getLastMessage(chatRoom, user);
		return ChatRoomBriefResponse.builder()
			.chatRoomId(chatRoom.getId())
			.type(chatRoom.getType())
			.roomName(getChatRoomName(chatRoom, user))
			.image(getChatRoomImage(chatRoom, user))
			.content(chatMessageService.getContentOfLastMessage(lastMessage))
			.headCount(chatMemberService.countChatMembers(chatRoom))
			.unreadCount(chatMemberService.getUnreadCount(chatRoom, user))
			.updatedAt(lastMessage == null ? chatRoom.getUpdatedAt() : lastMessage.getCreatedAt())
			.build();
	}

	/**
	 * PARTY_PRIVATE ChatRoom -> ChatRoomBriefResponse
	 */
	private ChatRoomBriefResponse privateChatRoomToBriefResponse(ChatRoom privateRoom, User user) {
		ChatRoom publicRoom = getPublicRoomByPrivateRoom(privateRoom);
		ChatMessage lastMessage = chatMessageService.getLastMessageOfTwoChatRoom(privateRoom,
			publicRoom, user);
		return ChatRoomBriefResponse.builder()
			.chatRoomId(privateRoom.getId())
			.type(privateRoom.getType())
			.roomName(privateRoom.getRoomName())
			.image(getChatRoomImage(privateRoom, user))
			.content(chatMessageService.getContentOfLastMessage(lastMessage))
			.headCount(chatMemberService.countChatMembers(privateRoom))
			.unreadCount(chatMemberService.getUnreadCount(privateRoom, user)
				+ chatMemberService.getUnreadCount(publicRoom, user))
			.updatedAt(
				lastMessage == null ? privateRoom.getUpdatedAt() : lastMessage.getCreatedAt())
			.build();
	}

	public ChatRoomDetailsResponse toDetailResponse(ChatRoom room, User user){
		ChatRoom publicRoom = getPublicRoomByPrivateRoom(room);
		List<UserBriefResponse> members = chatMemberService.getChatMembers(room).stream()
			.map(member -> UserBriefResponse.of(member.getUser()))
			.collect(Collectors.toList());
		return ChatRoomDetailsResponse.builder()
			.chatRoomId(room.getId())
			.type(room.getType())
			.publicRoomId(publicRoom == null ? null : publicRoom.getId())
			.partyId(room.getParty().getId())
			.myParty(chatMemberService.isMyParty(user, room.getParty()))
			.headCount(chatMemberService.countChatMembers(room))
			.roomName(getChatRoomName(room, user))
			.members(members)
			.messages(chatMessageService.getChatMessages(room, user))
			.build();
	}
}
