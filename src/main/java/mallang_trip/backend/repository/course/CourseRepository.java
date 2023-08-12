package mallang_trip.backend.repository.course;

import java.util.List;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOwnerAndDeleted(User user, Boolean deleted);
}
