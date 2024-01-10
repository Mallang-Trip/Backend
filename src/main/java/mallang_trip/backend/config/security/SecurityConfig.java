package mallang_trip.backend.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .httpBasic().disable()
            .cors()
            .and()

            .csrf().disable()
            .formLogin().disable()

            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            .authorizeRequests()
            .antMatchers("/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs").permitAll() // swagger
            .antMatchers("/check", "/signup", "/login", "/refresh-token", "/check-duplication", "/certification/**").permitAll() // User API
            .antMatchers("/upload/signup").permitAll() // fileUpload API
            .antMatchers(HttpMethod.GET, "/destination/**").permitAll() // destination API
            .antMatchers(HttpMethod.GET, "/driver/**").permitAll() // driver API
            .antMatchers(HttpMethod.GET, "/course/**").permitAll() // course API
            .antMatchers(HttpMethod.GET, "/article/**").permitAll() // article API
            .antMatchers(HttpMethod.GET, "/party/**").permitAll() // party API
            .antMatchers("/ws/chat/**").permitAll() // STOMP chat
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated()

            .and()
            .apply(new JwtSecurityConfig(tokenProvider));

        return http.build();
    }

    /**
     * PasswordEncoder 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
