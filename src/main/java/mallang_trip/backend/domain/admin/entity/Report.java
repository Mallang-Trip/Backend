package mallang_trip.backend.domain.admin.entity;

import static mallang_trip.backend.domain.admin.constant.ReportStatus.WAITING;

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
import mallang_trip.backend.domain.admin.constant.ReportStatus;
import mallang_trip.backend.domain.admin.constant.ReportType;
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
@SQLDelete(sql = "UPDATE report SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Report extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter_id", nullable = false)
	private User reporter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reportee_id", nullable = false)
	private User reportee;

	@Column
	private String content;

	@Enumerated(EnumType.STRING)
	private ReportType type;

	@Column(name = "target_id")
	private Long targetId;

	@Enumerated(EnumType.STRING)
	@Builder.Default()
	private ReportStatus status = WAITING;
}
