package br.com.paraondeir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.paraondeir.model.Cidade;

@Repository
public interface ICidadeDao extends JpaRepository<Cidade, Integer>{

}
