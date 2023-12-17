package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.User.UserBriefResponse;
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

    // 새로운 그룹 채팅방 생성
    public ChatRoomIdResponse startGroupChat(List<Long> userIds, String roomName) {
        // 채팅방 생성
        ChatRoom room = createChatRoom(true, roomName);
        // 현재 유저 멤버 추가
        createChatMember(room, userService.getCurrentUser());
        // 멤버 초대
        userIds.stream().map(userId -> userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(Not_Found)))
            // chatMember 추가
            .forEach(user -> createChatMember(room, user));
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
        userIds.stream().map(userId -> userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(Not_Found)))
            // 이미 채팅방에 있는 경우 필터링
            .filter(user -> !chatMemberRepository.existsByChatRoomAndUser(room, user))
            // chatMember 추가
            .forEach(user -> createChatMember(room, user));
    }

    // 1:1 채팅방 생성
    public ChatRoomIdResponse startCoupleChat(Long userIds) {
        User user = userService.getCurrentUser();
        User receiver = userRepository.findById(userIds)
            .orElseThrow(() -> new BaseException(Not_Found));
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
        String roomName = room.getIsGroup() ? room.getRoomName() : getOtherUserInCoupleChat(room).getName();
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

    private void createChatMember(ChatRoom room, User user) {
        chatMemberRepository.save(ChatMember.builder()
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
        return chatMessageRepository.findOneByChatRoomOrderByCreatedAtAsc(room);
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
}
