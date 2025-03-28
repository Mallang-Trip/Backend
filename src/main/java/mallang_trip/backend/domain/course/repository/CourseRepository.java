package mallang_trip.backend.domain.course.repository;

import java.util.List;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query(value =
        "SELECT c " +
        "FROM Course c " +
        "WHERE c.deleted = false " +
        "AND c.capacity >= :headcount " +
        "AND (:region = 'all' OR c.region = :region) " +
        "AND (c.totalPrice / :headcount) <= :maxPrice " +
        "ORDER BY c.totalPrice DESC",
    nativeQuery = false)
    List<Course> findAllByCondition(
        @Param("headcount") int headcount,
        @Param("region") String region,
        @Param("maxPrice") int maxPrice
    );

    List<Course> findAllByOwnerAndDeleted(User user, Boolean deleted);
}
