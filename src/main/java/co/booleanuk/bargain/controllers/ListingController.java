package co.booleanuk.bargain.controllers;

import co.booleanuk.bargain.models.Image;
import co.booleanuk.bargain.models.Listing;
import co.booleanuk.bargain.models.User;
import co.booleanuk.bargain.models.responses.MessageResponse;
import co.booleanuk.bargain.services.ListingService;
import co.booleanuk.bargain.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("listings")
@AllArgsConstructor
public class ListingController {
    private final ListingService listingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Listing listing) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Optional<User> userOptional = userService.getUserByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error: Could not map token to user"));
        }
        if (listing.notValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: Missing fields"));
        }

        // Setting the user and other properties for the listing
        listing.setUser(userOptional.get());
        listing.setSold(false);
        for (Image image : listing.getImages()) {
            image.setListing(listing);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.saveListing(listing));
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam(required = false) String q, @RequestParam(required = false) String c) {
        if (q != null && c != null) {
            // If both title and category parameters are provided, search by both
            List<Listing> listingsByTitleAndCategory = listingService.getListingsByTitleAndCategory(q, c);
            return ResponseEntity.ok(listingsByTitleAndCategory);
        } else if (q != null) {
            // If only title parameter is provided, search by title
            List<Listing> listingsByTitle = listingService.getListingsByTitle(q);
            return ResponseEntity.ok(listingsByTitle);
        } else if (c != null) {
            // If only category parameter is provided, search by category
            List<Listing> listingsByCategory = listingService.getListingsByCategory(c);
            return ResponseEntity.ok(listingsByCategory);
        } else {
            // If no parameters are provided, return all listings
            List<Listing> allListings = listingService.getAllListings();
            return ResponseEntity.ok(allListings);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        Optional<Listing> user = this.listingService.getListingById(id);
        return user.map(value -> ResponseEntity.status(HttpStatus.OK).body(value)).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateById(@PathVariable UUID id, @RequestBody Listing listing) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Optional<Listing> listingOptional = listingService.getListingById(id);
        if (listingOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Listing listingToUpdate = listingOptional.get();
        // Checking if the authenticated user is the owner of the listing
        if (!userEmail.equals(listingToUpdate.getUser().getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: You are not authorized to update this listing"));
        }

        // Updating the listing fields with new values
        listingToUpdate.setTitle(listing.getTitle() != null ? listing.getTitle() : listingToUpdate.getTitle());
        listingToUpdate.setDescription(listing.getDescription() != null ? listing.getDescription() : listingToUpdate.getDescription());
        listingToUpdate.setSold(listing.getSold() != null ? listing.getSold() : listingToUpdate.getSold());
        listingToUpdate.setPrice(listing.getPrice() != null ? listing.getPrice() : listingToUpdate.getPrice());
        listingToUpdate.setCategory(listing.getCategory() != null ? listing.getCategory() : listingToUpdate.getCategory());
        listingToUpdate.setAddress(listing.getAddress() != null ? listing.getAddress() : listingToUpdate.getAddress());
        listingToUpdate.setCity(listing.getCity() != null ? listing.getCity() : listingToUpdate.getCity());
        listingToUpdate.setPostcode(listing.getPostcode() != null ? listing.getPostcode() : listingToUpdate.getPostcode());
        if (!listing.getImages().isEmpty()) {
            listingToUpdate.setImages(listing.getImages());
            for (Image image : listing.getImages()) {
                image.setListing(listingToUpdate);
            }
        }
        listingToUpdate.setUpdatedAt(ZonedDateTime.now());

        // Saving the updated listing and returning the response
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.saveListing(listingToUpdate));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteById(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Optional<Listing> listingOptional = listingService.getListingById(id);
        if (listingOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Listing listingToDelete = listingOptional.get();
        if (!userEmail.equals(listingToDelete.getUser().getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Deleting the listing and handling any exceptions
        try {
            this.listingService.deleteListing(listingToDelete);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error: Violation of foreign key constraint"));
        }
        return ResponseEntity.ok(listingToDelete);
    }
}
