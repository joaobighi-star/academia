package com.academia.repository;

import com.academia.model.Disponibilidade;
import com.academia.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadeRepository extends JpaRepository<Disponibilidade, Long> {

    // Busca para renderizar a grade da semana
    List<Disponibilidade> findByProfessorAndDataBetween(Professor professor, LocalDate inicio, LocalDate fim);

    // Busca específica para o botão de "Alternar" (Abrir/Fechar)
    Optional<Disponibilidade> findByProfessorAndDataAndHora(Professor professor, LocalDate data, Integer hora);

    boolean existsByProfessorAndDataAndHora(Professor professor, LocalDate data, Integer hora);

    List<Disponibilidade> findByProfessorIdAndDataBetweenOrderByDataAscHoraAsc(Long professorId, LocalDate inicio, LocalDate fim);
}
