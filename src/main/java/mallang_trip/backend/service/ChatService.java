package mallang_trip.backend.service;

import static mallang_trip.backend.constant.ChatType.INFO;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.User.UserBriefResponse;
import mallang_trip.backend.domain.dto.chat.ChatMessageRequest;
import mallang_trip.backend.domain.dto.chat.ChatMessageResponse;
import mallang_trip.backend.domain.dto.chat.ChatRoomBriefResponse;
import mallang_trip.backend.domain.dto.chat.ChatRoomDetailsResponse;
import mallang_trip.backend.domain.dto.chat.ChatRoomIdResponse;
import mallang_trip.backend.domain.entity.chat.ChatMember;
import mallang_trip.backend.domain.entity.chat.ChatMessage;
import mallang_trip.backend.domain.entity.chat.ChatRoom;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.chat.ChatMemberRepository;
import mallang_trip.backend.repository.chat.ChatMessageRepository;
import mallang_trip.backend.repository.chat.ChatRoomRepository;
import mallang_trip.backend.repository.user.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final SimpMessagingTemplate template;

    // 새로운 그룹 채팅방 생성
    public ChatRoomIdResponse startGroupChat(List<Long> userIds, String roomName) {
        // 채팅방 생성
        ChatRoom room = createChatRoom(true, roomName);
        // 현재 유저 멤버 추가
        createChatMember(room, userService.getCurrentUser()).setActiveTrue();
        // 멤버 초대
        List<User> users = userIds.stream()
            .map(userId -> userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(Not_Found)))
            .filter(user -> !userService.getCurrentUser().equals(user))
            .collect(Collectors.toList());
        users.stream().forEach(user -> createChatMember(room, user));
        // 초대 메시지 생성
        createInviteMessage(users, room);
        return ChatRoomIdResponse.builder().chatRoomId(room.getId()).build();
    }

    // 그룹 채팅방 초대
    public void inviteToGroupChat(Long chatRoomId, List<Long> userIds) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 그룹 채팅방이 아닌 경우
        if (!room.getIsGroup()) {
            throw new BaseException(Forbidden);
        }
        // 초대 권한 CHECK
        if (!chatMemberRepository.existsByChatRoomAndUser(room, userService.getCurrentUser())) {
            throw new BaseException(Forbidden);
        }
        List<User> users = userIds.stream()
            .map(userId -> userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(Not_Found)))
            // 이미 채팅방에 있는 경우 필터링
            .filter(user -> !chatMemberRepository.existsByChatRoomAndUser(room, user))
            .collect(Collectors.toList());
        // chatMember 추가
        users.stream()
            .forEach(user -> createChatMember(room, user));
        // 초대 메시지 작성
        template.convertAndSend("/sub/room/" + room.getId(),
            ChatMessageResponse.of(createInviteMessage(users, room)));
    }

    // 1:1 채팅방 생성
    public ChatRoomIdResponse startCoupleChat(Long userIds) {
        User user = userService.getCurrentUser();
        User receiver = userRepository.findById(userIds)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 자신을 초대하는 경우
        if (user.equals(receiver)) {
            throw new BaseException(Bad_Request);
        }
        // 두 명으로 구성된 1:1 채팅방 탐색
        ChatRoom chatRoom = chatRoomRepository.findExistedChatRoom(user.getId(), receiver.getId());
        // 진행중인 채팅방이 없는 경우
        if (chatRoom == null) {
            // 채팅방 생성
            ChatRoom newChatRoom = createChatRoom(false, null);
            // 멤버 추가
            createChatMember(newChatRoom, user).setActiveTrue();
            createChatMember(newChatRoom, receiver);
            return ChatRoomIdResponse.builder().chatRoomId(newChatRoom.getId()).build();
        } else { // 진행중인 채팅방이 존재하는 경우
            chatMemberRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new BaseException(Not_Found)).setActiveTrue();
            return ChatRoomIdResponse.builder().chatRoomId(chatRoom.getId()).build();
        }
    }

    public void changeGroupChatRoomName(Long roomId, String roomName) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 확인
        if (!chatMemberRepository.existsByChatRoomAndUser(room, userService.getCurrentUser())) {
            throw new BaseException(Unauthorized);
        }
        // 그룹채팅방이 아닌 경우
        if (!room.getIsGroup()) {
            throw new BaseException(Forbidden);
        }
        room.setRoomName(roomName);
    }

    // 채팅방 나가기
    public void leaveChat(Long chatRoomId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(Not_Found));
        if (room.getIsGroup()) {
            leaveGroupChatRoom(room);
        } else {
            leaveCoupleChatRoom(room);
        }
    }

    // 그룹 채팅방 나가기
    private void leaveGroupChatRoom(ChatRoom room) {
        ChatMember member = chatMemberRepository.findByChatRoomAndUser(room,
            userService.getCurrentUser()).orElseThrow(() -> new BaseException(Not_Found));
        chatMemberRepository.delete(member);
        // 나가기 메시지 작성
        template.convertAndSend("/sub/room/" + room.getId(),
            ChatMessageResponse.of(createLeaveMessage(room)));
    }

    // 1:1 채팅방 나가기
    private void leaveCoupleChatRoom(ChatRoom room) {
        ChatMember member = chatMemberRepository.findByChatRoomAndUser(room,
            userService.getCurrentUser()).orElseThrow(() -> new BaseException(Not_Found));
        member.setActive(false);
    }

    // 채팅방 리스트 조회
    public List<ChatRoomBriefResponse> getChatRooms() {
        User user = userService.getCurrentUser();
        return getChatRooms(user);
    }

    private List<ChatRoomBriefResponse> getChatRooms(User user) {
        return chatMemberRepository.findByUserAndActive(user, true).stream()
            // 속한 채팅방 찾기
            .map(chatMember -> chatMember.getChatRoom())
            // 채팅방 -> ChatRoomBriefResponse
            .map(chatRoom -> {
                if (chatRoom.getIsGroup()) {
                    return groupChatToResponse(chatRoom, user);
                } else {
                    return coupleChatToResponse(chatRoom, user);
                }
            }).collect(Collectors.toList());
    }

    // 채팅방 상세 조회
    public ChatRoomDetailsResponse getChatRoomDetails(Long chatRoomId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(Not_Found));
        ChatMember user = chatMemberRepository.findByChatRoomAndUser(room,
            userService.getCurrentUser()).orElseThrow(() -> new BaseException(Not_Found));
        // unreadCount 0 초기화
        user.setUnreadCount(0);
        template.convertAndSend("/sub/list/" + user.getUser().getId(),
            getChatRooms(user.getUser()));
        // 채팅방 이름
        String roomName =
            room.getIsGroup() ? room.getRoomName()
                : getOtherUserInCoupleChat(room, userService.getCurrentUser()).getNickname();
        // 멤버 정보
        List<UserBriefResponse> members = getMembers(room).stream()
            .map(member -> UserBriefResponse.of(member.getUser())).collect(Collectors.toList());
        return ChatRoomDetailsResponse.builder()
            .chatRoomId(room.getId())
            .isGroup(room.getIsGroup())
            .headCount(countMembers(room))
            .roomName(roomName)
            .members(members)
            .messages(getChatMessages(room, userService.getCurrentUser()))
            .build();
    }

    // 새 메시지 handle
    public ChatMessageResponse handleNewMessage(ChatMessageRequest request,
        StompHeaderAccessor accessor) {
        User user = userService.getCurrentUser(accessor.getFirstNativeHeader("access-token"));
        ChatRoom room = chatRoomRepository.findById(
                Long.parseLong(accessor.getFirstNativeHeader("room-id")))
            .orElseThrow(() -> new BaseException(Not_Found));
        // 채팅 저장
        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
            .user(user)
            .chatRoom(room)
            .type(request.getType())
            .content(request.getContent())
            .build());
        // 모든 멤버 visibility 전환
        makeMembersActive(room);
        // 멤버 unreadCount++
        List<ChatMember> members = chatMemberRepository.findByChatRoom(room);
        plusUnreadCount(members);
        // 멤버들에게 업데이트된 채팅방 리스트 send
        sendNewChatRoomList(members);
        return ChatMessageResponse.of(message);
    }

    public void setUnreadCountZero(StompHeaderAccessor accessor) {
        User user = userService.getCurrentUser(accessor.getFirstNativeHeader("access-token"));
        ChatRoom room = chatRoomRepository.findById(
                Long.parseLong(accessor.getFirstNativeHeader("room-id")))
            .orElseThrow(() -> new BaseException(Not_Found));
        ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user)
            .orElseThrow(() -> new BaseException(Not_Found));
        member.setUnreadCount(0);
        template.convertAndSend("/sub/list/" + user.getId(), getChatRooms(user));
    }

    private List<ChatMessageResponse> getChatMessages(ChatRoom room, User user) {
        return chatMessageRepository.findByChatRoomAndUser(room.getId(), user.getId()).stream()
            .map(ChatMessageResponse::of)
            .collect(Collectors.toList());
    }

    private ChatRoom createChatRoom(Boolean isGroup, String roomName) {
        return chatRoomRepository.save(ChatRoom.builder()
            .roomName(roomName)
            .isGroup(isGroup)
            .build());
    }

    private ChatMember createChatMember(ChatRoom room, User user) {
        return chatMemberRepository.save(ChatMember.builder()
            .chatRoom(room)
            .user(user)
            .build());
    }

    private Integer countMembers(ChatRoom room) {
        return chatMemberRepository.countByChatRoom(room);
    }

    private List<ChatMember> getMembers(ChatRoom room) {
        return chatMemberRepository.findByChatRoom(room);
    }

    private ChatMessage getLastMessage(ChatRoom room) {
        return chatMessageRepository.findFirstByChatRoomAndTypeNotOrderByCreatedAtDesc(room, INFO);
    }

    private List<String> getProfileImages(ChatRoom room) {
        List<String> images = getMembers(room).stream()
            .map(member -> member.getUser().getProfileImage())
            .collect(Collectors.toList());
        if (images.size() > 4) {
            images = images.subList(0, 4);
        }
        return images;
    }

    private Integer getUnreadCount(ChatRoom room, User user) {
        ChatMember chatMember = chatMemberRepository.findByChatRoomAndUser(room, user)
            .orElseThrow(() -> new BaseException(Not_Found));
        return chatMember.getUnreadCount();
    }

    private User getOtherUserInCoupleChat(ChatRoom chatRoom, User user) {
        return chatMemberRepository.findByChatRoom(chatRoom).stream()
            .filter(member -> !member.getUser().equals(user))
            .collect(Collectors.toList())
            .get(0).getUser();
    }

    public ChatMessage createInviteMessage(List<User> users, ChatRoom room) {
        List<String> nicknames = users.stream()
            .map(user -> user.getNickname())
            .collect(Collectors.toList());
        User user = userService.getCurrentUser();
        String message = user.getNickname() + "님이 " + String.join(", ", nicknames) + "님을 초대했습니다.";
        return chatMessageRepository.save(ChatMessage.builder()
            .user(user)
            .chatRoom(room)
            .type(INFO)
            .content(message)
            .build());
    }

    private ChatMessage createLeaveMessage(ChatRoom room) {
        User user = userService.getCurrentUser();
        String message = user.getNickname() + "님이 나갔습니다.";
        return chatMessageRepository.save(ChatMessage.builder()
            .user(user)
            .chatRoom(room)
            .type(INFO)
            .content(message)
            .build());
    }

    private void makeMembersActive(ChatRoom room) {
        chatMemberRepository.findByChatRoomAndActive(room, false).stream()
            .forEach(member -> member.setActiveTrue());
    }

    private void plusUnreadCount(List<ChatMember> members) {
        members.stream().forEach(member -> member.plusUnreadCount());
    }

    private void sendNewChatRoomList(List<ChatMember> members) {
        members.stream()
            .map(member -> member.getUser())
            .forEach(user -> template.convertAndSend("/sub/list/" + user.getId(),
                getChatRooms(user)));
    }

    private ChatRoomBriefResponse coupleChatToResponse(ChatRoom chatRoom, User user) {
        User other = getOtherUserInCoupleChat(chatRoom, user);
        ChatMessage lastMessage = getLastMessage(chatRoom);
        List<String> profileImg = other.getProfileImage() == null ? null : List.of(other.getProfileImage());
        return ChatRoomBriefResponse.builder()
            .chatRoomId(chatRoom.getId())
            .isGroup(false)
            .roomName(other.getNickname())
            .profileImages(profileImg)
            .content(lastMessage == null ? "" : lastMessage.getContent())
            .headCount(countMembers(chatRoom))
            .unreadCount(getUnreadCount(chatRoom, user))
            .updatedAt(lastMessage == null ? chatRoom.getUpdatedAt() : lastMessage.getCreatedAt())
            .build();
    }

    private ChatRoomBriefResponse groupChatToResponse(ChatRoom chatRoom, User user) {
        ChatMessage lastMessage = getLastMessage(chatRoom);
        return ChatRoomBriefResponse.builder()
            .chatRoomId(chatRoom.getId())
            .isGroup(true)
            .roomName(chatRoom.getRoomName())
            .profileImages(getProfileImages(chatRoom))
            .content(lastMessage == null ? "" : lastMessage.getContent())
            .headCount(countMembers(chatRoom))
            .unreadCount(getUnreadCount(chatRoom, user))
            .updatedAt(lastMessage == null ? chatRoom.getUpdatedAt() : lastMessage.getCreatedAt())
            .build();
    }
}
