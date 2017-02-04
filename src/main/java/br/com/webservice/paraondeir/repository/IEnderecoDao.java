package br.com.webservice.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.webservice.paraondeir.model.Endereco;

public interface IEnderecoDao extends JpaRepository<Endereco, Integer> {

}
