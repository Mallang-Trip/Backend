package mallang_trip.backend.domain.article.service;

import static mallang_trip.backend.domain.global.io.BaseResponseStatus.CANNOT_FOUND_ARTICLE;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.SUSPENDING;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.article.constant.ArticleType;
import mallang_trip.backend.domain.article.repository.ArticleRepository;
import mallang_trip.backend.domain.global.io.BaseException;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.article.dto.ArticleBriefResponse;
import mallang_trip.backend.domain.article.dto.ArticleDetailsResponse;
import mallang_trip.backend.domain.article.dto.ArticleIdResponse;
import mallang_trip.backend.domain.article.dto.ArticleRequest;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {

	private final UserService userService;
	private final ArticleCommentService articleCommentService;
	private final ArticleDibsService articleDibsService;
	private final SuspensionService suspensionService;
	private final PartyRepository partyRepository;
	private final ArticleRepository articleRepository;

	/**
	 * 게시글 생성
	 */
	public ArticleIdResponse create(ArticleRequest request) {
		User user = userService.getCurrentUser();
		// 정지 CHECK
		if (suspensionService.isSuspending(user)) {
			throw new BaseException(SUSPENDING);
		}
		// 임베딩 파티 찾기
		Party party = request.getPartyId() == null ? null
			: partyRepository.findById(request.getPartyId()).orElse(null);
		// 게시글 저장
		Article article = articleRepository.save(Article.builder()
			.user(user)
			.type(ArticleType.from(request.getType()))
			.title(request.getTitle())
			.content(request.getContent())
			.party(party)
			.images(request.getImages())
			.build());

		return ArticleIdResponse.builder()
			.articleId(article.getId())
			.build();
	}

	/**
	 * 게시글 수정
	 */
	public void modify(Long articleId, ArticleRequest request) {
		Article article = articleRepository.findByDeletedAndId(false, articleId)
			.orElseThrow(() -> new BaseException(Not_Found));
		User user = userService.getCurrentUser();
		// 정지 CHECK
		if (suspensionService.isSuspending(user)) {
			throw new BaseException(SUSPENDING);
		}
		// 작성자인지 CHECK
		if (!user.equals(article.getUser())) {
			throw new BaseException(Forbidden);
		}
		// 수정
		Party party = request.getPartyId() == null ? null
			: partyRepository.findById(request.getPartyId()).orElse(null);
		article.modify(party, request);
	}

	/**
	 * 게시글 삭제
	 */
	public void delete(Long articleId) {
		Article article = articleRepository.findByDeletedAndId(false, articleId)
			.orElseThrow(() -> new BaseException(Not_Found));
		// 작성자 또는 관리자인지 CHECK
		User user = userService.getCurrentUser();
		if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(article.getUser())) {
			throw new BaseException(Forbidden);
		}
		articleRepository.delete(article);
	}

	/**
	 * 게시글 상세보기
	 */
	public ArticleDetailsResponse view(Long articleId) {
		Article article = articleRepository.findByDeletedAndId(false, articleId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_ARTICLE));
		Party embeddedParty = article.getParty();

		return ArticleDetailsResponse.builder()
			.articleId(article.getId())
			.partyId(embeddedParty == null ? null : embeddedParty.getId())
			.partyName(embeddedParty == null ? null : embeddedParty.getCourse().getName())
			.userId(article.getUser().getId())
			.nickname(article.getUser().getNickname())
			.profileImg(article.getUser().getProfileImage())
			.type(article.getType())
			.title(article.getTitle())
			.content(article.getContent())
			.images(article.getImages())
			.comments(articleCommentService.getCommentsAndReplies(article))
			.commentsCount(articleCommentService.countCommentsAndReplies(article))
			.dibs(articleDibsService.checkArticleDibs(userService.getCurrentUser(), article))
			.createdAt(article.getCreatedAt())
			.updatedAt(article.getUpdatedAt())
			.build();
	}

	/**
	 * 키워드 검색
	 */
	public Page<ArticleBriefResponse> getArticlesByKeyword(String keyword, Pageable pageable) {
		Page<Article> articles = articleRepository.findByDeletedAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByUpdatedAtDesc(
			false, keyword, keyword, pageable);
		List<ArticleBriefResponse> responses = getArticleBriefResponses(articles);

		return new PageImpl<>(responses, pageable, articles.getTotalElements());
	}

	/**
	 * 카테고리 별 조회
	 */
	public Page<ArticleBriefResponse> getArticlesByType(String type, Pageable pageable) {
		Page<Article> articles = type.equals("all") ?
			articleRepository.findByDeletedOrderByUpdatedAtDesc(false, pageable)
			: articleRepository.findByDeletedAndTypeOrderByUpdatedAtDesc(false, ArticleType.from(type), pageable);
		List<ArticleBriefResponse> responses = getArticleBriefResponses(articles);

		return new PageImpl<>(responses, pageable, articles.getTotalElements());
	}

	/**
	 * 내가 작성한 글 조회
	 */
	public Page<ArticleBriefResponse> getMyArticles(Pageable pageable) {
		User user = userService.getCurrentUser();
		Page<Article> articles = articleRepository.
			findByDeletedAndUserOrderByUpdatedAtDesc(false, user, pageable);
		List<ArticleBriefResponse> responses = getArticleBriefResponses(articles);

		return new PageImpl<>(responses, pageable, articles.getTotalElements());
	}

	/**
	 * List<Article> -> List<ArticleBriefResponse> 변환
	 */
	private List<ArticleBriefResponse> getArticleBriefResponses(Page<Article> articles) {
		List<ArticleBriefResponse> responses = articles.stream()
			.map(article -> ArticleBriefResponse.of(
				article,
				articleCommentService.countCommentsAndReplies(article))
			)
			.collect(Collectors.toList());
		return responses;
	}
}
