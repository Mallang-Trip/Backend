package mallang_trip.backend.domain.notification.entity;

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
import mallang_trip.backend.domain.notification.constant.NotificationType;
import mallang_trip.backend.domain.global.BaseEntity;
import mallang_trip.backend.domain.user.entity.User;

@Entity
@Table
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column
    private String content;

    @Column
    private NotificationType type;

    @Column(name = "target_id")
    private Long targetId;

    @Column
    @Builder.Default()
    private Boolean checked = false;

    public void setCheckTrue(){
        this.checked = true;
    }
}
