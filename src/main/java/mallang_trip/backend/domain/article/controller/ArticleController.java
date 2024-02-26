package mallang_trip.backend.domain.article.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.article.service.ArticleCommentService;
import mallang_trip.backend.domain.article.service.ArticleDibsService;
import mallang_trip.backend.domain.article.service.ArticleService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.article.dto.ArticleBriefResponse;
import mallang_trip.backend.domain.article.dto.ArticleDetailsResponse;
import mallang_trip.backend.domain.article.dto.ArticleIdResponse;
import mallang_trip.backend.domain.article.dto.ArticleRequest;
import mallang_trip.backend.domain.article.dto.MyCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Article API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/article")
public class ArticleController {

	private final ArticleService articleService;
	private final ArticleCommentService articleCommentService;
	private final ArticleDibsService articleDibsService;

	@ApiOperation(value = "글 등록")
	@PostMapping
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<ArticleIdResponse> createArticle(
		@RequestBody @Valid ArticleRequest request) throws BaseException {
		return new BaseResponse<>(articleService.create(request));
	}

	@ApiOperation(value = "글 수정")
	@PutMapping("/{article_id}")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> changeArticle(@PathVariable(value = "article_id") Long articleId,
		@RequestBody @Valid ArticleRequest request) throws BaseException {
		articleService.modify(articleId, request);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "글 삭제")
	@DeleteMapping("/{article_id}")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteArticle(@PathVariable(value = "article_id") Long articleId)
		throws BaseException {
		articleService.delete(articleId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "글 상세조회")
	@GetMapping("/{article_id}")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<ArticleDetailsResponse> viewDetails(
		@PathVariable(value = "article_id") Long articleId) throws BaseException {
		return new BaseResponse<>(articleService.view(articleId));
	}

	@ApiOperation(value = "키워드 검색")
	@GetMapping("/search")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<Page<ArticleBriefResponse>> searchArticles(@RequestParam String keyword,
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(articleService.getArticlesByKeyword(keyword, pageable));
	}

	@ApiOperation(value = "게시글 리스트 조회")
	@GetMapping()
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<Page<ArticleBriefResponse>> getArticles(@RequestParam String type,
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(articleService.getArticlesByType(type, pageable));
	}

	@ApiOperation(value = "내가 쓴 글 리스트 조회")
	@GetMapping("/my")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<Page<ArticleBriefResponse>> getMyArticles(
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(articleService.getMyArticles(pageable));
	}

	@ApiOperation(value = "내 댓글 & 대댓글 조회")
	@GetMapping("/comment/my")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<Page<MyCommentResponse>> getMyComments(
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(articleCommentService.getMyComments(pageable));
	}

	@PostMapping("/dibs/{article_id}")
	@ApiOperation(value = "게시글 좋아요")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createArticleDibs(@PathVariable(value = "article_id") Long articleId)
		throws BaseException {
		articleDibsService.create(articleId);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/dibs/{article_id}")
	@ApiOperation(value = "게시글 좋아요 취소")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteArticleDibs(@PathVariable(value = "article_id") Long articleId)
		throws BaseException {
		articleDibsService.delete(articleId);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/comment/{article_id}")
	@ApiOperation(value = "댓글 작성")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createComment(@PathVariable(value = "article_id") Long articleId,
		@RequestParam String content) throws BaseException {
		articleCommentService.createComment(articleId, content);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/reply/{comment_id}")
	@ApiOperation(value = "대댓글 작성")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createReply(@PathVariable(value = "comment_id") Long commentId,
		@RequestParam String content) throws BaseException {
		articleCommentService.createReply(commentId, content);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/comment/{comment_id}")
	@ApiOperation(value = "댓글 삭제")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteComment(@PathVariable(value = "comment_id") Long commentId)
		throws BaseException {
		articleCommentService.deleteComment(commentId);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/reply/{reply_id}")
	@ApiOperation(value = "대댓글 삭제")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteReply(@PathVariable(value = "reply_id") Long replyId)
		throws BaseException {
		articleCommentService.deleteReply(replyId);
		return new BaseResponse<>("성공");
	}

}
