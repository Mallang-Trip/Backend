package mallang_trip.backend.repository.destination;

import java.util.List;
import java.util.Optional;
import mallang_trip.backend.constant.DestinationType;
import mallang_trip.backend.domain.entity.destination.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

	@Query(value = "select * from destination d\n"
		+ "    where type = 'BY_ADMIN' AND deleted = 'false' AND REPLACE(d.name, ' ', '') like REPLACE(CONCAT('%',:keyword,'%'), ' ', '')\n"
		+ "        order by\n"
		+ "            case\n"
		+ "                when d.name = ':keyword' then 0\n"
		+ "                when d.name like ':keyword%' then 1\n"
		+ "                when d.name like '%:keyword' then 2\n"
		+ "                when d.name like '%:keyword%' then 3\n"
		+ "            else 4 end,\n"
		+ "            d.views DESC;", nativeQuery = true)
	List<Destination> searchByKeyword(@Param(value = "keyword") String keyword);

	List<Destination> findByTypeAndDeleted(DestinationType type, Boolean deleted);

	Optional<Destination> findByIdAndDeleted(Long id, Boolean deleted);
}
