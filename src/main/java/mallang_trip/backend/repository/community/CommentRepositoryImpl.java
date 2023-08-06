package mallang_trip.backend.repository.community;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dto.comment.MyCommentResponse;
import mallang_trip.backend.domain.entity.user.QUser;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.domain.entity.community.QArticle;
import mallang_trip.backend.domain.entity.community.QComment;
import mallang_trip.backend.domain.entity.community.QReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MyCommentResponse> getMyCommentsAndReplies(User user, Pageable pageable) {

        QUser u = QUser.user;
        QArticle a = QArticle.article;
        QComment c = QComment.comment;
        QReply r = QReply.reply;

        List<Tuple> commentResults = queryFactory
            .select(
                c.article.id.as("articleId"),
                u.nickname.as("nickname"),
                a.title.as("title"),
                c.content,
                c.createdAt
            )
            .from(c)
            .join(c.user, u)
            .join(c.article, a)
            .where(c.user.id.eq(user.getId()), c.deleted.eq(false))
            .fetch();

        List<Tuple> replyResults = queryFactory
            .select(
                r.comment.article.id.as("articleId"),
                u.nickname.as("nickname"),
                a.title.as("title"),
                r.content,
                r.createdAt
            )
            .from(r)
            .join(r.user, u)
            .join(r.comment, c)
            .join(c.article, a)
            .where(r.user.id.eq(user.getId()), r.deleted.eq(false))
            .fetch();

        List<Tuple> allResults = new ArrayList<>();
        allResults.addAll(commentResults);
        allResults.addAll(replyResults);

        List<MyCommentResponse> resultDTOList = allResults.stream()
            .map(tuple -> new MyCommentResponse(
                tuple.get(0, Long.class),
                tuple.get(1, String.class),
                tuple.get(2, String.class),
                tuple.get(3, String.class),
                tuple.get(4, LocalDateTime.class)
            ))
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), resultDTOList.size());
        return new PageImpl<>(resultDTOList.subList(start, end), pageable, resultDTOList.size());
    }
}
