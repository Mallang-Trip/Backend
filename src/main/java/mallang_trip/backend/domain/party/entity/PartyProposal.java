package mallang_trip.backend.domain.party.entity;

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
import mallang_trip.backend.domain.party.constant.AgreementStatus;
import mallang_trip.backend.domain.party.constant.ProposalStatus;
import mallang_trip.backend.domain.party.constant.ProposalType;
import mallang_trip.backend.domain.reservation.entity.UserPromotionCode;
import mallang_trip.backend.global.entity.BaseEntity;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.user.entity.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Builder
@Table(name = "party_proposal")
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE party_proposal SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class PartyProposal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false, updatable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id", nullable = false, updatable = false)
    private User proposer;

    @Column
    private Integer headcount;

    @Column
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "driver_agreement")
    @Builder.Default()
    private AgreementStatus driverAgreement = AgreementStatus.WAITING;

    @Enumerated(EnumType.STRING)
    private ProposalType type;

    @Enumerated(EnumType.STRING)
    @Builder.Default()
    private ProposalStatus status = ProposalStatus.WAITING;

    @ManyToOne
    @JoinColumn(name = "user_promotion_code_id", nullable = true)
    private UserPromotionCode userPromotionCode;
}
