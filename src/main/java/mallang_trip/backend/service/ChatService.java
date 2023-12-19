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
import mallang_trip.backend.repository.redis.ChatRoomConnection;
import mallang_trip.backend.repository.user.UserRepository;
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
    private final ChatRoomConnection chatRoomConnection;
    //private final SimpMessagingTemplate template;

    // 새로운 그룹 채팅방 생성
    public ChatRoomIdResponse startGroupChat(List<Long> userIds, String roomName) {
        // 채팅방 생성
        ChatRoom room = createChatRoom(true, roomName);
        // 현재 유저 멤버 추가
        createChatMember(room, userService.getCurrentUser()).setActive(true);
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
    public void inviteGroupChatMember(Long chatRoomId, List<Long> userIds) {
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
        // 초대 메시지 생성
        /*template.convertAndSend("/sub/room/" + room.getId(),
            ChatMessageResponse.of(createInviteMessage(users, room)));*/
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
            createChatMember(newChatRoom, userService.getCurrentUser());
            createChatMember(newChatRoom, receiver);
            return ChatRoomIdResponse.builder().chatRoomId(newChatRoom.getId()).build();
        } else { // 진행중인 채팅방이 존재하는 경우
            return ChatRoomIdResponse.builder().chatRoomId(chatRoom.getId()).build();
        }
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
        // 나가기 메시지 생성
        /* template.convertAndSend("/sub/room/" + room.getId(),
            ChatMessageResponse.of(createLeaveMessage(room)));*/
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
        return chatMemberRepository.findByUserAndActive(user, true).stream()
            // 속한 채팅방 찾기
            .map(chatMember -> chatMember.getChatRoom())
            // 채팅방 -> ChatRoomBriefResponse
            .map(chatRoom -> {
                if (chatRoom.getIsGroup()) {
                    return ChatRoomBriefResponse.builder()
                        .chatRoomId(chatRoom.getId())
                        .isGroup(true)
                        .roomName(chatRoom.getRoomName())
                        .profileImages(getProfileImages(chatRoom))
                        .content(getLastMessage(chatRoom).getContent())
                        .headCount(countMembers(chatRoom))
                        .unreadCount(getUnreadCount(chatRoom, user))
                        .updatedAt(getLastMessage(chatRoom).getCreatedAt())
                        .build();
                } else {
                    User other = getOtherUserInCoupleChat(chatRoom);
                    return ChatRoomBriefResponse.builder()
                        .chatRoomId(chatRoom.getId())
                        .isGroup(false)
                        .roomName(other.getName())
                        .profileImages(List.of(other.getProfileImage()))
                        .content(getLastMessage(chatRoom).getContent())
                        .headCount(2)
                        .unreadCount(getUnreadCount(chatRoom, user))
                        .updatedAt(getLastMessage(chatRoom).getCreatedAt())
                        .build();
                }
            }).collect(Collectors.toList());
    }

    // 채팅방 상세 조회
    public ChatRoomDetailsResponse getChatRoomDetails(Long chatRoomId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 CHECK
        if (!chatMemberRepository.existsByChatRoomAndUser(room, userService.getCurrentUser())) {
            throw new BaseException(Unauthorized);
        }
        String roomName =
            room.getIsGroup() ? room.getRoomName() : getOtherUserInCoupleChat(room).getName();
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
    public ChatMessageResponse handleNewMessage(ChatMessageRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new BaseException(Not_Found));
        ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
            .orElseThrow(() -> new BaseException(Not_Found));
        // 권한 CHECK
        if (!chatMemberRepository.existsByChatRoomAndUser(room, user)) {
            throw new BaseException(Unauthorized);
        }
        // 채팅 저장
        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
            .user(user)
            .chatRoom(room)
            .type(request.getType())
            .content(request.getContent())
            .build());
        // 모든 멤버 visibility 전환
        makeMembersActive(room);
        // 현재 접속중인 멤버 제외하고 unreadCount++
        plusUnreadCount(room);
        return ChatMessageResponse.of(message);
    }

    public void connectToChatRoom(StompHeaderAccessor accessor, Long chatRoomId) {
        User user = userService.getCurrentUser(accessor);
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(Not_Found));
        ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user)
            .orElseThrow(() -> new BaseException(Not_Found));
        // 모두 읽음 표시
        member.setUnreadCount(0);
        // Redis 채팅 연결 상태 저장
        chatRoomConnection.saveConnection(member, room);
    }

    public void disconnectToChatRoom(Long userId, Long chatRoomId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BaseException(Not_Found));
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new BaseException(Not_Found));
        ChatMember member = chatMemberRepository.findByChatRoomAndUser(room, user)
            .orElseThrow(() -> new BaseException(Not_Found));
        chatRoomConnection.deleteConnection(member, room);
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
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(
            room);
        if (messages == null) {
            return null;
        }
        return messages.get(0);
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

    private void plusUnreadCount(ChatRoom chatRoom) {
        List<Long> connectedMemberIds = chatRoomConnection.getConnection(chatRoom);
        chatMemberRepository.findByChatRoom(chatRoom).stream()
            .filter(member -> !connectedMemberIds.contains(member.getId()))
            .forEach(member -> member.plusUnreadCount());
    }

    private void makeMembersActive(ChatRoom room) {
        chatMemberRepository.findByChatRoomAndActive(room, false).stream()
            .forEach(member -> member.setActive(true));
    }

    private User getOtherUserInCoupleChat(ChatRoom chatRoom) {
        User user = userService.getCurrentUser();
        return chatMemberRepository.findByChatRoom(chatRoom).stream()
            .filter(member -> !member.getUser().equals(user))
            .collect(Collectors.toList())
            .get(0).getUser();
    }

    private ChatMessage createInviteMessage(List<User> users, ChatRoom room) {
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
}
