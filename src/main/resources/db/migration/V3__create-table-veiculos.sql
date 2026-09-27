create table veiculos(
    id bigint not null auto_increment,
    marca varchar(80) not null,
    modelo varchar(80) not null,
    versao varchar(80) not null,
    ano int not null,
    ativo boolean not null default true,

    primary key(id),
    unique key uk_veiculo (marca, modelo, versao, ano)
);
