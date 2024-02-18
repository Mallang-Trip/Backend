package mallang_trip.backend.domains.course.repository;

import java.util.List;
import mallang_trip.backend.domains.course.entity.Course;
import mallang_trip.backend.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOwnerAndDeleted(User user, Boolean deleted);
}
