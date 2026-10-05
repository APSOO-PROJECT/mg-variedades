SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS mg_variedades CHARACTER SET utf8mb4;

USE mg_variedades;

CREATE TABLE
    IF NOT EXISTS usuario (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nome VARCHAR(50) NOT NULL,
        login VARCHAR(30) NOT NULL UNIQUE,
        senha CHAR(64) NOT NULL,
        cargo ENUM ('ADMIN', 'VENDEDOR') NOT NULL,
        status ENUM ('ATIVO', 'INATIVO') NOT NULL DEFAULT 'ATIVO',
        seed BOOLEAN NOT NULL DEFAULT FALSE,
        data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE
    IF NOT EXISTS categoria (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nome VARCHAR(40) NOT NULL UNIQUE,
        prazo_devolucao_horas INT NOT NULL DEFAULT 168,
        status ENUM ('ATIVO', 'INATIVO') NOT NULL DEFAULT 'ATIVO'
    );

CREATE TABLE
    IF NOT EXISTS categoria_historico (
        id INT AUTO_INCREMENT PRIMARY KEY,
        categoria_id INT NOT NULL,
        data_alteracao DATETIME NOT NULL,
        campo VARCHAR(30) NOT NULL,
        valor_anterior VARCHAR(60) NOT NULL,
        valor_novo VARCHAR(60) NOT NULL,
        usuario_id INT NOT NULL,
        FOREIGN KEY (categoria_id) REFERENCES categoria (id),
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
    );

CREATE TABLE
    IF NOT EXISTS produto (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nome VARCHAR(60) NOT NULL,
        codigo_barras VARCHAR(20) UNIQUE,
        custo_aquisicao DECIMAL(10, 2) NOT NULL,
        preco DECIMAL(10, 2) NOT NULL,
        quantidade_atual INT NOT NULL DEFAULT 0,
        quantidade_minima INT NOT NULL DEFAULT 0,
        status ENUM ('DISPONIVEL', 'INDISPONIVEL') NOT NULL DEFAULT 'DISPONIVEL',
        categoria_id INT NOT NULL,
        data_adicao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        data_atualizacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        usuario_atualizacao_id INT,
        FOREIGN KEY (categoria_id) REFERENCES categoria (id),
        FOREIGN KEY (usuario_atualizacao_id) REFERENCES usuario (id)
    );

CREATE TABLE
    IF NOT EXISTS caixa (
        id INT AUTO_INCREMENT PRIMARY KEY,
        fundo_troco_inicial DECIMAL(10, 2) NOT NULL,
        data_hora_abertura DATETIME NOT NULL,
        data_hora_fechamento DATETIME,
        saldo_esperado DECIMAL(10, 2),
        saldo_real DECIMAL(10, 2),
        justificativa_quebra VARCHAR(200),
        status ENUM ('ABERTO', 'FECHADO') NOT NULL DEFAULT 'ABERTO',
        usuario_abertura_id INT NOT NULL,
        usuario_fechamento_id INT,
        FOREIGN KEY (usuario_abertura_id) REFERENCES usuario (id),
        FOREIGN KEY (usuario_fechamento_id) REFERENCES usuario (id)
    );

CREATE TABLE
    IF NOT EXISTS movimentacao_caixa (
        id INT AUTO_INCREMENT PRIMARY KEY,
        caixa_id INT NOT NULL,
        tipo_operacao ENUM ('SANGRIA', 'SUPRIMENTO', 'REEMBOLSO') NOT NULL,
        valor DECIMAL(10, 2) NOT NULL,
        motivo VARCHAR(100) NOT NULL,
        data_hora DATETIME NOT NULL,
        usuario_id INT NOT NULL,
        FOREIGN KEY (caixa_id) REFERENCES caixa (id),
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
    );

CREATE TABLE
    IF NOT EXISTS pagamento (
        id INT AUTO_INCREMENT PRIMARY KEY,
        forma_pagamento ENUM ('DINHEIRO', 'PIX', 'CARTAO') NOT NULL,
        valor_venda DECIMAL(10, 2) NOT NULL,
        valor_recebido DECIMAL(10, 2) NOT NULL,
        caixa_id INT NOT NULL,
        FOREIGN KEY (caixa_id) REFERENCES caixa (id)
    );

CREATE TABLE
    IF NOT EXISTS dinheiro (
        id INT PRIMARY KEY,
        valor_recebido DECIMAL(10, 2) NOT NULL,
        troco DECIMAL(10, 2) NOT NULL,
        FOREIGN KEY (id) REFERENCES pagamento (id)
    );

CREATE TABLE
    IF NOT EXISTS pix (
        id INT PRIMARY KEY,
        comprovante VARCHAR(60) NOT NULL,
        FOREIGN KEY (id) REFERENCES pagamento (id)
    );

CREATE TABLE
    IF NOT EXISTS cartao (
        id INT PRIMARY KEY,
        modalidade ENUM ('DEBITO', 'CREDITO') NOT NULL,
        taxa DECIMAL(5, 2) NOT NULL DEFAULT 0,
        parcelas INT NOT NULL DEFAULT 1,
        FOREIGN KEY (id) REFERENCES pagamento (id)
    );

CREATE TABLE
    IF NOT EXISTS venda (
        id INT AUTO_INCREMENT PRIMARY KEY,
        valor_total DECIMAL(10, 2) NOT NULL,
        data_hora DATETIME NOT NULL,
        cpf_cnpj_cliente VARCHAR(14) NOT NULL,
        pagamento_id INT NOT NULL,
        usuario_id INT NOT NULL,
        FOREIGN KEY (pagamento_id) REFERENCES pagamento (id),
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
    );

CREATE TABLE
    IF NOT EXISTS item_venda (
        id INT AUTO_INCREMENT PRIMARY KEY,
        venda_id INT NOT NULL,
        produto_id INT NOT NULL,
        quantidade INT NOT NULL,
        preco_unitario DECIMAL(10, 2) NOT NULL,
        custo_unitario DECIMAL(10, 2) NOT NULL,
        subtotal DECIMAL(10, 2) NOT NULL,
        FOREIGN KEY (venda_id) REFERENCES venda (id),
        FOREIGN KEY (produto_id) REFERENCES produto (id)
    );

CREATE TABLE
    IF NOT EXISTS devolucao (
        id INT AUTO_INCREMENT PRIMARY KEY,
        venda_id INT NOT NULL,
        data_hora DATETIME NOT NULL,
        status_produto ENUM ('INTEGRAL', 'PARCIAL') NOT NULL,
        motivo VARCHAR(100) NOT NULL,
        tipo_reembolso ENUM ('DINHEIRO', 'PIX', 'CARTAO') NOT NULL,
        valor_reembolsado DECIMAL(10, 2) NOT NULL,
        usuario_id INT NOT NULL,
        FOREIGN KEY (venda_id) REFERENCES venda (id),
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
    );

CREATE TABLE
    IF NOT EXISTS item_devolucao (
        id INT AUTO_INCREMENT PRIMARY KEY,
        devolucao_id INT NOT NULL,
        item_venda_id INT NOT NULL,
        quantidade INT NOT NULL,
        FOREIGN KEY (devolucao_id) REFERENCES devolucao (id),
        FOREIGN KEY (item_venda_id) REFERENCES item_venda (id)
    );

CREATE TABLE
    IF NOT EXISTS sugestao_compra (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nome VARCHAR(60) NOT NULL,
        data_hora DATETIME NOT NULL,
        usuario_id INT NOT NULL,
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
    );

-- Administrador padrão (seed). Login: admin | Senha: admin123 (SHA-256)
INSERT IGNORE INTO usuario (nome, login, senha, cargo, status, seed)
VALUES (
    'Administrador',
    'admin',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    'ADMIN',
    'ATIVO',
    TRUE
);

-- Categorias padrão (prazo de devolução em horas)
INSERT IGNORE INTO categoria (nome, prazo_devolucao_horas) VALUES
    ('Doces e mercearia', 168),
    ('Eletrônicos', 120),
    ('Materiais escolares', 168),
    ('Utensílios domésticos', 168),
    ('Brinquedos', 168),
    ('Bijuterias', 24),
    ('Ferramentas', 168);
