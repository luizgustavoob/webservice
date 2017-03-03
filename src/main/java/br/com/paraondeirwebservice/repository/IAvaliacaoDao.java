package br.com.paraondeirwebservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.paraondeirwebservice.model.Avaliacao;

public interface IAvaliacaoDao extends JpaRepository<Avaliacao, Integer> {
	
	@Query(value = "SELECT COUNT(AVALIACAOID) "
				  + " FROM AVALIACAO A "
				  + "WHERE A.IDESTABELECIMENTO IN :idestabelecimento", nativeQuery = true)
	int countAvaliacoesByEstabelecimentos(@Param("idestabelecimento") List<Integer> idsEstabelecimento);

	@Query(value = "SELECT COUNT(AVALIACAOID) "
			      + " FROM AVALIACAO A"
			      + " WHERE A.IDESTABELECIMENTO IN :idsestabelecimento "
			      + "   AND A.GOSTOU = :gostou", nativeQuery = true)
	int countAvaliacoesByEstabelecimentosAndGostou(
			@Param("idsestabelecimento") List<Integer> idsEstabelecimento, @Param("gostou") String gostou);
	
	@Query(value = "SELECT A.AVALIACAOID"
			      + " FROM AVALIACAO A"
			      + " WHERE A.USUARIO = :usuario"
			      + "   AND A.IDESTABELECIMENTO = :idestabelecimento", nativeQuery = true)
	int findIdAvaliacaoByUsuarioAndIdEstabelecimento(@Param("usuario") String usuario, 
			@Param("idestabelecimento") int idEstabelecimento);
}