package mallang_trip.backend.service;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.repository.ArticleRepository;
import mallang_trip.backend.repository.CommentRepository;
import mallang_trip.backend.repository.ReplyRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;


}
