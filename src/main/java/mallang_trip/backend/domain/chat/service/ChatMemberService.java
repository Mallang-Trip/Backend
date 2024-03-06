package mallang_trip.backend.domain.chat.service;

import static mallang_trip.backend.domain.chat.Exception.ChatExceptionStatus.CANNOT_KICK_CHAT_MEMBER;
import static mallang_trip.backend.domain.chat.Exception.ChatExceptionStatus.CHATROOM_EXIT_FORBIDDEN;
import static mallang_trip.backend.domain.chat.Exception.ChatExceptionStatus.NOT_CHATROOM_MEMBER;
import static mallang_trip.backend.domain.chat.constant.ChatRoomType.COUPLE;
import static mallang_trip.backend.domain.chat.constant.ChatRoomType.PARTY_PUBLIC;
import static mallang_trip.backend.domain.party.constant.PartyStatus.DAY_OF_TRAVEL;
import static mallang_trip.backend.domain.party.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.domain.party.constant.PartyStatus.SEALED;
import static mallang_trip.backend.domain.party.constant.PartyStatus.WAITING_COURSE_CHANGE_APPROVAL;
import static mallang_trip.backend.domain.party.constant.PartyStatus.WAITING_JOIN_APPROVAL;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.party.constant.PartyStatus;
import mallang_trip.backend.domain.chat.repository.ChatMemberRepository;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.chat.dto.ChatMessageResponse;
import mallang_trip.backend.domain.chat.entity.ChatMember;
import mallang_trip.backend.domain.chat.entity.ChatRoom;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyMemberRepository;
import mallang_trip.backend.domain.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMemberService {

	private final ChatMemberRepository chatMemberRepository;
	private final PartyMemberRepository partyMemberRepository;
	private final UserRepository userRepository;
	private final ChatMessageService chatMessageService;
	private final ChatBlockService chatBlockService;
	private final SimpMessagingTemplate template;

	/**
	 * 채팅방 멤버 조회. 없다면 채팅 멤버 생성
	 */
	public ChatMember getOrCreate(ChatRoom room, User user) {
		ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user).orElse(null);
		if (member == null) {
			return chatMemberRepository.save(ChatMember.builder()
				.chatRoom(room)
				.user(user)
				.build());
		} else {
			return member;
		}
	}

	/**
	 * 채팅방 초대
	 */
	public List<User> inviteUsers(ChatRoom room, User currentUser, List<Long> userIds){
		List<User> users = userIds.stream()
			.map(userId -> userRepository.findById(userId)
				.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER)))
			.filter(user -> !chatBlockService.isBlocked(user, currentUser))
			.filter(user -> !chatMemberRepository.existsByChatRoomAndUser(room, user))
			.collect(Collectors.toList());
		users.forEach(user -> getOrCreate(room, user));

		return users;
	}

	/**
	 * 채팅방의 모든 멤버 조회
	 */
	public List<ChatMember> getChatMembers(ChatRoom room) {
		return chatMemberRepository.findByChatRoom(room);
	}

	/**
	 * 채팅방 멤버 수 COUNT
	 */
	public int countChatMembers(ChatRoom room) {
		return chatMemberRepository.countByChatRoom(room);
	}

	/**
	 * COUPLE 채팅방에서 상대방 유저 조회
	 */
	public User getOtherUserInCoupleChatRoom(ChatRoom room, User user) {
		return chatMemberRepository.findByChatRoom(room).stream()
			.map(ChatMember::getUser)
			.filter(memberUser -> !memberUser.equals(user))
			.findFirst()
			.orElse(null);
	}

	/**
	 * COUPLE 채팅방에서 상대방을 차단했을 때, 활성화 하지 않음.
	 */
	private boolean shouldSkipActivation(ChatMember member) {
		ChatRoom chatRoom = member.getChatRoom();
		User currentUser = member.getUser();
		if (chatRoom.getType().equals(COUPLE) &&
			chatBlockService.isBlocked(currentUser, getOtherUserInCoupleChatRoom(chatRoom, currentUser))) {
			return true;
		}
		return false;
	}

	/**
	 * 채팅방 모든 멤버 활성화
	 */
	public void activeAllMembers(ChatRoom room) {
		chatMemberRepository.findByChatRoom(room)
			.stream().forEach(member -> {
				if (!shouldSkipActivation(member)) {
					member.setActiveTrue();
				}
			});
	}

	/**
	 * 유저를 제외한 채팅방 모든 멤버 unread++
	 */
	public void increaseAllMembersUnreadCount(ChatRoom room, User user) {
		chatMemberRepository.findByChatRoom(room).stream()
			.filter(member -> !member.getUser().equals(user))
			.forEach(member -> member.increaseUnreadCount());
	}

	/**
	 * unreadCount 조회
	 */
	public int getUnreadCount(ChatRoom room, User user) {
		ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user)
			.orElseThrow(() -> new BaseException(NOT_CHATROOM_MEMBER));
		return member.getUnreadCount();
	}

	/**
	 * COUPLE 채팅방 나가기 (active status -> false)
	 */
	public void leaveCoupleChatRoom(ChatRoom room, User user) {
		ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user)
			.orElseThrow(() -> new BaseException(NOT_CHATROOM_MEMBER));
		member.setActiveFalse();
	}

	/**
	 * 나가기 메시지와 함께 채팅방 나가기
	 */
	public void leaveChatRoomWithMessage(ChatRoom room, User user) {
		chatMemberRepository.findByChatRoomAndUser(room, user)
			.ifPresent(member -> {
				template.convertAndSend("/sub/room/" + room.getId(),
					ChatMessageResponse.of(chatMessageService.createLeaveMessage(user, room)));
				chatMemberRepository.delete(member);
			});
	}

	/**
	 * PARTY 채팅방 나가기. 내가 속한 파티이고, 파티가 진행중이면 나가기 불가.
	 */
	public void leavePartyChatRoom(ChatRoom room, User user) {
		Party party = room.getParty();
		PartyStatus status = party.getStatus();
		if (isMyParty(user, party) &&
			(status.equals(RECRUITING)
				|| status.equals(WAITING_JOIN_APPROVAL)
				|| status.equals(WAITING_COURSE_CHANGE_APPROVAL)
				|| status.equals(SEALED)
				|| status.equals(DAY_OF_TRAVEL))) {
			throw new BaseException(CHATROOM_EXIT_FORBIDDEN);
		} else {
			leaveChatRoomWithMessage(room, user);
		}
	}

	/**
	 * 채팅방 강퇴
	 */
	public void kickChatMember(ChatRoom room, User user, User target) {
		if (!room.getType().equals(PARTY_PUBLIC)
			|| !isMyParty(user, room.getParty())
			|| isMyParty(target, room.getParty())) {
			throw new BaseException(CANNOT_KICK_CHAT_MEMBER);
		}
		leaveChatRoomWithMessage(room, target);
	}

	public Boolean isChatMember(User user, ChatRoom room){
		return chatMemberRepository.existsByChatRoomAndUser(room, user);
	}

	public Boolean isMyParty(User user, Party party) {
		if (user == null) {
			return false;
		}
		if (user.equals(party.getDriver().getUser())) {
			return true;
		}
		return partyMemberRepository.existsByPartyAndUser(party, user);
	}
}
