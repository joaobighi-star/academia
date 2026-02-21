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
                // Recursos estáticos liberados
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                
                // Rotas públicas
                .requestMatchers("/recuperar-senha/**", "/login").permitAll()
                
                // LIBERAÇÃO DAS FOTOS: Deve vir ANTES da regra restrita do /aluno/**
                // Permite que Admin e Professores vejam as fotos dos alunos também
                .requestMatchers("/aluno/foto/**").hasAnyRole("ADMIN", "PROFESSOR", "ALUNO")
                
                // Restrições por Role
                .requestMatchers("/painel-mestre-bjj/**").hasRole("ADMIN")
                .requestMatchers("/professor/**").hasRole("PROFESSOR")
                .requestMatchers("/aluno/**").hasRole("ALUNO")
                
                // Qualquer outra requisição (como /home) exige login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true) // Direciona para o redirectByRole do LoginController
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
        // Retornamos 403 (Forbidden) para facilitar o debug de acesso negado
        return (request, response, accessDeniedException) -> {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        };
    }
}