package mallang_trip.backend.domain.course.repository;

import java.util.List;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query(value =
        "SELECT c " +
        "FROM Course c " +
        "WHERE c.deleted = false ")
    List<Course> findAllCourse();

    List<Course> findAllByOwnerAndDeleted(User user, Boolean deleted);
}
