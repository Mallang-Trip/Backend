package mallang_trip.backend.domain.chat.service;

import static mallang_trip.backend.domain.admin.exception.AdminExceptionStatus.SUSPENDING;
import static mallang_trip.backend.domain.chat.Exception.ChatExceptionStatus.CANNOT_FOUND_CHATROOM;
import static mallang_trip.backend.domain.chat.Exception.ChatExceptionStatus.NOT_CHATROOM_MEMBER;
import static mallang_trip.backend.domain.chat.constant.ChatRoomType.COUPLE;
import static mallang_trip.backend.domain.chat.constant.ChatRoomType.GROUP;
import static mallang_trip.backend.domain.chat.constant.ChatRoomType.PARTY_PRIVATE;
import static mallang_trip.backend.domain.chat.constant.ChatRoomType.PARTY_PUBLIC;
import static mallang_trip.backend.domain.party.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.CANNOT_FOUND_PARTY;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.PARTY_NOT_RECRUITING;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.chat.constant.ChatRoomType;
import mallang_trip.backend.domain.chat.repository.ChatMemberRepository;
import mallang_trip.backend.domain.chat.repository.ChatRoomRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.chat.dto.ChatMessageRequest;
import mallang_trip.backend.domain.chat.dto.ChatMessageResponse;
import mallang_trip.backend.domain.chat.dto.ChatRoomBriefResponse;
import mallang_trip.backend.domain.chat.dto.ChatRoomDetailsResponse;
import mallang_trip.backend.domain.chat.dto.ChatRoomIdResponse;
import mallang_trip.backend.domain.chat.entity.ChatMember;
import mallang_trip.backend.domain.chat.entity.ChatMessage;
import mallang_trip.backend.domain.chat.entity.ChatRoom;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import mallang_trip.backend.domain.user.repository.UserRepository;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.domain.user.service.UserService;
import mallang_trip.backend.domain.party.service.PartyMemberService;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final CurrentUserService currentUserService;
    private final PartyMemberService partyMemberService;
    private final ChatRoomService chatRoomService;
    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;
    private final SuspensionService suspensionService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final PartyRepository partyRepository;
    private final SimpMessagingTemplate template;

    /**
     * 파티 전용 채팅방 시작
     */
    public void startPartyChat(Party party) {
        startPartyChat(party, PARTY_PRIVATE);
        startPartyChat(party, PARTY_PUBLIC);
    }

    /**
     * 파티 전용 채팅방 생성 + 파티원, 드라이버 초대
     */
    private void startPartyChat(Party party, ChatRoomType type) {
        ChatRoom room = chatRoomService.createChatRoom(type, null, party);
        partyMemberService.getMembersAndDriver(party)
            .forEach(user -> chatMemberService.getOrCreate(room, user));
        chatMessageService.createStartPartyMessage(room);
        chatMemberService.activeAllMembers(room);
    }

    /**
     * PARTY_PRIVATE, PARTY_PUBLIC 채팅방 입장 PARTY_PRIVATE ChatRoom 반환
     */
    public ChatRoom joinPartyChat(User user, Party party) {
        joinPartyChat(user, party, PARTY_PUBLIC);
        return joinPartyChat(user, party, PARTY_PRIVATE);
    }

    /**
     * 파티 채팅방 입장 이미 가입되어 있다면 해당 ChatRoom 반환
     */
    private ChatRoom joinPartyChat(User user, Party party, ChatRoomType type) {
        ChatRoom room = chatRoomRepository.findByPartyAndType(party, type)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_CHATROOM));
        if (!chatMemberService.isChatMember(user, room)) {
            // 채팅 멤버 생성
            chatMemberService.getOrCreate(room, user).setActiveTrue();
            // 입장 알림 메시지 생성
            template.convertAndSend("/sub/room/" + room.getId(),
                ChatMessageResponse.of(chatMessageService.createEnterMessage(user, room)));
        }
        return room;
    }

    /**
     * 파티 탈퇴 시 PARTY_PRIVATE ChatRoom 강퇴
     */
    public void leavePrivateChatWhenLeavingParty(User user, Party party) {
        chatRoomRepository.findByPartyAndType(party, PARTY_PRIVATE)
            .ifPresent(room -> {
                chatMemberService.leaveChatRoomWithMessage(room, user);
            });
    }

    /**
     * 파티 채팅방 입장하기
     */
    public ChatRoomIdResponse enterPartyChatRoom(Long partyId) {
        User user = currentUserService.getCurrentUser();
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
        if (chatMemberService.isMyParty(user, party)) {
            return enterPartyPrivateChatRoom(party, user);
        } else {
            return enterPartyPublicChatRoom(party, user);
        }
    }


    /**
     * 내 파티의 채팅방 입장
     */
    private ChatRoomIdResponse enterPartyPrivateChatRoom(Party party, User user) {
        ChatRoom privateRoom = joinPartyChat(user, party);
        return ChatRoomIdResponse.builder().chatRoomId(privateRoom.getId()).build();
    }

    /**
     * PARTY_PUBLIC 채팅방 입장 및 조회
     */
    private ChatRoomIdResponse enterPartyPublicChatRoom(Party party, User user) {
        // 정지된 유지언지 CHECK
        if (suspensionService.isSuspending(user)) {
            throw new BaseException(SUSPENDING);
        }
        // 파티 status CHECK
        if (!party.getStatus().equals(RECRUITING)) {
            throw new BaseException(PARTY_NOT_RECRUITING);
        }
        // PARTY_PUBLIC 채팅방 입장 및 조회
        ChatRoom publicRoom = joinPartyChat(user, party, PARTY_PUBLIC);

        return ChatRoomIdResponse.builder().chatRoomId(publicRoom.getId()).build();
    }

    /**
     * 채팅방 강퇴
     */
    public void kickChatMember(Long chatRoomId, Long targetUserId) {
        User user = currentUserService.getCurrentUser();
        User target = userRepository.findById(targetUserId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_CHATROOM));

        chatMemberService.kickChatMember(room, user, target);
    }

    /**
     * 새로운 그룹 채팅방 생성
     */
    public ChatRoomIdResponse startGroupChat(List<Long> userIds, String roomName) {
        ChatRoom room = chatRoomService.createChatRoom(GROUP, roomName, null);
        User currentUser = currentUserService.getCurrentUser();
        // 정지 유저인지 CHECK
        if (suspensionService.isSuspending(currentUser)) {
            throw new BaseException(SUSPENDING);
        }
        // 자기 자신을 멤버로 추가
        chatMemberService.getOrCreate(room, currentUser);
        // 멤버 초대
        List<User> users = chatMemberService.inviteUsers(room, currentUser, userIds);
        // 멤버 전체 활성화
        chatMemberService.activeAllMembers(room);
        // 초대 메시지 생성
        chatMessageService.createInviteMessage(currentUser, users, room);

        return ChatRoomIdResponse.builder().chatRoomId(room.getId()).build();
    }

    /**
     * 그룹 채팅방 초대
     */
    public void inviteToGroupChat(Long chatRoomId, List<Long> userIds) {
        User currentUser = currentUserService.getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_CHATROOM));
        // 그룹 채팅방이 아닌 경우
        if (!room.getType().equals(GROUP)) {
            throw new BaseException(Forbidden);
        }
        // 초대 권한 CHECK
        if (!chatMemberRepository.existsByChatRoomAndUser(room, currentUser)) {
            throw new BaseException(Forbidden);
        }
        // 멤버 초대
        List<User> users = chatMemberService.inviteUsers(room, currentUser, userIds);
        chatMemberService.activeAllMembers(room);
        // 초대 메시지 SEND
        template.convertAndSend("/sub/room/" + room.getId(), ChatMessageResponse.of(
            chatMessageService.createInviteMessage(currentUser, users, room)));
    }

    /**
     * 1:1 채팅방 생성
     */
    public ChatRoomIdResponse startCoupleChat(Long userId) {
        User user = currentUserService.getCurrentUser();
        User receiver = userRepository.findById(userId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        // 정지인지 CHECK
        if (suspensionService.isSuspending(user)) {
            throw new BaseException(SUSPENDING);
        }
        // 자신을 초대하는 경우
        if (user.equals(receiver)) {
            throw new BaseException(Forbidden);
        }
        // 두 명으로 구성된 1:1 채팅방 탐색
        ChatRoom chatRoom = chatRoomRepository.findExistedChatRoom(user.getId(), receiver.getId());
        // 진행중인 채팅방이 없는 경우
        if (chatRoom == null) {
            ChatRoom newChatRoom = chatRoomService.createChatRoom(COUPLE, null, null);
            chatMemberService.getOrCreate(newChatRoom, user).setActiveTrue();
            chatMemberService.getOrCreate(newChatRoom, receiver);
            return ChatRoomIdResponse.builder().chatRoomId(newChatRoom.getId()).build();
        } else { // 진행중인 채팅방이 존재하는 경우
            chatMemberRepository.findByChatRoomAndUser(chatRoom, user)
                .ifPresent(ChatMember::setActiveTrue);
            return ChatRoomIdResponse.builder().chatRoomId(chatRoom.getId()).build();
        }
    }

    /**
     * 그룹 채팅방 이름 변경
     */
    public void changeGroupChatRoomName(Long roomId, String roomName) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_CHATROOM));
        chatRoomService.changeChatRoomName(room, currentUserService.getCurrentUser(), roomName);
    }

    /**
     * 채팅방 나가기 By chat_room_id
     */
    public void leaveChat(Long chatRoomId) {
        User user = currentUserService.getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_CHATROOM));
        chatRoomService.leaveChat(room, user);
    }

    /**
     * (회원탈퇴) 채팅방 모두 나가기
     */
    public void leaveAllChat(User user) {
        chatRoomService.getChatRooms(user).stream()
            .forEach(room -> chatRoomService.leaveChat(room, user));
    }

    /**
     * 현재 유저의 채팅방 목록 조회
     */
    public List<ChatRoomBriefResponse> getChatRooms() {
        User user = currentUserService.getCurrentUser();
        return getChatRooms(user);
    }

    /**
     * 유저의 채팅방 목록 조회
     */
    private List<ChatRoomBriefResponse> getChatRooms(User user) {
        return chatRoomService.getChatRooms(user).stream()
            .map(room -> chatRoomService.toBriefResponse(room, user))
            .collect(Collectors.toList());
    }

    /**
     * 채팅방 상세조회
     */
    public ChatRoomDetailsResponse getChatRoomDetails(Long chatRoomId) {
        User currentUser = currentUserService.getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_CHATROOM));
        ChatMember currentMember = chatMemberRepository.findByChatRoomAndUser(room, currentUser)
            .orElseThrow(() -> new BaseException(NOT_CHATROOM_MEMBER));
        // 현재 채팅 멤버의 unreadCount 0으로 초기화
        currentMember.setUnreadCountZero();
        // 현재 유저에게 업데이트된 채팅방 리스트 STOMP publish
        sendNewChatRoomList(currentUser);

        return chatRoomService.toDetailResponse(room, currentUser);
    }

    /**
     * STOMP SEND '/pub/write' 처리 로직
     */
    public void handleNewMessage(ChatMessageRequest request,
        StompHeaderAccessor accessor) {
        Long roomId = Long.parseLong(accessor.getFirstNativeHeader("room-id"));
        User currentUser = currentUserService.getCurrentUser(accessor);

        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new MessageDeliveryException("CANNOT_FOUND_CHATROOM"));
        // 모든 멤버 활성화
        chatMemberService.activeAllMembers(room);
        // 채팅 메시지 생성
        ChatMessage message = chatMessageService.create(currentUser, room, request.getType(),
            request.getContent());
        // 멤버 unreadCount++
        chatMemberService.increaseAllMembersUnreadCount(room, currentUser);
		// 채팅 메시지 PUBLISH
		template.convertAndSend("/sub/room/" + roomId, ChatMessageResponse.of(message));
		// 멤버들에게 업데이트된 채팅방 리스트 STOMP publish
		sendNewChatRoomList(chatMemberService.getChatMembers(room));
    }

    /**
     * STOMP SEND '/pub/read' 처리 로직
     */
    public void setUnreadCountZero(StompHeaderAccessor accessor) {
        User currentUser = currentUserService.getCurrentUser(accessor);
        Long roomId = Long.parseLong(accessor.getFirstNativeHeader("room-id"));

        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new MessageDeliveryException("CANNOT_FOUND_CHATROOM"));
        ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, currentUser)
            .orElseThrow(() -> new MessageDeliveryException("NOT_CHAT_MEMBER"));
        // 현재 채팅 멤버의 unreadCount 0으로 초기화
        member.setUnreadCountZero();
        // 현재 유저에게 업데이트된 채팅방 STOMP publish
        sendNewChatRoomList(currentUser);
    }

    /**
     * 유저에게 업데이트된 ChatRoomList STOMP Publish
     */
    private void sendNewChatRoomList(User user){
        template.convertAndSend("/sub/list/" + user.getId(), getChatRooms(user));
    }

    /**
     * 채팅 멤버들에게 업데이트된 ChatRoomList STOMP Publish
     */
    private void sendNewChatRoomList(List<ChatMember> members) {
        members.stream()
            .map(member -> member.getUser())
            .forEach(user -> sendNewChatRoomList(user));
    }

    /**
     * (관리자) 채팅방의 모든 채팅 내역 조회
     */
    public List<ChatMessageResponse> getEntireMessages(Long roomId) {
        return chatRoomService.getEntireMessages(roomId).stream()
            .map(ChatMessageResponse::of)
            .collect(Collectors.toList());
    }

}
