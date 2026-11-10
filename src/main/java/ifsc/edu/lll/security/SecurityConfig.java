package ifsc.edu.lll.security;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final int rateLimit;
    private final long rateLimitJanelaMs;
    private final boolean rateLimitHabilitado;

    public SecurityConfig(
            @Value("${rate-limit.limit:100}") int rateLimit,
            @Value("${rate-limit.window-seconds:60}") long rateLimitWindowSeconds,
            @Value("${rate-limit.enabled:true}") boolean rateLimitHabilitado) {
        this.rateLimit = rateLimit;
        this.rateLimitJanelaMs = rateLimitWindowSeconds * 1000;
        this.rateLimitHabilitado = rateLimitHabilitado;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtService jwtService,
                                           RestAuthenticationEntryPoint entryPoint,
                                           RestAccessDeniedHandler deniedHandler) throws Exception {
        var rateLimitFilter = new RateLimitFilter(rateLimit, rateLimitJanelaMs, rateLimitHabilitado);
        var jwtAuthFilter = new JwtAuthenticationFilter(jwtService);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/clientes", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/previsao/**", "/ping").permitAll()
                        .requestMatchers("/docs", "/docs/**", "/docs-json", "/docs-json/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/logs", "/logs/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // custo 10 (padrão)
    }
}
