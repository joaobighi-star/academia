package com.academia.repository;

import com.academia.model.Aluno;
import com.academia.model.Professor;
import com.academia.model.AulaParticular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AulaParticularRepository extends JpaRepository<AulaParticular, Long> {

    // Busca para o Aluno
    List<AulaParticular> findByAlunoOrderByDataHoraAsc(Aluno aluno);
    
    // Busca para o Professor (Necessário para o ProfessorController)
    List<AulaParticular> findByProfessorOrderByDataHoraAsc(Professor professor);
}