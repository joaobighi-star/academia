package com.academia.repository;

import com.academia.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    /**
     * Busca um aluno através do e-mail do usuário vinculado.
     * Útil para recuperar o perfil do aluno logado via Spring Security.
     */
    Optional<Aluno> findByUsuarioEmail(String email);
    
}