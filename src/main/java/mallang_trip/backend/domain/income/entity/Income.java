package mallang_trip.backend.domain.income.entity;

import static mallang_trip.backend.domain.income.constant.IncomeType.PARTY_INCOME;
import static mallang_trip.backend.domain.income.constant.IncomeType.PENALTY_INCOME;

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
import javax.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.income.constant.IncomeType;
import mallang_trip.backend.domain.income.dto.RemittanceCompleteRequest;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.global.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "deleted = false")
@SQLDelete(sql = "UPDATE driver SET deleted = true WHERE id = ?")
public class Income extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "party_id", nullable = false, updatable = false)
	private Party party;

	@Column(nullable = false, updatable = false)
	private Integer amount;

	@Column
	@Builder.Default()
	private Integer commission = 0;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private IncomeType type;

	@Column
	@Builder.Default()
	private Boolean remitted = false;

	@Column
	private String senderBank;

	@Column
	private String receiverBank;

	@Column
	private String receiverAccountNumber;

	/**
	 * DB에 엔티티를 저장하기 전, 수수료를 계산합니다.
	 * <p>
	 * 파티 수익 수수료는 1.7%, 위약금 수익 수수료는 10%로 계산합니다.
	 */
	@PrePersist
	public void calculateCommission() {
		if(this.type.equals(PARTY_INCOME)){
			this.commission = (int) (this.amount * 0.017);
		} else if (this.type.equals(PENALTY_INCOME)){
			this.commission = (int) (this.amount * 0.1);
		}
	}

	/**
	 * 송금 완료 처리합니다.
	 */
	public void completeRemittance(RemittanceCompleteRequest request){
		this.remitted = true;
		this.senderBank = request.getSenderBank();
		this.receiverBank = request.getReceiverBank();
		this.receiverAccountNumber = request.getReceiverAccountNumber();
	}
}
