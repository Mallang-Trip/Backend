package mallang_trip.backend.service;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.repository.ArticleRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleService {

	private final ArticleRepository articleRepository;

}
