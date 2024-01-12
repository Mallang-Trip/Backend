package mallang_trip.backend.service.chat;

import static mallang_trip.backend.constant.ChatRoomType.PARTY_PUBLIC;
import static mallang_trip.backend.constant.PartyStatus.DAY_OF_TRAVEL;
import static mallang_trip.backend.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.constant.PartyStatus.SEALED;
import static mallang_trip.backend.constant.PartyStatus.WAITING_COURSE_CHANGE_APPROVAL;
import static mallang_trip.backend.constant.PartyStatus.WAITING_JOIN_APPROVAL;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_KICK_CHAT_MEMBER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CHATROOM_EXIT_FORBIDDEN;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_CHATROOM_MEMBER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.PartyStatus;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.chat.ChatMessageResponse;
import mallang_trip.backend.domain.entity.chat.ChatMember;
import mallang_trip.backend.domain.entity.chat.ChatRoom;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.chat.ChatMemberRepository;
import mallang_trip.backend.service.party.PartyService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMemberService {

	private final ChatMemberRepository chatMemberRepository;
	private final PartyService partyService;
	private final ChatMessageService chatMessageService;
	private final SimpMessagingTemplate template;

	/**
	 * 채팅방 멤버 생성
	 */
	public ChatMember createChatMember(ChatRoom room, User user){
		return chatMemberRepository.save(ChatMember.builder()
			.chatRoom(room)
			.user(user)
			.build());
	}

	/**
	 * 채팅방 멤버 조회
	 */
	public List<ChatMember> getChatMembers(ChatRoom room){
		return chatMemberRepository.findByChatRoom(room);
	}

	/**
	 * 채팅방 멤버 수 COUNT
	 */
	public int countChatMembers(ChatRoom room){
		return chatMemberRepository.countByChatRoom(room);
	}

	/**
	 * COUPLE ChatRoom에서 상대방 유저 조회
	 */
	public User getOtherUserInCoupleChatRoom(ChatRoom room, User user){
		return chatMemberRepository.findByChatRoom(room).stream()
			.map(ChatMember::getUser)
			.filter(memberUser -> !memberUser.equals(user))
			.findFirst()
			.orElse(null);
	}

	/**
	 * 채팅방 모든 멤버 활성화
	 */
	public void activeAllMembers(ChatRoom room){
		chatMemberRepository.findByChatRoom(room)
			.stream().forEach(member -> member.setActiveTrue());
	}

	/**
	 * 유저를 제외한 채팅방 모든 멤버 unread++
	 */
	public void increaseAllMembersUnreadCount(ChatRoom room, User user){
		chatMemberRepository.findByChatRoom(room).stream()
			.filter(member -> !member.getUser().equals(user))
			.forEach(member -> member.increaseUnreadCount());
	}

	/**
	 * unreadCount 조회
	 */
	public int getUnreadCount(ChatRoom room, User user){
		ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user)
			.orElseThrow(() -> new BaseException(NOT_CHATROOM_MEMBER));
		return member.getUnreadCount();
	}

	/**
	 * COUPLE ChatRoom 나가기
	 */
	public void leaveCoupleChatRoom(ChatRoom room, User user){
		ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user)
			.orElseThrow(() -> new BaseException(NOT_CHATROOM_MEMBER));
		member.setActive(false);
	}

	/**
	 * GROUP ChatRoom 나가기
	 */
	public void leaveGroupChatRoom(ChatRoom room, User user) {
		ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user)
			.orElseThrow(() -> new BaseException(NOT_CHATROOM_MEMBER));
		chatMemberRepository.delete(member);
		// 나가기 메시지 작성
		template.convertAndSend("/sub/room/" + room.getId(),
			ChatMessageResponse.of(chatMessageService.createLeaveMessage(user, room)));
	}

	/**
	 * PARTY_PUBLIC ChatRoom 나가기.
	 * 내가 속한 파티이고, 파티가 진행중이면 나가기 불가.
	 */
	public void leavePartyChatRoom(ChatRoom room, User user) {
		Party party = room.getParty();
		PartyStatus status = party.getStatus();
		if (partyService.isMyParty(user, party) &&
			(status.equals(RECRUITING)
				|| status.equals(WAITING_JOIN_APPROVAL)
				|| status.equals(WAITING_COURSE_CHANGE_APPROVAL)
				|| status.equals(SEALED)
				|| status.equals(DAY_OF_TRAVEL))) {
			throw new BaseException(CHATROOM_EXIT_FORBIDDEN);
		} else {
			leaveGroupChatRoom(room, user);
		}
	}

	/**
	 * 채팅방 강퇴
	 */
	public void kickChatMember(ChatRoom room, User user, User target){
		if(!room.getType().equals(PARTY_PUBLIC)
			|| !partyService.isMyParty(user, room.getParty())
			|| partyService.isMyParty(target, room.getParty())){
			throw new BaseException(CANNOT_KICK_CHAT_MEMBER);
		} else {
			leaveGroupChatRoom(room, target);
		}
	}
}
