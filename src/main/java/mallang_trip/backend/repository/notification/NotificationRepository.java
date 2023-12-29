package mallang_trip.backend.repository.notification;

import java.util.List;
import mallang_trip.backend.domain.entity.notification.Notification;
import mallang_trip.backend.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser(User user);

    Integer countByUserAndChecked(User user, Boolean checked);
}
