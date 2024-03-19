package co.booleanuk.bargain.controllers;

import co.booleanuk.bargain.models.User;
import co.booleanuk.bargain.models.requests.LoginRequest;
import co.booleanuk.bargain.models.requests.SignupRequest;
import co.booleanuk.bargain.models.responses.MessageResponse;
import co.booleanuk.bargain.models.responses.TokenResponse;
import co.booleanuk.bargain.security.CustomUserDetails;
import co.booleanuk.bargain.security.jwt.JwtUtil;
import co.booleanuk.bargain.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@AllArgsConstructor
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // Generating JWT token for the authenticated user
        String jwt = jwtUtil.generateToken(userDetails);

        // Returning the JWT token along with user details in the response
        return ResponseEntity.ok(new TokenResponse(jwt, userDetails.getId(), userDetails.getUsername()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        // Checking if the email is already in use
        if (userService.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Error: Email is already in use"));
        }

        // Creating a new User object and setting its properties from the signup request
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setPhone(signupRequest.getPhone());
        // Encoding the password before storing it
        user.setPassword(encoder.encode(signupRequest.getPassword()));

        if (user.notValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: Missing fields"));
        }

        user = userService.saveUser(user);

        // Returning the saved user details in the response
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
