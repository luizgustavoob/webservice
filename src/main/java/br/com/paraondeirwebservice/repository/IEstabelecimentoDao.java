package br.com.webservice.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.webservice.paraondeir.model.Estabelecimento;

public interface IEstabelecimentoDao extends JpaRepository<Estabelecimento, Integer> {

}
