# 🕹️ Camada de Controladores (Controllers)

Esta pasta contém as classes Java responsáveis por gerenciar a interatividade e o ciclo de vida das telas FXML.

## 📌 Responsabilidade

- Capturar eventos do usuário (clique em botões, digitação, seleção em tabelas).
- Mapear componentes visuais anotados com `@FXML`.
- Delegar o processamento de regras de negócio para os componentes da camada `services`.
- Atualizar os elementos visuais da tela com os dados retornados do serviço.

## ⛔ O que NÃO deve existir aqui

- **Regras de negócio complexas:** Cálculos financeiros, validações de desconto, cálculo de troco ou regras de prazo de devolução devem ficar estritamente na camada `services`.
- **Comandos SQL / Banco de Dados:** Nunca acesse o banco diretamente pelo Controller.

## 🚀 Quando criar um arquivo aqui?

Para cada arquivo `.fxml` criado na pasta `views/`, deve existir um Controller correspondente.

## 🏷️ Convenção de Nomenclatura

- Nome da tela em **PascalCase** + sufixo `Controller.java`:
  - `LoginController.java`
  - `VendasController.java`
