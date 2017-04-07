package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeir.model.Estabelecimento;

public interface IEstabelecimentoDao extends JpaRepository<Estabelecimento, Integer> {

}
