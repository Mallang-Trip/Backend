package mallang_trip.backend.domain.entity.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import mallang_trip.backend.constant.ChatRoomType;
import mallang_trip.backend.domain.entity.BaseEntity;
import mallang_trip.backend.domain.entity.party.Party;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", updatable = false)
    private Party party;

    @Column(name = "room_name")
    private String roomName;

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;
}
