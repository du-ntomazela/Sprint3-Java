create table especificacoes_veiculo(
    id bigint not null auto_increment,
    veiculo_id bigint not null,
    atributo_id bigint not null,
    valor varchar(255) not null,
    fonte varchar(255),

    primary key(id),
    unique key uk_especificacao (veiculo_id, atributo_id),
    constraint fk_especificacao_veiculo foreign key (veiculo_id) references veiculos(id),
    constraint fk_especificacao_atributo foreign key (atributo_id) references atributos(id)
);
