package com.academia.repository;

import com.academia.model.AulaParticular;
import com.academia.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AulaParticularRepository extends JpaRepository<AulaParticular, Long> {
    
    List<AulaParticular> findByProfessor(Professor professor);

    // Busca as aulas de um professor em um intervalo de tempo (para a grade semanal)
    List<AulaParticular> findByProfessorAndDataHoraBetween(
        Professor professor, 
        LocalDateTime inicio, 
        LocalDateTime fim
    );
}