package com.academia.repository;

import com.academia.model.Aluno;
import com.academia.model.Professor;
import com.academia.model.AulaParticular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AulaParticularRepository extends JpaRepository<AulaParticular, Long> {

    List<AulaParticular> findByAlunoId(Long alunoId);

    List<AulaParticular> findByAlunoIdOrderByDataHoraAsc(Long alunoId);

    List<AulaParticular> findByProfessorIdAndStatus(Long professorId, String status);

    List<AulaParticular> findByProfessorIdOrderByDataHoraAsc(Long professorId);

    List<AulaParticular> findByAlunoOrderByDataHoraAsc(Aluno aluno);
    
    List<AulaParticular> findByProfessorOrderByDataHoraAsc(Professor professor);
}
