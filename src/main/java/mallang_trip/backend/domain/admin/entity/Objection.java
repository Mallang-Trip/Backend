package mallang_trip.backend.domain.admin.entity;

import static mallang_trip.backend.domain.admin.constant.ObjectionStatus.WAITING;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mallang_trip.backend.domain.admin.constant.ObjectionStatus;
import mallang_trip.backend.global.entity.BaseEntity;
import mallang_trip.backend.domain.user.entity.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE objection SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Objection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objector_id", nullable = false)
    private User objector;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "report_id", nullable = false)
//    private Report report;

    @Column
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default()
    private ObjectionStatus status = WAITING;
}
