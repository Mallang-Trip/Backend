package mallang_trip.backend.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.article.CreateArticleRequest;
import mallang_trip.backend.domain.dto.article.CreateArticleResponse;
import mallang_trip.backend.service.ArticleService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/article")
public class ArticleController {

	private final ArticleService articleService;

	@PostMapping
	public BaseResponse<CreateArticleResponse> create(@RequestBody @Valid CreateArticleRequest request){
		return new BaseResponse<> (articleService.create(request));
	}

	@DeleteMapping("/{id}")
	public BaseResponse<String> delete(@PathVariable Long id){
		articleService.delete(id);
		return new BaseResponse<> ("성공");
	}
}
