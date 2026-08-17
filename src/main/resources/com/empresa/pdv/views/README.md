# 🖼️ Camada de Visão (FXML)

Esta pasta contém os arquivos de interface gráfica em formato XML (`.fxml`) declarados para a aplicação JavaFX.

## 📌 Responsabilidade

- Definir exclusivamente a estrutura visual das telas (layouts, campos de texto, tabelas, botões).
- Vincular cada elemento visual ao seu respectivo controlador Java através do atributo `fx:controller`.

## ⚠️ Regras de Estilização e Design System

- **Zero Cores Hardcoded:** É estritamente proibido utilizar `style="-fx-background-color: ...;"` inline.
- Utilize a propriedade `styleClass` para aplicar classes utilitárias do AtlantaFX ou variáveis do `globals.css`.
- Todos os ícones devem utilizar a tag `<FontIcon />` da biblioteca Ikonli com o prefixo `fth-` (Feather Icons).

## 🚀 Quando criar um arquivo aqui?

Sempre que uma nova tela, modal, aba ou componente reaproveitável complexo for adicionado ao sistema.

## 🏷️ Convenção de Nomenclatura

- Nomes em **letra minúscula** e em português, usando `snake_case`:
  - `login.fxml`
  - `produtos.fxml`
  - `consulta_produtos.fxml`
