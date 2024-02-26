package mallang_trip.backend.domain.article.service;

import static mallang_trip.backend.domain.article.exception.ArticleExceptionStatus.CANNOT_FOUND_ARTICLE;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.article.entity.ArticleDibs;
import mallang_trip.backend.domain.article.repository.ArticleDibsRepository;
import mallang_trip.backend.domain.article.repository.ArticleRepository;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleDibsService {

	private final UserService userService;
	private final ArticleRepository articleRepository;
	private final ArticleDibsRepository articleDibsRepository;

	/**
	 * 게시글 찜하기
	 */
	public void create(Long articleId) {
		Article article = articleRepository.findByDeletedAndId(false, articleId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_ARTICLE));
		User currentUser = userService.getCurrentUser();
		// 이미 찜한 경우
		if (checkArticleDibs(currentUser, article)) {
			return;
		}
		articleDibsRepository.save(ArticleDibs.builder()
			.article(article)
			.user(currentUser)
			.build());
	}

	/**
	 * 게시글 찜 취소
	 */
	public void delete(Long articleId) {
		Article article = articleRepository.findByDeletedAndId(false, articleId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_ARTICLE));
		User currentUser = userService.getCurrentUser();
		// 찜한 여행지 아닐 때
		if (!checkArticleDibs(currentUser, article)) {
			return;
		}
		articleDibsRepository.deleteByArticleAndUser(article, currentUser);
	}

	/**
	 * 찜 여부 확인
	 */
	public boolean checkArticleDibs(User user, Article article) {
		if (user == null) {
			return false;
		}
		return articleDibsRepository.existsByArticleAndUser(article, user);
	}
}
