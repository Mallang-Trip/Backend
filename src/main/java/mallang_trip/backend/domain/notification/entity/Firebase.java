package mallang_trip.backend.domain.notification.entity;

import lombok.*;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.global.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Table
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE firebase SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Firebase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column
    private String token;
}
