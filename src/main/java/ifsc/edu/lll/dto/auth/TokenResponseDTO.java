package ifsc.edu.lll.dto.auth;

public record TokenResponseDTO(String accessToken, String tokenType, long expiresIn) {}
