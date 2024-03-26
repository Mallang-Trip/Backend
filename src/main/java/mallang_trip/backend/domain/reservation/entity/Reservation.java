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
import mallang_trip.backend.global.entity.BaseEntity;
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
	@Builder.Default()
	private String id = UUID.randomUUID().toString();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "party_member_id", nullable = false)
	private PartyMember member;

	@Column
	private Integer paymentAmount;

	@Column
	private String orderId;

	@Column
	private String receiptUrl;

	@Column
	private String cancelReceiptUrl;

	@Column
	private String payTime;

	@Column
	@Builder.Default()
	private Integer refundAmount = 0;

	@Enumerated(EnumType.STRING)
	private ReservationStatus status;

	public void saveBillingResult(String orderId, String receiptUrl, String payTime){
		this.orderId = orderId;
		this.receiptUrl = receiptUrl;
		this.payTime = payTime;
	}

	public void saveCancelReceipt(String cancelReceiptUrl){
		this.cancelReceiptUrl = cancelReceiptUrl;
	}

	public void changeStatus(ReservationStatus status){
		this.status = status;
	}

	public void setRefundAmount(Integer refundAmount){
		this.refundAmount = refundAmount;
	}
}
