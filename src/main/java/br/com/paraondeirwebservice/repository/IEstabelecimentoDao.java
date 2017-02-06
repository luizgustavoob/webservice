package br.com.paraondeirwebservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeirwebservice.model.Estabelecimento;

public interface IEstabelecimentoDao extends JpaRepository<Estabelecimento, Integer> {

}
