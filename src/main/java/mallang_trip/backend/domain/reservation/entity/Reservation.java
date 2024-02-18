package mallang_trip.backend.domain.reservation.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.reservation.constant.ReservationStatus;
import mallang_trip.backend.domain.global.BaseEntity;
import mallang_trip.backend.domain.party.entity.PartyMember;
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
	private String id = UUID.randomUUID().toString();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "party_member_id", nullable = false)
	private PartyMember member;

	@Column
	private Integer paymentAmount;

	@Column
	private String paymentKey;

	@Column
	private String receiptUrl;

	@Column
	@Builder.Default()
	private Integer refundAmount = 0;

	@Enumerated(EnumType.STRING)
	private ReservationStatus status;

	public void savePaymentKeyAndReceiptUrl(String paymentKey, String url){
		this.paymentKey = paymentKey;
		this.receiptUrl = url;
	}

	public void changeStatus(ReservationStatus status){
		this.status = status;
	}

	public void setRefundAmount(Integer refundAmount){
		this.refundAmount = refundAmount;
	}
}
