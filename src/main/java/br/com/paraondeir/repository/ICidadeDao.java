package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.paraondeir.model.Cidade;

public interface ICidadeDao extends JpaRepository<Cidade, Integer>{

}
