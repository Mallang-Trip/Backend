package mallang_trip.backend.domain.notification.entity;

import lombok.*;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.global.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

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

    @Type(type = "io.hypersistence.utils.hibernate.type.json.JsonType")
    @Column(name ="tokens", columnDefinition = "json", nullable = false)
    private List<String> tokens;

    public void changeTokens(List<String> tokens)
    {
        this.tokens = tokens;
    }
}
