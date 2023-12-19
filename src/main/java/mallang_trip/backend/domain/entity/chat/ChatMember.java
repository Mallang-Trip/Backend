package mallang_trip.backend.domain.entity.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mallang_trip.backend.domain.entity.BaseEntity;
import mallang_trip.backend.domain.entity.user.User;

@Entity
@Table(name = "chat_member")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false, updatable = false)
    private ChatRoom chatRoom;

    @Column(name = "unread_count")
    @Builder.Default()
    private Integer unreadCount = 0;

    @Column(name = "is_active")
    @Builder.Default()
    private Boolean active = false;

    public void plusUnreadCount(){
        this.unreadCount++;
    }
}
