create table usuarios(
    id bigint not null auto_increment,
    nome varchar(150) not null,
    login varchar(150) not null unique,
    senha varchar(255) not null,
    perfil varchar(20) not null,
    ativo boolean not null default true,

    primary key(id)
);
