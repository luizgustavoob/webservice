package br.com.paraondeirwebservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.paraondeirwebservice.model.Avaliacao;

public interface IAvaliacaoDao extends JpaRepository<Avaliacao, Integer> {

	@Query(value = "SELECT DISTINCT USUARIO " 
				 + "  FROM AVALIACAO "
				 + " ORDER BY USUARIO", nativeQuery = true)
	List<String> findUsuariosAvaliacao();

	@Query(value = "SELECT DISTINCT A.IDESTABELECIMENTO "
				 + "  FROM AVALIACAO A " 
				 + " WHERE A.USUARIO = :usuario "
				 + " ORDER BY A.IDESTABELECIMENTO ", nativeQuery = true)
	List<Integer> findEstabsByUsuario(@Param("usuario") String usuario);

	@Query(value = "SELECT DISTINCT A.IDESTABELECIMENTO "
				 + "  FROM AVALIACAO A " 
				 + " WHERE A.USUARIO = :usuario "
				 + "   AND A.GOSTOU = :gostou " 
				 + " ORDER BY A.IDESTABELECIMENTO", nativeQuery = true)
	List<Integer> findEstabsByUsuarioAndGostou(
			@Param("usuario") String usuario, @Param("gostou") String gostou);

	@Query(value = "SELECT A.AVALIACAOID" 
				 + "  FROM AVALIACAO A"
				 + " WHERE A.USUARIO = :usuario"
				 + "   AND A.IDESTABELECIMENTO = :idestabelecimento", nativeQuery = true)
	int findIdAvaliacaoByUsuarioAndIdEstabelecimento(
			@Param("usuario") String usuario,
			@Param("idestabelecimento") int idEstabelecimento);
}