package br.com.paraondeirwebservice.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.paraondeirwebservice.model.RegistroDeletado;
import br.com.paraondeirwebservice.model.RegistroDeletadoPK;

public interface IRegistroDeletadoDao extends JpaRepository<RegistroDeletado, RegistroDeletadoPK>{

	@Query(value = "select * from registro_deletado where data_exclusao <= :data", nativeQuery = true)
	List<RegistroDeletado> findRegistrosDeletadosGreaterThanData(@Param("data") Timestamp data);
}
