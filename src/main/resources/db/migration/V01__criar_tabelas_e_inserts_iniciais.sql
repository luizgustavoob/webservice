create sequence uf_id start 1 increment 1;

create table if not exists uf (
	iduf integer not null default nextval('uf_id'),
	nome varchar(100) not null,
	sigla varchar(2) unique not null,
	constraint pk_uf primary key (iduf)
);

create sequence cidade_id start 1 increment 1;

create table if not exists cidade (
	idcidade integer not null default nextval('cidade_id'),
	nome varchar(100) not null,
	iduf integer not null,
	constraint pk_cidade primary key (idcidade),
	constraint fk_cidade_uf foreign key (iduf) references uf (iduf)
);

create sequence endereco_id start 1 increment 1;

create table if not exists endereco (
	idend integer not null default nextval('endereco_id'),
	logradouro varchar(100) not null,
	bairro varchar(100) not null,
	numero varchar(10) not null,
	cep varchar(20) not null,
	idcidade integer not null,
	constraint pk_endereco primary key (idend),
	constraint fk_endereco_cidade foreign key (idcidade) references cidade(idcidade)
);

create domain largeobject as oid;

create sequence estabelecimento_id start 1 increment 1;

create table if not exists estabelecimento (
	idestabelecimento integer not null default nextval('estabelecimento_id'),
	nome varchar(100) not null,
	idend integer not null,
	telefone varchar(20),
	imagem largeobject,
	constraint pk_estabelecimento primary key (idestabelecimento),
	constraint fk_estabelecimento_endereco foreign key (idend) references endereco (idend)
);

create table if not exists avaliacao (	
	idestabelecimento integer not null,
	gostou varchar(1) not null,
	usuario varchar(100) not null,
	constraint pk_avaliacao primary key (idestabelecimento, usuario),
	constraint fk_avaliacao_estabelecimento foreign key (idestabelecimento) references estabelecimento(idestabelecimento)
);

create table registro_deletado(
  nome_tabela varchar(50) not null,
  id_tabela integer not null,
  data_exclusao timestamp not null,
  constraint pk_registro_deletado primary key (nome_tabela, id_tabela)
);

commit;

create or replace function uf_deletada() returns trigger as $grava_sinc_uf$
begin
    insert into registro_deletado (nome_tabela, id_tabela, data_exclusao) 
         values ('uf', old.iduf, current_timestamp);
    return old;
end
$grava_sinc_uf$ LANGUAGE plpgsql;

create trigger grava_sinc_uf after delete on uf
for each row
execute procedure uf_deletada();

/**********************************************************************************/
create or replace function cidade_deletada() returns trigger as $grava_sinc_cidade$
begin
    insert into registro_deletado (nome_tabela, id_tabela, data_exclusao) 
         values ('cidade', old.idcidade, current_timestamp);
    return old;
end
$grava_sinc_cidade$ LANGUAGE plpgsql;

create trigger grava_sinc_cidade after delete on cidade
for each row
execute procedure cidade_deletada();

/**********************************************************************************/
create or replace function endereco_deletado() returns trigger as $grava_sinc_endereco$
begin
    insert into registro_deletado (nome_tabela, id_tabela, data_exclusao) 
         values ('endereco', old.idend, current_timestamp);
    return old;
end
$grava_sinc_endereco$ LANGUAGE plpgsql;

create trigger grava_sinc_endereco after delete on endereco
for each row
execute procedure endereco_deletado();

/**********************************************************************************/
create or replace function estabelecimento_deletado() returns trigger as $grava_sinc_estabelecimento$
begin
    insert into registro_deletado (nome_tabela, id_tabela, data_exclusao) 
         values ('estabelecimento', old.idestabelecimento, current_timestamp);
    return old;
end
$grava_sinc_estabelecimento$ LANGUAGE plpgsql;

create trigger grava_sinc_estabelecimento after delete on estabelecimento
for each row
execute procedure estabelecimento_deletado();

