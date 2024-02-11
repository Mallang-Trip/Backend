package mallang_trip.backend.domain.entity.reservation;

import java.util.UUID;
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
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mallang_trip.backend.constant.ReservationStatus;
import mallang_trip.backend.domain.entity.BaseEntity;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.user.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE reservation SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Reservation extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "party_member_id", nullable = false)
	private PartyMember member;

	@Column
	private Integer paymentAmount;

	@Column
	@Builder.Default()
	private String orderId = UUID.randomUUID().toString();

	@Column
	private String paymentKey;

	@Column
	@Builder.Default()
	private Integer refundAmount = 0;

	@Enumerated(EnumType.STRING)
	private ReservationStatus status;

	public void savePaymentKey(String paymentKey){
		this.paymentKey = paymentKey;
	}

	public void changeStatus(ReservationStatus status){
		this.status = status;
	}

	public void setRefundAmount(Integer refundAmount){
		this.refundAmount = refundAmount;
	}
}
