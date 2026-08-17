# 📦 Camada de Modelos de Dados (Entities / DTOs)

Esta pasta contém as entidades que representam o domínio do negócio e as estruturas de dados transmitidas pelo sistema.

## 📌 Responsabilidade

- Representar os objetos do mundo real refletidos no sistema (ex: `Produto`, `Usuario`, `Venda`, `ItemVenda`, `Caixa`).
- Encapsular atributos, métodos _getters_, _setters_, `equals` e `hashCode`.
- Transportar dados entre a interface gráfica, os serviços e o banco de dados.

## 💡 Diretrizes

- Mantenha os atributos privados (`private`).
- Os modelos devem ser limpos (POJOs), contendo prioritariamente dados e validações simples de atributos.

## 🚀 Quando criar um arquivo aqui?

Quando uma nova entidade for identificada no modelo de dados do sistema.

## 🏷️ Convenção de Nomenclatura

- Substantivo no **singular** e em **PascalCase**:
  - `Produto.java`
  - `Usuario.java`
  - `Categoria.java`
