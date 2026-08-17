# 🧠 Camada de Serviços (Regras de Negócio)

Esta pasta concentra toda a inteligência de negócios, validações corporativas e fluxos operacionais do sistema.

## 📌 Responsabilidade

- Executar validações de dados (ex: verificar se o valor da sangria excede o saldo disponível no caixa).
- Aplicar regras do documento de requisitos (ex: verificar se o produto está dentro do prazo limite de devolução da categoria).
- Coordenar chamadas aos repositórios (`repository`) para leitura e escrita de dados.
- Processar exportações e relatórios (`ReportExportService`).

## 🛡️ Princípios de Design

- As classes de serviço devem ser puras e **independentes da interface gráfica**.
- **Nunca** importe pacotes `javafx.scene.*` ou referencie componentes visuais (`TextField`, `Button`) dentro de um Service.

## 🚀 Quando criar um arquivo aqui?

Quando houver um domínio de negócio ou módulo operacional claro (ex: Autenticação, Vendas, Caixa, Devolução).

## 🏷️ Convenção de Nomenclatura

- Nome do domínio em **PascalCase** + sufixo `Service.java`:
  - `AuthService.java`
  - `CaixaService.java`
  - `VendaService.java`
