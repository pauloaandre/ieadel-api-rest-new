package ex.model.repository;

import ex.model.Congregacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CongregacaoRepository extends JpaRepository<Congregacao, Long> {
    List<Congregacao> findAll();

}
