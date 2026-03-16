package ex.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ex.model.Usuario;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    UserDetails findByEmail(String email);
    List<Usuario> findAll();
    List<Usuario> findByAtivoTrue();

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND u.congregacao.idCongregacao = :idCongregacao AND u.ativo = true")
    List<Usuario> findByNomeContainingIgnoreCaseAndCongregacao(@Param("nome") String nome, @Param("idCongregacao") Long idCongregacao);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND u.ativo = true")
    List<Usuario> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    @Query("SELECT u FROM Usuario u WHERE u.congregacao.idCongregacao = :idCongregacao AND u.ativo = false AND u.nome LIKE 'Visitante - %'")
    Usuario findVisitanteByCongregacao(@Param("idCongregacao") Long idCongregacao);
}
