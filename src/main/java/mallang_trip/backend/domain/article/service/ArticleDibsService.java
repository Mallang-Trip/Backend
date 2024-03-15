package mallang_trip.backend.domain.article.service;

import static mallang_trip.backend.domain.article.exception.ArticleExceptionStatus.CANNOT_FOUND_ARTICLE;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.article.entity.ArticleDibs;
import mallang_trip.backend.domain.article.repository.ArticleDibsRepository;
import mallang_trip.backend.domain.article.repository.ArticleRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleDibsService {

	private final CurrentUserService currentUserService;
	private final ArticleRepository articleRepository;
	private final ArticleDibsRepository articleDibsRepository;

	/**
	 * 게시글 좋아요를 처리하는 메소드입니다.
	 * <p>
	 * 이미 좋아요를 누른 상태라면 변경없이 넘어갑니다.
	 *
	 * @param articleId 좋아요를 누를 게시글의 ID 값
	 * @throws BaseException articleId에 해당하는 게시글을 찾지 못할 경우 발생하는 예외
	 */
	public void create(Long articleId) {
		Article article = articleRepository.findByDeletedAndId(false, articleId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_ARTICLE));
		User currentUser = currentUserService.getCurrentUser();
		// 이미 좋아요를 누른 경우
		if (checkArticleDibs(currentUser, article)) {
			return;
		}

		articleDibsRepository.save(ArticleDibs.builder()
			.article(article)
			.user(currentUser)
			.build());
	}

	/**
	 * 게시글 좋아요를 취소하는 메소드입니다.
	 * <p>
	 * 좋아요를 누른 게시글이 아니라면 변경없이 넘어갑니다.
	 *
	 * @param articleId articleId 좋아요를 취소할 게시글의 ID 값
	 * @throws BaseException articleId에 해당하는 게시글을 찾지 못할 경우 발생하는 예외
	 */
	public void delete(Long articleId) {
		Article article = articleRepository.findByDeletedAndId(false, articleId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_ARTICLE));
		User currentUser = currentUserService.getCurrentUser();
		// 좋아요를 누른 게시글이 아닐 경우
		if (!checkArticleDibs(currentUser, article)) {
			return;
		}

		articleDibsRepository.deleteByArticleAndUser(article, currentUser);
	}

	/**
	 * 유저의 게시글 좋아요 여부를 확인하는 메소드입니다.
	 *
	 * @param user    여부를 확인할 User 객체
	 * @param article 여부를 확인할 Article 객체
	 * @return 좋아요를 누른 상태인 경우 true를 반환하고, 누르지 않은 상태인 경우이거나 User값이 null인 경우 false를 반환합니다.
	 */
	public boolean checkArticleDibs(User user, Article article) {
		if (user == null) {
			return false;
		}

		return articleDibsRepository.existsByArticleAndUser(article, user);
	}
}
