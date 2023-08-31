package mallang_trip.backend.repository.party;

import java.time.LocalDate;
import java.util.List;
import mallang_trip.backend.domain.entity.party.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {

    @Query(value = "SELECT * FROM party "
        + "WHERE region = :region AND startDate = :startDate AND capacity - headcount >= :headcount "
        + "AND status = 'RECRUITING'"
        + "ORDER BY updated_at DESC ", nativeQuery = true)
    List<Party> findParties(@Param(value = "region") String region,
        @Param(value = "headcount") Integer headcount,
        @Param(value = "startDate") LocalDate startDate);
}
