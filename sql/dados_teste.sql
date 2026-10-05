SET NAMES utf8mb4;

USE mg_variedades;

-- Dados de exemplo apenas para testar a interface (opcional, executar uma única vez)
-- Vendedor de teste: vendedor | vendedor123
INSERT IGNORE INTO usuario (nome, login, senha, cargo)
VALUES (
    'Vendedor Teste',
    'vendedor',
    '56976bf24998ca63e35fe4f1e2469b5751d1856003e8d16fef0aafef496ed044',
    'VENDEDOR'
);

INSERT IGNORE INTO produto (nome, codigo_barras, custo_aquisicao, preco, quantidade_atual, quantidade_minima, categoria_id)
SELECT 'Caderno 96 folhas', '7891000000011', 8.00, 15.90, 30, 5, id FROM categoria WHERE nome = 'Materiais escolares'
UNION ALL
SELECT 'Caneta azul', NULL, 0.80, 2.50, 100, 20, id FROM categoria WHERE nome = 'Materiais escolares'
UNION ALL
SELECT 'Fone de ouvido', '7891000000028', 12.00, 29.90, 15, 3, id FROM categoria WHERE nome = 'Eletrônicos'
UNION ALL
SELECT 'Bala de goma (un)', NULL, 0.10, 0.50, 500, 100, id FROM categoria WHERE nome = 'Doces e mercearia'
UNION ALL
SELECT 'Brinquedo carrinho', '7891000000035', 6.00, 14.90, 20, 4, id FROM categoria WHERE nome = 'Brinquedos'
UNION ALL
SELECT 'Elástico de cabelo', NULL, 0.50, 2.00, 80, 10, id FROM categoria WHERE nome = 'Bijuterias';

-- Abre um caixa para permitir vendas (somente teste)
INSERT INTO caixa (fundo_troco_inicial, data_hora_abertura, status, usuario_abertura_id)
SELECT 100.00, NOW(), 'ABERTO', id FROM usuario WHERE login = 'admin'
AND NOT EXISTS (SELECT 1 FROM caixa WHERE status = 'ABERTO');
