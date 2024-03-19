package co.booleanuk.bargain.repositories;

import co.booleanuk.bargain.models.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByTitleContainingIgnoreCase(String title);
    List<Listing> findByCategory(String category);
    List<Listing> findByTitleContainingIgnoreCaseAndCategory(String title, String category);
}
