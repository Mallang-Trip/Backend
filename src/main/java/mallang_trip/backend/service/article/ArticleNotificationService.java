package mallang_trip.backend.service.article;

import static mallang_trip.backend.constant.NotificationType.ARTICLE;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.entity.article.Article;
import mallang_trip.backend.domain.entity.article.Comment;
import mallang_trip.backend.domain.entity.article.Reply;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.article.ReplyRepository;
import mallang_trip.backend.service.NotificationService;
import mallang_trip.backend.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleNotificationService {

	private final NotificationService notificationService;
	private final ReplyRepository replyRepository;
	private final UserService userService;

	// 1. 새 댓글 알림
	public void newComment(Article article) {
		String content = new StringBuilder()
			.append("[")
			.append(article.getTitle())
			.append("] 게시글에 새 댓글이 추가되었습니다.")
			.toString();
		notificationService.create(article.getUser(), content, ARTICLE, article.getId());
	}

	// 2. 새 답글 알림
	public void newReply(Comment comment) {
		String content = new StringBuilder()
			.append("내 댓글에 새 답글이 추가되었습니다.")
			.toString();
		getRepliedUsers(comment).stream()
			.forEach(user ->
				notificationService.create(user, content, ARTICLE, comment.getArticle().getId()));
	}

	/**
	 * 답글 작성자 + 답글에 대댓글 단 유저 조회 (현재 유저 제외)
	 */
	private List<User> getRepliedUsers(Comment comment) {
		User writer = comment.getUser();
		List<User> users = replyRepository.findByCommentAndDeleted(comment, false).stream()
			.map(Reply::getUser)
			.collect(Collectors.toList());
		users.add(writer);
		List<User> targets = users.stream()
			.distinct()
			.filter(user -> !user.equals(userService.getCurrentUser()))
			.collect(Collectors.toList());
		return targets;
	}
}
