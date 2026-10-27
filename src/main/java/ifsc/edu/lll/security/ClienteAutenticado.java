package ifsc.edu.lll.security;

import java.time.Instant;

/**
 * Dados do cliente extraídos de um token válido.
 * É o principal do Spring Security.
 */
public record ClienteAutenticado(Long id, String email, Instant expiraEm) {}
