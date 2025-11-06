package runnity.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import runnity.domain.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {

  Optional<Region> findByLatAndLng(Double lat, Double lng);
}
