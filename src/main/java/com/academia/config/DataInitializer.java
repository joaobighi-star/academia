package com.academia.config;

import com.academia.enums.Perfil;
import com.academia.model.Usuario;
import com.academia.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.findByEmail("admin@bjj.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setEmail("admin@bjj.com");
                admin.setSenha(passwordEncoder.encode("mestre123"));
                admin.setPerfis(Set.of(Perfil.ROLE_ADMIN));
                admin.setSenhaAlteradaPeloAdmin(false);
                repository.save(admin);
            }
        };
    }
}