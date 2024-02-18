package mallang_trip.backend.domain.party.entity;

import javax.persistence.Entity;
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
import mallang_trip.backend.domain.global.BaseEntity;
import mallang_trip.backend.domain.user.entity.User;

@Entity
@Getter
@Setter
@Builder
@Table(name = "party_dibs")
@AllArgsConstructor
@NoArgsConstructor
public class PartyDibs extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "party_id", nullable = false)
	private Party party;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
