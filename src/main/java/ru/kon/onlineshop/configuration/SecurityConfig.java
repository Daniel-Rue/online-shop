package ru.kon.onlineshop.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.kon.onlineshop.security.auth.JwtAuthEntryPoint;
import ru.kon.onlineshop.security.auth.JwtAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthEntryPoint authEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(authEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll() // Аутентификация/Регистрация
                .antMatchers(HttpMethod.GET, "/api/products", "/api/products/").permitAll()

                // Разрешает GET /products/{id}, /products/{id}/reviews, /products/{id}/rating и т.д.
                .antMatchers(HttpMethod.GET, "/api/products/{productId:\\d+}/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                // Эндпоинты для любых аутентифицированных пользователей
                .antMatchers(HttpMethod.POST, "/api/products/{productId:\\d+}/reviews").authenticated()
                .antMatchers("/api/cart/**").authenticated() // Корзина пользователя

                // Эндпоинты ТОЛЬКО для ADMIN
                .antMatchers(HttpMethod.DELETE, "/api/reviews/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/products", "/api/products/{productId:\\d+}/categories").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/products/{productId:\\d+}/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/products/{productId:\\d+}/**").hasRole("ADMIN")

                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

}