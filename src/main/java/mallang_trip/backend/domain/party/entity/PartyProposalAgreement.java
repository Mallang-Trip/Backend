package mallang_trip.backend.domain.party.entity;

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
import mallang_trip.backend.domain.global.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Builder
@Table(name = "party_proposal_agreement")
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE party_proposal_agreement SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class PartyProposalAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private PartyProposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private PartyMember member;

    @Enumerated(EnumType.STRING)
    @Builder.Default()
    private AgreementStatus status = AgreementStatus.WAITING;
}
