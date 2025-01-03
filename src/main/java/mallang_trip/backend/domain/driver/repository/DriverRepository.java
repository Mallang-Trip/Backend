package mallang_trip.backend.domain.driver.repository;

import feign.Param;
import java.util.List;
import java.util.Optional;
import mallang_trip.backend.domain.driver.constant.DriverStatus;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findAllByStatus(DriverStatus status);

    Optional<Driver> findByIdAndStatus(Long id, DriverStatus status);

    Optional<Driver> findByUser(User user);

    @Query(value = "SELECT d.*, r.region "
            + "FROM driver d INNER JOIN driver_region r "
            + "ON d.id = r.driver_id "
            + "WHERE (:region IS NULL OR r.region = :region) AND d.status = 'ACCEPTED' "
            + "GROUP BY d.id"
            , nativeQuery = true)
    List<Driver> findByRegionContaining(@Param("region") String region);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END "
        + "FROM driver d JOIN driver_region r ON d.id = r.driver_id "
        + "WHERE r.region = :region AND d.status = 'ACCEPTED' ", nativeQuery = true)
    Boolean existsByRegion(@Param("region") String region);

    Long countByStatus(DriverStatus status);
}
