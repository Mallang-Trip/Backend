package mallang_trip.backend.domain.entity.party;

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
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.domain.entity.BaseEntity;
import mallang_trip.backend.domain.entity.user.User;

@Entity
@Getter
@Setter
@Builder
@Table(name = "party_proposal")
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(name = "agreement_need", nullable = false)
    private Integer agreementNeed;

    @Column(name = "agreement_count", nullable = false)
    @Builder.Default()
    private Integer agreementCount = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default()
    private ProposalStatus status = ProposalStatus.WAITING;
}
