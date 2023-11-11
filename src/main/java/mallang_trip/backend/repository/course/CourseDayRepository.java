package mallang_trip.backend.repository.course;

import java.util.List;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.course.CourseDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseDayRepository extends JpaRepository<CourseDay, Long> {

    List<CourseDay> findAllByCourse(Course course);

    void deleteAllByCourse(Course course);
}
