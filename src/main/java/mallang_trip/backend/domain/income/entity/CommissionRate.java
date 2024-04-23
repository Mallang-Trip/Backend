package mallang_trip.backend.domain.income.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommissionRate extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Double partyCommissionRate;

	@Column
	private Double penaltyCommissionRate;

	public void changeCommissionRate(double partyCommissionRate, double penaltyCommissionRate) {
		this.partyCommissionRate = partyCommissionRate;
		this.penaltyCommissionRate = penaltyCommissionRate;
	}
}
