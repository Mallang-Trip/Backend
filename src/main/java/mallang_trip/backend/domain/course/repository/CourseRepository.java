package mallang_trip.backend.domain.course.repository;

import java.util.List;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOwnerAndDeleted(User user, Boolean deleted);
}
