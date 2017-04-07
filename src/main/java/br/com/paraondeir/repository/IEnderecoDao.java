package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeir.model.Endereco;

public interface IEnderecoDao extends JpaRepository<Endereco, Integer> {

}
