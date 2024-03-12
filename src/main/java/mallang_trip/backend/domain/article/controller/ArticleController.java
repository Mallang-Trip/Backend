package mallang_trip.backend.domain.article.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.data.domain.PageRequest;
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

	@ApiOperation(value = "게시글 등록", notes = "커뮤니티 게시글을 등록합니다.")
	@PostMapping
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "정지된 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<ArticleIdResponse> createArticle(
		@RequestBody @Valid ArticleRequest request) throws BaseException {
		return new BaseResponse<>(articleService.create(request));
	}

	@PutMapping("/{article_id}")
	@ApiOperation(value = "게시글 수정", notes = "article_id에 해당되는 게시글을 수정합니다. 관리자와 작성자만 수정 가능합니다. 변경하지 않는 값이라도 기존 값을 입력해야 합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "article_id", value = "article_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "수정 권한이 없거나, 정지된 사용자입니다."),
		@ApiResponse(code = 404, message = "해당 게시글을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> changeArticle(@PathVariable(value = "article_id") Long articleId,
		@RequestBody @Valid ArticleRequest request) throws BaseException {
		articleService.modify(articleId, request);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/{article_id}")
	@ApiOperation(value = "글 삭제", notes = "article_id에 해당되는 게시글을 삭제합니다. 관리자와 작성자만 삭제 가능합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "article_id", value = "article_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "삭제 권한이 없는 사용자입니다."),
		@ApiResponse(code = 404, message = "해당 게시글을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteArticle(@PathVariable(value = "article_id") Long articleId)
		throws BaseException {
		articleService.delete(articleId);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/{article_id}")
	@ApiOperation(value = "게시글 상세조회", notes = "article_id에 해당되는 게시글을 상세조회합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = false, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "article_id", value = "article_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponse(code = 404, message = "해당 게시글을 찾을 수 없습니다.")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<ArticleDetailsResponse> viewDetails(
		@PathVariable(value = "article_id") Long articleId) throws BaseException {
		return new BaseResponse<>(articleService.view(articleId));
	}

	@GetMapping("/search")
	@ApiOperation(value = "키워드 검색", notes = "제목이나 내용에 키워드가 포함된 게시글 목록을 조회합니다.")
	@ApiImplicitParam(name = "keyword", value = "키워드", required = true, paramType = "query", dataTypeClass = String.class)
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<Page<ArticleBriefResponse>> searchArticles(@RequestParam String keyword,
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(articleService.getArticlesByKeyword(keyword, pageable));
	}

	@GetMapping()
	@ApiOperation(value = "게시글 리스트 조회", notes = "게시판 별 게시글 목록을 조회합니다.")
	@ApiImplicitParam(name = "type", value = "게시판 타입 (all | FIND_PARTNER | FREE_BOARD | FEEDBACK 중 하나)", required = true, paramType = "query", dataTypeClass = String.class)
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<Page<ArticleBriefResponse>> getArticles(@RequestParam String type,
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(articleService.getArticlesByType(type, pageable));
	}

	@GetMapping("/my")
	@ApiOperation(value = "내가 쓴 글 리스트 조회", notes = "내가 작성한 게시글 목록을 조회합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<Page<ArticleBriefResponse>> getMyArticles(
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(articleService.getMyArticles(pageable));
	}

	@GetMapping("/comment/my")
	@ApiOperation(value = "내 댓글 & 대댓글 조회", notes = "내가 작성한 댓글 & 대댓글 목록을 조회합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<Page<MyCommentResponse>> getMyComments(
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(articleCommentService.getMyComments(pageable));
	}

	@PostMapping("/dibs/{article_id}")
	@ApiOperation(value = "게시글 좋아요", notes = "article_id에 해당되는 게시글에 좋아요를 설정합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 404, message = "해당 게시글을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createArticleDibs(@PathVariable(value = "article_id") Long articleId)
		throws BaseException {
		articleDibsService.create(articleId);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/dibs/{article_id}")
	@ApiOperation(value = "게시글 좋아요 취소", notes = "article_id에 해당되는 게시글에 좋아요를 취소합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 404, message = "해당 게시글을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteArticleDibs(@PathVariable(value = "article_id") Long articleId)
		throws BaseException {
		articleDibsService.delete(articleId);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/comment/{article_id}")
	@ApiOperation(value = "댓글 작성", notes = "article_id에 해당되는 게시글에 댓글을 작성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "article_id", value = "article_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자이거나 정지된 사용자입니다."),
		@ApiResponse(code = 404, message = "해당 게시글을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createComment(@PathVariable(value = "article_id") Long articleId,
		@RequestParam String content) throws BaseException {
		articleCommentService.createComment(articleId, content);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/reply/{comment_id}")
	@ApiOperation(value = "대댓글 작성", notes = "comment_id에 해당되는 댓글에 대댓글을 작성합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "comment_id", value = "comment_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자이거나 정지된 사용자입니다."),
		@ApiResponse(code = 404, message = "해당 댓글을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createReply(@PathVariable(value = "comment_id") Long commentId,
		@RequestParam String content) throws BaseException {
		articleCommentService.createReply(commentId, content);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/comment/{comment_id}")
	@ApiOperation(value = "댓글 삭제", notes = "comment_id에 해당되는 댓글을 삭제합니다. 관리자와 작성자만 삭제 가능합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "comment_id", value = "comment_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자이거나 삭제 권한이 없는 사용자입니다."),
		@ApiResponse(code = 404, message = "해당 댓글을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteComment(@PathVariable(value = "comment_id") Long commentId)
		throws BaseException {
		articleCommentService.deleteComment(commentId);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/reply/{reply_id}")
	@ApiOperation(value = "대댓글 삭제", notes = "reply_id에 해당되는 대댓글을 삭제합니다. 관리자와 작성자만 삭제 가능합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "reply_id", value = "reply_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자이거나 삭제 권한이 없는 사용자입니다."),
		@ApiResponse(code = 404, message = "해당 대댓글을 찾을 수 없습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteReply(@PathVariable(value = "reply_id") Long replyId)
		throws BaseException {
		articleCommentService.deleteReply(replyId);
		return new BaseResponse<>("성공");
	}
}
