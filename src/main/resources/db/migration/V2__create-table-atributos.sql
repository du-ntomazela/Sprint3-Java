create table atributos(
    id bigint not null auto_increment,
    codigo varchar(60) not null unique,
    nome varchar(150) not null,
    unidade varchar(30),
    categoria varchar(60) not null,
    ativo boolean not null default true,

    primary key(id)
);
