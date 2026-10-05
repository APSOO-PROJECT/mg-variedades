# MG Variedades - Sistema de Vendas

Projeto da disciplina APSOO. Interface em JavaFX (tema próprio inspirado no AtlantaFX), acesso a dados com DAO e banco MySQL.

## Arquitetura

```text
sql/
├── bd.sql              (esquema + admin padrão + categorias padrão)
└── dados_teste.sql     (opcional: vendedor, produtos e caixa aberto para testes)
src/main/java/br/com/apsoo/
├── MainApp.java
├── model/              (entidades e regras de negócio)
├── dao/                (Conexao + um DAO por entidade)
├── controller/         (fluxo dos casos de uso)
└── view/               (telas JavaFX)
src/main/resources/css/tema.css
```

## Como executar

1. Execute `sql/bd.sql` no MySQL (e, se quiser dados de exemplo, `sql/dados_teste.sql`).
2. Ajuste URL, usuário e senha em `br.com.apsoo.dao.Conexao`.
3. Rode `mvn javafx:run`.

Login padrão: `admin` / `admin123`. Com `dados_teste.sql`: `vendedor` / `vendedor123`.

## Iterações implementadas

- 1ª: Login/logout e Registrar venda (CPF/CNPJ, produtos, pagamento em dinheiro, Pix ou cartão).
- 2ª: Localizar venda e Devolução.
- 3ª: Produtos e Categorias (com histórico de alterações da categoria).
- 4ª: Caixa (abrir, sangria, suprimento, fechar e histórico).
- 5ª: Usuários e Minha conta (redefinir senha).
- 6ª: Dashboard, Estoque, Vendas por categoria e Relatório de vendas por período.
- 7ª: Relatórios de mais vendidos, por vendedor, produtos em falta e devoluções.
- 8ª: Exportação dos relatórios em PDF (OpenPDF) e CSV (planilha), preservando os filtros da tela.

## Interface

- JavaFX com tema próprio (`src/main/resources/css/tema.css`): cor primária azul, cantos arredondados, ícones SVG.
- Totalmente responsiva: o menu lateral vira só ícones abaixo de 1000 px e uma gaveta abaixo de 720 px; formulários,
  indicadores e filtros reorganizam as colunas conforme a largura (`GradeResponsiva`, `Barra`, `Fluxo`); as tabelas têm
  larguras mínimas e rolam na horizontal quando a janela é muito estreita.
- Componentes reutilizáveis em `br.com.apsoo.view`: `Icones`, `Botoes`, `Campo`, `Passo`, `Indicador`, `Tabelas`.

## Configurações

Em **Configurações** cada usuário escolhe onde abrem os formulários e as mensagens das ações (cadastrar, editar, pagar,
confirmar...): **na própria tela** (padrão, sobre a página atual) ou **em uma janela separada**. A preferência fica
guardada neste computador, por usuário (`PreferenciaDAO`), e vale para todas as janelas do sistema (`Janela`).

## Registrar venda (PDV em passos)

A venda é um assistente de 4 passos, na ordem do caso de uso Realizar Venda: **Cliente** (CPF/CNPJ) → **Produtos**
(busca por nome ou código de barras e quantidade) → **Pagamento** (dinheiro com troco, Pix ou cartão) → **Revisão**
e conclusão. Enter avança em campos de texto, Ctrl+Enter avança de qualquer ponto, e é possível voltar aos passos
já feitos (botão Voltar ou clicando no indicador de passos).
