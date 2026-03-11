package eastmeet.backend5.config;

import eastmeet.backend5.filter.JwtFilter;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        httpSecurity.httpBasic(HttpBasicConfigurer::disable)
            .cors(getCorsConfigurerCustomizer())
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(FormLoginConfigurer::disable)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/auth/v1/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/v1/**").authenticated()
                    .anyRequest().denyAll()
            );

        return httpSecurity.build();
    }

    private @NonNull Customizer<CorsConfigurer<HttpSecurity>> getCorsConfigurerCustomizer() {
        return configurer -> {
            CorsConfiguration corsConfigurer = new CorsConfiguration();
            corsConfigurer.setAllowedOriginPatterns(List.of("*"));
            corsConfigurer.setAllowedMethods(
                Arrays.asList(HttpMethod.POST.name(), HttpMethod.GET.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.PATCH.name()));
            corsConfigurer.addAllowedHeader("*");
            corsConfigurer.setAllowCredentials(true);
            corsConfigurer.setMaxAge(3600L);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", corsConfigurer);
            configurer.configurationSource(source);
        };
    }


}
