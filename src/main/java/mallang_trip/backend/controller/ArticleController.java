package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.article.ArticleBriefResponse;
import mallang_trip.backend.domain.dto.article.ArticleDetailsResponse;
import mallang_trip.backend.domain.dto.article.ArticleIdResponse;
import mallang_trip.backend.domain.dto.article.ArticleRequest;
import mallang_trip.backend.domain.dto.article.MyCommentResponse;
import mallang_trip.backend.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

	@ApiOperation(value = "글 등록")
	@PostMapping
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<ArticleIdResponse> createArticle(
		@RequestBody @Valid ArticleRequest request) {
		return new BaseResponse<>(articleService.createArticle(request));
	}

	@ApiOperation(value = "글 수정")
	@PutMapping("/{article_id}")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> changeArticle(@PathVariable(value = "article_id") Long id,
		@RequestBody @Valid ArticleRequest request) {
		articleService.changeArticle(id, request);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "글 삭제")
	@DeleteMapping("/{article_id}")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteArticle(@PathVariable(value = "article_id") Long id)
		throws BaseException {
		articleService.deleteArticle(id);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "글 상세조회")
	@GetMapping("/{article_id}")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<ArticleDetailsResponse> viewDetails(
		@PathVariable(value = "article_id") Long id) throws BaseException {
		return new BaseResponse<>(articleService.getArticleDetails(id));
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
		@PageableDefault(size = 6, sort = "createdAt", direction = Direction.DESC) Pageable pageable)
		throws BaseException {
		return new BaseResponse<>(articleService.getMyComments(pageable));
	}

	@PostMapping("/dibs/{article_id}")
	@ApiOperation(value = "게시글 좋아요")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createArticleDibs(@PathVariable(value = "article_id") Long id)
		throws BaseException {
		articleService.createArticleDibs(id);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/dibs/{article_id}")
	@ApiOperation(value = "게시글 좋아요 취소")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteArticleDibs(@PathVariable(value = "article_id") Long id)
		throws BaseException {
		articleService.deleteArticleDibs(id);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/comment/{article_id}")
	@ApiOperation(value = "댓글 작성")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createComment(@PathVariable(value = "article_id") Long id,
		@RequestParam String content) throws BaseException {
		articleService.createComment(id, content);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/reply/{comment_id}")
	@ApiOperation(value = "대댓글 작성")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createReply(@PathVariable(value = "comment_id") Long id,
		@RequestParam String content) throws BaseException {
		articleService.createReply(id, content);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/comment/{comment_id}")
	@ApiOperation(value = "댓글 삭제")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteComment(@PathVariable(value = "comment_id") Long id)
		throws BaseException {
		articleService.deleteComment(id);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/reply/{reply_id}")
	@ApiOperation(value = "대댓글 삭제")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteReply(@PathVariable(value = "reply_id") Long id)
		throws BaseException {
		articleService.deleteReply(id);
		return new BaseResponse<>("성공");
	}

}
