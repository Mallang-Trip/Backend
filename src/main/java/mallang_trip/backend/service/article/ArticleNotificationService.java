package mallang_trip.backend.service.article;

import static mallang_trip.backend.constant.NotificationType.ARTICLE;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.entity.article.Article;
import mallang_trip.backend.domain.entity.article.Reply;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleNotificationService {

	private final NotificationService notificationService;

	// 1. 새 댓글 알림
	public void newComment(Article article) {
		StringBuilder content = new StringBuilder();
		content.append("[")
			.append(article.getTitle())
			.append("] 게시글에 새 댓글이 추가되었습니다.");
		notificationService.create(article.getUser(), content.toString(), ARTICLE, article.getId());
	}

	// 2. 새 답글 알림
	public void newReply(Reply reply) {
		StringBuilder content = new StringBuilder();
		content.append("내 댓글에 새 답글이 추가되었습니다.");
		notificationService.create(reply.getUser(), content.toString(), ARTICLE,
			reply.getComment().getArticle().getId());
	}
}
