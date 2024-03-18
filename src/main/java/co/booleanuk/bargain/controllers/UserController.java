package co.booleanuk.bargain.controllers;

import co.booleanuk.bargain.models.User;
import co.booleanuk.bargain.models.responses.MessageResponse;
import co.booleanuk.bargain.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder encoder;

    @GetMapping("{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        Optional<User> user = this.userService.getUserById(id);
        return user.map(value -> ResponseEntity.status(HttpStatus.OK).body(value)).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        Optional<User> userToUpdateOptional = this.userService.getUserById(id);

        if (userToUpdateOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (!currentUserEmail.equals(userToUpdateOptional.get().getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User userToUpdate = userToUpdateOptional.get();

        userToUpdate.setEmail(user.getEmail() != null ? user.getEmail() : userToUpdate.getEmail());
        userToUpdate.setFirstName(user.getFirstName() != null ? user.getFirstName() : userToUpdate.getFirstName());
        userToUpdate.setLastName(user.getLastName() != null ? user.getLastName() : userToUpdate.getLastName());
        userToUpdate.setPhone(user.getPhone() != null ? user.getPhone() : userToUpdate.getPhone());
        userToUpdate.setPassword(user.getPassword() != null ? encoder.encode(user.getPassword()) : userToUpdate.getPassword());
        userToUpdate.setUpdatedAt(ZonedDateTime.now());

        userToUpdate = this.userService.saveUser(userToUpdate);

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.saveUser(userToUpdate));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteById(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        Optional<User> userToDeleteOptional = this.userService.getUserById(id);
        if (userToDeleteOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User userToDelete = userToDeleteOptional.get();
        if (!currentUserEmail.equals(userToDelete.getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            this.userService.deleteUser(userToDelete);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error: Violation of foreign key constraint"));
        }
        return ResponseEntity.ok(userToDelete);
    }
}
