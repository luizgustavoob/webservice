package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.paraondeir.model.Endereco;

@Repository
public interface IEnderecoDao extends JpaRepository<Endereco, Integer> {

}
