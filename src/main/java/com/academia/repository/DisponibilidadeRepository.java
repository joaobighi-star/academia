package com.academia.repository;

import com.academia.model.Disponibilidade;
import com.academia.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadeRepository extends JpaRepository<Disponibilidade, Long> {

    // Busca todos os horários que o professor abriu naquela semana específica
    List<Disponibilidade> findByProfessorAndDataBetween(
        Professor professor, 
        LocalDate inicio, 
        LocalDate fim
    );

    // Verifica se já existe um slot aberto para não duplicar
    boolean existsByProfessorAndDataAndHora(Professor professor, LocalDate data, Integer hora);
}