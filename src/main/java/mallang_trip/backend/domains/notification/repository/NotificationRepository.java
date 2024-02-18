package mallang_trip.backend.domains.notification.repository;

import java.util.List;
import mallang_trip.backend.domains.notification.entity.Notification;
import mallang_trip.backend.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser(User user);

    Integer countByUserAndChecked(User user, Boolean checked);
}
