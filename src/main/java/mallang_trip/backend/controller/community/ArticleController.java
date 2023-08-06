package mallang_trip.backend.controller.community;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.article.ArticleBriefResponse;
import mallang_trip.backend.domain.dto.article.ArticleDetailsResponse;
import mallang_trip.backend.domain.dto.article.ArticleIdResponse;
import mallang_trip.backend.domain.dto.article.ArticleRequest;
import mallang_trip.backend.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequiredArgsConstructor
@RequestMapping("/article")
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public BaseResponse<ArticleIdResponse> createArticle(
        @RequestBody @Valid ArticleRequest request) {
        return new BaseResponse<>(articleService.createArticle(request));
    }

    @PutMapping("/{id}")
    public BaseResponse<ArticleIdResponse> changeArticle(@PathVariable Long id,
        @RequestBody @Valid ArticleRequest request) {
        return new BaseResponse<>(articleService.changeArticle(id, request));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<String> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/{id}")
    public BaseResponse<ArticleDetailsResponse> viewDetails(@PathVariable Long id) {
        return new BaseResponse<>(articleService.getArticleDetails(id));
    }

    @GetMapping("/search")
    public BaseResponse<Page<ArticleBriefResponse>> searchArticles(@RequestParam String type,
        @RequestParam String keyword,
        @PageableDefault(size = 6, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new BaseResponse<>(articleService.searchArticles(type, keyword, pageable));
    }

    @GetMapping("/my")
    public BaseResponse<Page<ArticleBriefResponse>> getMyArticles(
        @PageableDefault(size = 6, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new BaseResponse<>(articleService.getMyArticles(pageable));
    }
}
