package mallang_trip.backend.service;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;
import mallang_trip.backend.domain.Article;
import mallang_trip.backend.domain.User;
import mallang_trip.backend.domain.dto.article.CreateArticleRequest;
import mallang_trip.backend.domain.dto.article.CreateArticleResponse;
import mallang_trip.backend.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {

	private final ArticleRepository articleRepository;

	public CreateArticleResponse create(CreateArticleRequest request){
		Article article = Article.builder()
			.user(getCurrentUser())
			.type(request.getArticleType())
			.title(request.getTitle())
			.content(request.getContent())
			.build();
		return CreateArticleResponse.builder()
			.articleId(articleRepository.save(article).getId())
			.build();
	}

	public void delete(Long id){
		Article article = articleRepository.findById(id)
			.orElseThrow(() -> new BaseException(BaseResponseStatus.Not_Found));
		if(getCurrentUser().getId() != article.getUser().getId()){
			throw new BaseException(BaseResponseStatus.Forbidden);
		}
		articleRepository.delete(article);
	}

	private User getCurrentUser(){
		User user = User.builder().build();
		user.setId(-1L);
		return user;
	}
}