commit;

insert into uf (nome, sigla) values ('Paraná', 'PR');
insert into cidade (nome, iduf) values ('Pato Branco', 1);

-- Endereços.
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Assis Brasil', 'Brasília', '115', '85504011', 1); -- REST. FERREIRA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Tocantins', 'Centro', '2034', '85501272', 1); -- L'CASA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Guarani', 'Centro', '713', '85501036', 1); -- PALADAR
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Jaciretã', 'Centro', '67', '85504440', 1); -- BELA CASA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Paraná', 'Centro', '379', '85501074', 1);  -- UNIVERSITÁRIO
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Arariboia', 'Centro', '172', '85501260', 1); -- MARLY
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', '2125', '85501284', 1); -- ESTÂNCIA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Tamoio', 'Centro', '541', '85501067', 1); -- LEBLON
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Paraná', 'Centro', '368', '85501074', 1); -- GAÚCHA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Tapajós', 'Centro', '435', '85501043', 1); -- PEQUIM
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Pedro Ramires de Mello', 'Centro', '223', '85501250', 1); -- DIVINO SABOR
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Tamoio', 'Centro', '774', ' 	85501054', 1); -- SABOR E ARTE
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Itabira', 'Centro', '1276', '85501047', 1); -- BOCATTA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Caramuru', 'Centro', '802', '85501034', 1); -- PATO LANCHES
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Goianases', 'Centro', '557', '85501020', 1); -- CASARAO
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', 'S/N', '85504000', 1); -- BENEDITO
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Guarani', 'Centro', '912', '85501036', 1); -- CAFETERIA POSTO Guarani
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Vila Izabel', '885', '85504288', 1); -- CACHAÇARIA AGUA DOCE
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Itabira', 'Centro', '1597', '85501286', 1); -- HAMBURGUERIA DO CHEF
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Iguaçu', 'Centro', '790', '85501266', 1); -- PANIFICADORA ITALIA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', '1530', '85501039', 1); --SUBWAY
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', '1480', '85501039', 1); --DOM BURGUER
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', '1967', '85501284', 1); --BABAGANOUSH
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Luiz Favretto', 'Centro', '15', '85505150', 1); -- BELA CASA GRILL
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', '2200', '85501063', 1); -- SANTORINI GRILL
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', '2470', '85501065', 1); -- CAFÉ DA PRAÇA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Itabira', 'Centro', '1720', '85501286', 1); -- PIZZARIA VISA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Guarani', 'Centro', '282', '85501048', 1); -- LETRA CAFÉ
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Brasília', '1423', '85504014', 1); --SABIA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Ibiporã', 'Centro', '305', '85501056', 1); --NONNA JOANA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', '1571', '85504014', 1); -- CREPE
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Assis Brasil', 'Brasília', '219', '85504011', 1); --YUJ SUSHI HOUSE
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', '1455', '85504014', 1); --BODEGUERO
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Ibiporã', 'Centro', '379', '85501049', 1); -- PASTELARIA TONY
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Assis Brasil', 'Brasília', '126', '85504011', 1); -- MECANICA MEAT'N BEER
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Tapajós', 'Centro', '366', '85501045', 1); -- FRANGOS E FRITAS
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Tamoio', 'Centro', '695', '85501067', 1); --GORDÃO
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Tupi', 'Centro', '3155', '85501274', 1); -- CALÇADÃO
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Tapajós', 'Centro', '319', ' 	85501045', 1); -- THABERNA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Itacolomi', 'Centro', '910', '85501240', 1); -- CRAVO E CANELA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Iguaçu', 'Centro', '815', '85501266', 1); --MARABÁ
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Pioneiro Alberto Braun', 'La Salle', '360', '85505100', 1); --HONG KONG
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Xingú', 'Centro', '238', '85501230', 1); --SANTA TEREZINHA
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Avenida Brasil', 'Centro', '7', '85501071', 1); --ENGENHO DO SABOR
insert into endereco (logradouro, bairro, numero, cep, idcidade) values ('Rua Paraná', 'Centro', '379', '85501074', 1); --PROTEINA

