package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.paraondeir.model.Estabelecimento;

@Repository
public interface IEstabelecimentoDao extends JpaRepository<Estabelecimento, Integer> {

}
