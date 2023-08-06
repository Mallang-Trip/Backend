package mallang_trip.backend.repository.party;

import java.util.List;
import mallang_trip.backend.domain.entity.party.Course;
import mallang_trip.backend.domain.entity.party.CourseDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseDayRepository extends JpaRepository<CourseDay, Long> {

    List<CourseDay> findAllByCourse(Course course);
}
