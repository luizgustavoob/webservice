package br.com.paraondeirwebservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeirwebservice.model.Endereco;

public interface IEnderecoDao extends JpaRepository<Endereco, Integer> {

}
