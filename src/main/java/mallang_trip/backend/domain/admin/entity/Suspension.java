package mallang_trip.backend.domain.admin.entity;

import static mallang_trip.backend.domain.admin.constant.SuspensionStatus.SUSPENDING;

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
import mallang_trip.backend.domain.admin.constant.SuspensionStatus;
import mallang_trip.backend.domain.global.BaseEntity;
import mallang_trip.backend.domain.user.entity.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE suspension SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Suspension extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column
	private String content;

	@Column
	private Integer duration;

	@Enumerated(EnumType.STRING)
	@Builder.Default()
	private SuspensionStatus status = SUSPENDING;
}
