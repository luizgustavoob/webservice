drop table firebase;

create table if not exists usuario (
	usuario varchar(50) not null,
	fcmid varchar(100),
	constraint pk_usuario primary key (usuario)
);