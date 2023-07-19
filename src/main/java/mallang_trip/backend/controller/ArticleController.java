package mallang_trip.backend.controller;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.service.ArticleService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArticleController {

	private final ArticleService articleService;
}
