package mallang_trip.backend.domain.notification.repository;

import java.util.List;
import mallang_trip.backend.domain.notification.entity.Notification;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    Integer countByUserAndChecked(User user, Boolean checked);

    List<Notification> findByUser(User user);
}
