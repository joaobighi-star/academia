package com.academia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // AJUSTE SEO: Permitindo acesso público aos arquivos de SEO e recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/img/**", "/robots.txt", "/sitemap.xml").permitAll()
                
                // Rotas de autenticação pública
                .requestMatchers("/recuperar-senha/**", "/login").permitAll()
                
                // Controle de acesso por ROLES
                .requestMatchers("/aluno/foto/**").hasAnyRole("ADMIN", "PROFESSOR", "ALUNO")
                .requestMatchers("/painel-mestre-bjj/**").hasRole("ADMIN")
                .requestMatchers("/professor/**").hasRole("PROFESSOR")
                .requestMatchers("/aluno/**").hasRole("ALUNO")
                
                // Qualquer outra rota exige login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(customAccessDeniedHandler())
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // Em vez de redirecionar, envia um erro 403 puro (mais seguro para APIs e rotas internas)
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        };
    }
}