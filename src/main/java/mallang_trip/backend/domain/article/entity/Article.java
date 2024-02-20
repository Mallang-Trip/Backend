package mallang_trip.backend.domain.article.entity;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.article.constant.ArticleType;
import mallang_trip.backend.domain.article.dto.ArticleRequest;
import mallang_trip.backend.domain.global.BaseEntity;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.user.entity.User;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE article SET deleted = true WHERE id = ?")
public class Article extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, updatable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "party_id")
	private Party party;

	@Enumerated(EnumType.STRING)
	private ArticleType type;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String content;

	@ElementCollection
	@OrderColumn
	private List<String> images;

	public void modify(Party party, ArticleRequest request){
		this.party = party;
		this.type = ArticleType.from(request.getType());
		this.title = request.getTitle();
		this.content = request.getContent();
		this.images = request.getImages();
	}
}
