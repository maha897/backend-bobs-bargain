package co.booleanuk.bargain.models.responses;

import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
public class TokenResponse {
    @NonNull
    private String token;
    private String type = "Bearer";
    @NonNull
    private UUID id;
    @NonNull
    private String email;
}
