package mallang_trip.backend.controller.community;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.comment.CommentRequest;
import mallang_trip.backend.domain.dto.comment.MyCommentResponse;
import mallang_trip.backend.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comment/{id}")
    public BaseResponse<String> createComment(
        @PathVariable Long id,
        @RequestBody @Valid CommentRequest request) {
        commentService.createComment(id, request);
        return new BaseResponse<>("성공");
    }

    @PostMapping("/reply/{id}")
    public BaseResponse<String> createReply(
        @PathVariable Long id,
        @RequestBody @Valid CommentRequest request) {
        commentService.createReply(id, request);
        return new BaseResponse<>("성공");
    }

    @DeleteMapping("/comment/{id}")
    public BaseResponse<String> createComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return new BaseResponse<>("성공");
    }

    @DeleteMapping("/reply/{id}")
    public BaseResponse<String> createReply(@PathVariable Long id) {
        commentService.deleteReply(id);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/comment/my")
    public BaseResponse<Page<MyCommentResponse>> getMyCommentsAndReplies(
        @PageableDefault(size = 6) Pageable pageable) {
        return new BaseResponse<>(commentService.getMyCommentsAndReplies(pageable));
    }

}
