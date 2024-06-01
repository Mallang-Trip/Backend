package mallang_trip.backend.domain.article.service;

import static mallang_trip.backend.domain.notification.constant.NotificationType.ARTICLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.article.repository.ReplyRepository;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.article.entity.Comment;
import mallang_trip.backend.domain.article.entity.Reply;
import mallang_trip.backend.domain.mail.service.MailService;
import mallang_trip.backend.domain.notification.entity.Firebase;
import mallang_trip.backend.domain.notification.repository.FirebaseRepository;
import mallang_trip.backend.domain.notification.service.FirebaseService;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.notification.service.NotificationService;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleNotificationService {

	private final NotificationService notificationService;
	private final ReplyRepository replyRepository;
	private final CurrentUserService currentUserService;

	private final MailService mailService;
	private final FirebaseRepository firebaseRepository;
	private final FirebaseService firebaseService;

	/**
	 * 새 댓글이 작성되었을 때 알림을 전송하는 메소드입니다.
	 * <p>
	 * 새 댓글이 작성되었을 때 게시글의 작성자에게 "[게시글 제목] 게시글에 새 댓글이 추가되었습니다." 알림을 전송합니다.
	 *
	 * @param article 새 댓글이 작성된 Article 객체
	 */
	public void newComment(Article article) {
		String content = new StringBuilder()
			.append("[")
			.append(article.getTitle())
			.append("] 게시글에 새 댓글이 추가되었습니다.")
			.toString();
		notificationService.create(article.getUser(), content, ARTICLE, article.getId());
		//mailService.sendEmailNotification(article.getUser().getEmail(),article.getUser().getName(),content,"새 댓글이 추가되었습니다.");
		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokenNotNull(article.getUser());
		firebase.ifPresent(value -> firebaseService.sendPushMessage(value.getToken(), "말랑트립", content));
	}

	/**
	 * 새 답글이 작성되었을 때 알림을 전송하는 메소드입니다.
	 * <p>
	 * 댓글의 작성자와 댓글의 하위 답글 작성자들에게 "내 댓글에 새 답글이 추가되었습니다." 알림을 전송합니다. 현재 유저는 제외합니다.
	 *
	 * @param comment 새 답글이 작성된 Comment 객체
	 */
	public void newReply(Comment comment) {
		String content = new StringBuilder()
			.append("내 댓글에 새 답글이 추가되었습니다.")
			.toString();

		List<String> firebaseTokens = new ArrayList<>();

		getRepliedUsers(comment).stream()
			.forEach(user ->
			{
				notificationService.create(user, content, ARTICLE, comment.getArticle().getId());

				// firebase push message
				Optional<Firebase> firebase = firebaseRepository.findByUserAndTokenNotNull(user);
				firebase.ifPresent(value -> firebaseTokens.add(value.getToken()));
			});

		if(firebaseTokens != null && !firebaseTokens.isEmpty()){
			firebaseService.sendPushMessage(firebaseTokens,"말랑트립",content);
		}
	}

	/**
	 * 새 답글 알림을 전송할 유저들을 조회하는 메소드입니다.
	 * <p>
	 * 댓글의 작성자와 댓글의 하위에 있는 답글들의 작성자들을 조회합니다. 현재 유저는 제외합니다.
	 *
	 * @param comment
	 * @return 대상 유저들의 목록을 담은 List<User> 객체
	 */
	private List<User> getRepliedUsers(Comment comment) {
		// 댓글에 대한 대댓글 작성자 목록 조회
		List<User> users = replyRepository.findByCommentAndDeleted(comment, false).stream()
			.map(Reply::getUser)
			.collect(Collectors.toList());
		// 댓글 작성자 추가
		users.add(comment.getUser());
		// 중복 제거 + 현재 유저 제외
		return users.stream()
			.distinct()
			.filter(user -> !user.equals(currentUserService.getCurrentUser()))
			.collect(Collectors.toList());
	}
}