-- Estabelecimentos
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Ferreira', 1, '(46) 3225-4374', lo_import('c:\temp\padrao.jpg')); -- REST. FERREIRA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante L"Casa', 2, '(46) 9911-9054', lo_import('c:\temp\padrao.jpg')); -- L'CASA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Paladar', 3, '(46) 3025-5385', lo_import('c:\temp\padrao.jpg')); -- PALADAR
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Bela Casa', 4, '(46) 3224-1340', lo_import('c:\temp\padrao.jpg')); -- BELA CASA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Universitário', 5, '(46) 9113-5798', lo_import('c:\temp\padrao.jpg'));  -- UNIVERSITÁRIO
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Mary', 6, '(46) 3224-5768', lo_import('c:\temp\padrao.jpg')); -- MARLY
insert into estabelecimento (nome, idend, telefone, imagem) values ('Estância Restaurante', 7, '(46) 3224-6759', lo_import('c:\temp\padrao.jpg')); -- ESTÂNCIA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Leblon', 8, '(46) 3225-7778', lo_import('c:\temp\padrao.jpg')); -- LEBLON
insert into estabelecimento (nome, idend, telefone, imagem) values ('Churrascaria Gaúcha', 9, '(46) 3224-8457', lo_import('c:\temp\padrao.jpg')); -- GAÚCHA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Pequim', 10, '(46) 3225-1745', lo_import('c:\temp\padrao.jpg')); -- PEQUIM
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Divino Sabor', 11, '(46) 3225-7592', lo_import('c:\temp\padrao.jpg')); -- DIVINO SABOR
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Sabor e Arte', 12, '(46) 3224-6867', lo_import('c:\temp\padrao.jpg')); -- SABOR E ARTE
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Bocatta', 13, '(46) 3225-4064', lo_import('c:\temp\padrao.jpg')); -- BOCATTA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Patô Lanches', 14, '(46) 3225-5165', lo_import('c:\temp\padrao.jpg')); -- PATO LANCHES
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Casarão', 15, '(46) 3225-0839', lo_import('c:\temp\padrao.jpg')); -- CASARAO
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Benedito', 16, '(46) 3223-4857', lo_import('c:\temp\padrao.jpg')); -- BENEDITO
insert into estabelecimento (nome, idend, telefone, imagem) values ('Cafeteria Posto Guarani', 17, '(46) 3225-7499', lo_import('c:\temp\padrao.jpg')); -- CAFETERIA POSTO Guarani
insert into estabelecimento (nome, idend, telefone, imagem) values ('Cachaçaria Água Doce', 18, '(46) 3224-4000', lo_import('c:\temp\padrao.jpg')); -- CACHAÇARIA AGUA DOCE
insert into estabelecimento (nome, idend, telefone, imagem) values ('Hamburgueria do Chef', 19, '(46) 3025-2300', lo_import('c:\temp\padrao.jpg')); -- HAMBURGUERIA DO CHEF
insert into estabelecimento (nome, idend, telefone, imagem) values ('Panificadora Itália', 20, '(46) 3225-2934', lo_import('c:\temp\padrao.jpg')); -- PANIFICADORA ITALIA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Subway', 21, '(46) 3025-2377', lo_import('c:\temp\padrao.jpg')); --SUBWAY
insert into estabelecimento (nome, idend, telefone, imagem) values ('Dom Burguer', 22, '(46) 3025-1150', lo_import('c:\temp\padrao.jpg')); --DOM BURGUER
insert into estabelecimento (nome, idend, telefone, imagem) values ('Babaganoush', 23, '(46) 3025-5473', lo_import('c:\temp\padrao.jpg')); --BABAGANOUSH
insert into estabelecimento (nome, idend, telefone, imagem) values ('Bela Casa Grill', 24, '(46) 3025-1341', lo_import('c:\temp\padrao.jpg')); -- BELA CASA GRILL
insert into estabelecimento (nome, idend, telefone, imagem) values ('Santorini Grill', 25, '(46) 3225-2518', lo_import('c:\temp\padrao.jpg')); -- SANTORINI GRILL
insert into estabelecimento (nome, idend, telefone, imagem) values ('Café da Praça', 26, '(46) 3025-4015', lo_import('c:\temp\padrao.jpg')); -- CAFÉ DA PRAÇA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Pizzaria Visa', 27, '(46) 3025-2345', lo_import('c:\temp\padrao.jpg')); -- PIZZARIA VISA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Letra Café', 28, '(46) 3225-3412', lo_import('c:\temp\padrao.jpg')); -- LETRA CAFÉ
insert into estabelecimento (nome, idend, telefone, imagem) values ('Lanchonete Sabiá', 29, '(46) 3224-1377', lo_import('c:\temp\padrao.jpg')); --SABIA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Nonna Joana', 30, '(46) 3025-5015', lo_import('c:\temp\padrao.jpg')); --NONNA JOANA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Mr. Crepe', 31, '(46) 3224-1072', lo_import('c:\temp\padrao.jpg')); -- CREPE
insert into estabelecimento (nome, idend, telefone, imagem) values ('Yuj Sushi House', 32, '(46) 3225-5533', lo_import('c:\temp\padrao.jpg')); --YUJ SUSHI HOUSE
insert into estabelecimento (nome, idend, telefone, imagem) values ('Bodeguero', 33, '(46) 3025-5110', lo_import('c:\temp\padrao.jpg')); --BODEGUERO
insert into estabelecimento (nome, idend, telefone, imagem) values ('Pastelaria do Tony', 34, '(46) 3225-6493', lo_import('c:\temp\padrao.jpg')); -- PASTELARIA TONY
insert into estabelecimento (nome, idend, telefone, imagem) values ('Mecânica Meat"n Beer', 35, '(41) 9676-9697', lo_import('c:\temp\padrao.jpg')); -- MECANICA MEAT'N BEER
insert into estabelecimento (nome, idend, telefone, imagem) values ('Frangos e Fritas', 36, '(46) 3025-2366', lo_import('c:\temp\padrao.jpg')); -- FRANGOS E FRITAS
insert into estabelecimento (nome, idend, telefone, imagem) values ('Lanchonete Gordão', 37, '(46) 3225-7443', lo_import('c:\temp\padrao.jpg')); --GORDÃO
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Calçadão', 38, '(46) 3225-3815', lo_import('c:\temp\padrao.jpg')); -- CALÇADÃO
insert into estabelecimento (nome, idend, telefone, imagem) values ('Pizzaria Thaberna', 39, '(46) 3225-5050', lo_import('c:\temp\padrao.jpg')); -- THABERNA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Panificadora Cravo e Canela', 40, '(46) 3225-8896', lo_import('c:\temp\padrao.jpg')); -- CRAVO E CANELA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Marabá', 41, '(46) 3225-3288', lo_import('c:\temp\padrao.jpg')); --MARABÁ
insert into estabelecimento (nome, idend, telefone, imagem) values ('Hong Kong', 42, '(46) 3224-4007', lo_import('c:\temp\padrao.jpg')); --HONG KONG
insert into estabelecimento (nome, idend, telefone, imagem) values ('Panificadora Santa Terezinha', 43, '(46) 3224-1638', lo_import('c:\temp\padrao.jpg')); --SANTA TEREZINHA
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Engenho do Sabor', 44, '(46) 3025-1333', lo_import('c:\temp\padrao.jpg')); --ENGENHO DO SABOR
insert into estabelecimento (nome, idend, telefone, imagem) values ('Restaurante Proteína', 45, '(46) 3224-1500', lo_import('c:\temp\padrao.jpg')); --PROTEINA

commit;