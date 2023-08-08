package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.domain.entity.party.Course;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOwner(User user);
}
