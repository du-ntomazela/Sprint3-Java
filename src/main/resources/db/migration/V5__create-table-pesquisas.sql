create table pesquisas(
    id bigint not null auto_increment,
    usuario_id bigint not null,
    marca varchar(80) not null,
    modelo varchar(80) not null,
    versao varchar(80) not null,
    atributos_solicitados json not null,
    resultado json not null,
    criada_em datetime not null,

    primary key(id),
    constraint fk_pesquisa_usuario foreign key (usuario_id) references usuarios(id)
);
