package mallang_trip.backend.repository.redis;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.entity.chat.ChatMember;
import mallang_trip.backend.domain.entity.chat.ChatRoom;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ChatRoomConnection {

    private static final String CONNECTIONS_KEY_PREFIX = "chat:connections:";

    private final StringRedisTemplate stringRedisTemplate;

    // 연결 정보 저장
    public void saveConnection(ChatMember member, ChatRoom room) {
        String memberKey = getMemberKey(member);
        String roomKey = getRoomKey(room);
        stringRedisTemplate.opsForSet().add(roomKey, memberKey);
    }

    // 연결 정보 삭제
    public void deleteConnection(ChatMember member, ChatRoom room) {
        String memberKey = getMemberKey(member);
        String roomKey = getRoomKey(room);
        stringRedisTemplate.opsForSet().remove(roomKey, memberKey);
    }

    // 연결 정보 조회
    public List<Long> getConnection(ChatRoom room) {
        String roomKey = getRoomKey(room);
        Set<String> memberKeys = stringRedisTemplate.opsForSet().members(roomKey);
        return memberKeys.stream()
            .map(this::extractUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private String getMemberKey(ChatMember member) {
        return CONNECTIONS_KEY_PREFIX + "member:" + member.getId();
    }

    private String getRoomKey(ChatRoom room) {
        return CONNECTIONS_KEY_PREFIX + "room:" + room.getId();
    }

    private Long extractUserId(Object memberKey) {
        String keyString = (String) memberKey;
        String[] parts = keyString.split(":");
        if (parts.length == 4 && "member".equals(parts[2])) {
            return Long.parseLong(parts[3]);
        }
        return null;
    }
}