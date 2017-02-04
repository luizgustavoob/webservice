package br.com.webservice.paraondeir.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.webservice.paraondeir.model.Avaliacao;

public interface IAvaliacaoDao extends JpaRepository<Avaliacao, Integer> {
	
	@Query(value = "SELECT COUNT(IDAVALIACAO) "
				  + " FROM AVALIACAO A "
				  + "WHERE A.IDESTABELECIMENTO IN :idestabelecimento", nativeQuery = true)
	int countAvaliacoesByEstabelecimentos(@Param("idestabelecimento") List<Integer> idsEstabelecimento);

	@Query(value = "SELECT COUNT(IDAVALIACAO) "
			      + " FROM AVALIACAO A"
			      + " WHERE A.IDESTABELECIMENTO IN :idsestabelecimento "
			      + "   AND A.GOSTOU = :gostou", nativeQuery = true)
	int countAvaliacoesByEstabelecimentosAndGostou(
			@Param("idsestabelecimento") List<Integer> idsEstabelecimento, @Param("gostou") String gostou);
}