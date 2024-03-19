package co.booleanuk.bargain.services;

import co.booleanuk.bargain.models.Listing;
import co.booleanuk.bargain.repositories.ListingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;

    public Listing saveListing(Listing listing) {
        return listingRepository.save(listing);
    }

    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    public List<Listing> getListingsByTitle(String title) {
        return listingRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Listing> getListingsByCategory(String category) {
        return listingRepository.findByCategory(category);
    }

    public List<Listing> getListingsByTitleAndCategory(String title, String category) {
        return listingRepository.findByTitleContainingIgnoreCaseAndCategory(title, category);
    }

    public Optional<Listing> getListingById(UUID id) {
        return listingRepository.findById(id);
    }

    public void deleteListing(Listing listing) {
        listingRepository.delete(listing);
    }
}
