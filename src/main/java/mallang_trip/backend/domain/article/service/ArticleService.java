package mallang_trip.backend.domain.article.service;

import static mallang_trip.backend.domain.admin.exception.AdminExceptionStatus.SUSPENDING;
import static mallang_trip.backend.domain.article.exception.ArticleExceptionStatus.CANNOT_FOUND_ARTICLE;
import static mallang_trip.backend.domain.article.exception.ArticleExceptionStatus.DELETION_FORBIDDEN;
import static mallang_trip.backend.domain.article.exception.ArticleExceptionStatus.MODIFICATION_FORBIDDEN;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_ADMIN;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.article.constant.ArticleType;
import mallang_trip.backend.domain.article.repository.ArticleRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.article.entity.Article;
import mallang_trip.backend.domain.article.dto.ArticleBriefResponse;
import mallang_trip.backend.domain.article.dto.ArticleDetailsResponse;
import mallang_trip.backend.domain.article.dto.ArticleIdResponse;
import mallang_trip.backend.domain.article.dto.ArticleRequest;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {

	private final CurrentUserService currentUserService;
	private final ArticleCommentService articleCommentService;
	private final ArticleDibsService articleDibsService;
	private final SuspensionService suspensionService;
	private final PartyRepository partyRepository;
	private final ArticleRepository articleRepository;

	/**
	 * 게시글 생성을 처리하는 메소드입니다.
	 *
	 * @param request 게시글 요청 객체
	 * @return 생성된 게시글 ID를 담은 ArticleIdResponse 객체
	 * @throws BaseException 정지된 사용자인 경우 발생하는 예외
	 */
	public ArticleIdResponse create(ArticleRequest request) {
		User user = currentUserService.getCurrentUser();
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
	 * 게시글 수정을 처리하는 메소드입니다.
	 *
	 * @param articleId 수정할 게시글의 ID 값
	 * @param request   게시글 요청 객체
	 * @throws BaseException 정지된 사용자이거나 작성자가 아닌 경우 발생하는 예외
	 */
	public void modify(Long articleId, ArticleRequest request) {
		Article article = articleRepository.findByDeletedAndId(false, articleId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_ARTICLE));
		User user = currentUserService.getCurrentUser();
		// 정지 CHECK
		if (suspensionService.isSuspending(user)) {
			throw new BaseException(SUSPENDING);
		}
		// 작성자인지 CHECK
		if (!user.equals(article.getUser())) {
			throw new BaseException(MODIFICATION_FORBIDDEN);
		}
		// 수정
		Party party = request.getPartyId() == null ? null
			: partyRepository.findById(request.getPartyId()).orElse(null);

		article.modify(party, request);
	}

	/**
	 * 게시글 삭제를 처리하는 메소드입니다.
	 * <p>
	 * 해당하는 게시글을 soft delete(deleted = ture) 처리합니다.
	 *
	 * @param articleId 삭제할 게시글의 ID 값
	 * @throws BaseException 작성자나 관리자가 아닌 경우 발생하는 예외
	 */
	public void delete(Long articleId) {
		Article article = articleRepository.findByDeletedAndId(false, articleId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_ARTICLE));
		// 작성자 또는 관리자인지 CHECK
		User user = currentUserService.getCurrentUser();
		if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(article.getUser())) {
			throw new BaseException(DELETION_FORBIDDEN);
		}

		articleRepository.delete(article);
	}

	/**
	 * 게시글의 상세 정보를 조회하는 메소드입니다.
	 *
	 * @param articleId 조회할 게시글의 ID 값
	 * @throws BaseException articleId에 해당하는 게시글을 찾지 못할 경우 발생하는 예외
	 * @return 게시글의 상세 정보를 담은 ArticleDetailsResponse 객체
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
			.dibs(articleDibsService.checkArticleDibs(currentUserService.getCurrentUser(), article))
			.createdAt(article.getCreatedAt())
			.updatedAt(article.getUpdatedAt())
			.build();
	}

	/**
	 * 키워드가 포함된 게시글 목록을 조회하는 메소드입니다.
	 * <p>
	 * 제목이나 내용에 키워드가 포함된 게시글들을 수정시간이 최신인 순서로 정렬하여 조회합니다.
	 *
	 * @param keyword  검색 키워드 값
	 * @param pageable 페이징 정보를 담은 Pageable 객체
	 * @return 게시글들의 요약 정보와 페이징 정보를 담은 Page<ArticleBriefResponse> 객체
	 */
	public Page<ArticleBriefResponse> getArticlesByKeyword(String keyword, Pageable pageable) {
		Page<Article> articles = articleRepository.findByDeletedAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByUpdatedAtDesc(
			false, keyword, keyword, pageable);
		List<ArticleBriefResponse> responses = getArticleBriefResponses(articles);

		return new PageImpl<>(responses, pageable, articles.getTotalElements());
	}

	/**
	 * 게시판 별로 게시글 목록을 조회하는 메소드입니다.
	 *
	 * @param type     게시판 종류 값 (all, free_board, find_partner, feedback)
	 * @param pageable 페이징 정보를 담은 Pageable 객체
	 * @return 게시글들의 요약 정보와 페이징 정보를 담은 Page<ArticleBriefResponse> 객체
	 */
	public Page<ArticleBriefResponse> getArticlesByType(String type, Pageable pageable) {
		Page<Article> articles = type.equals("all") ?
			articleRepository.findByDeletedOrderByUpdatedAtDesc(false, pageable)
			: articleRepository.findByDeletedAndTypeOrderByUpdatedAtDesc(false,
				ArticleType.from(type), pageable);
		List<ArticleBriefResponse> responses = getArticleBriefResponses(articles);

		return new PageImpl<>(responses, pageable, articles.getTotalElements());
	}

	/**
	 * 내가 작성한 게시글 목록을 조회하는 메소드입니다.
	 *
	 * @param pageable 페이징 정보를 담은 Pageable 객체
	 * @return 게시글들의 요약 정보와 페이징 정보를 담은 Page<ArticleBriefResponse> 객체
	 */
	public Page<ArticleBriefResponse> getMyArticles(Pageable pageable) {
		User user = currentUserService.getCurrentUser();
		Page<Article> articles = articleRepository.
			findByDeletedAndUserOrderByUpdatedAtDesc(false, user, pageable);
		List<ArticleBriefResponse> responses = getArticleBriefResponses(articles);

		return new PageImpl<>(responses, pageable, articles.getTotalElements());
	}

	/**
	 * Page<Article>으로 List<ArticleBriefResponse>를 생성하는 메소드입니다.
	 *
	 * @param articles 사용할 Page<Article> 객체
	 * @return 생성된 List<ArticleBriefResponse> 객체
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
