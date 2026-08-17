# 🗄️ Camada de Repositórios / Acesso a Dados (Persistence)

Esta pasta contém as classes responsáveis pela comunicação direta com o Banco de Dados Relacional (PostgreSQL / MySQL).

## 📌 Responsabilidade

- Executar instruções SQL (queries de `SELECT`, `INSERT`, `UPDATE`, `DELETE`).
- Mapear os dados brutos oriundos das tabelas do banco de dados em objetos Java da camada `models`.
- Gerenciar conexões e transações de forma segura.

## ⛔ O que NÃO deve existir aqui

- Lógica de interface gráfica ou tomada de decisões de negócio.

## 🚀 Quando criar um arquivo aqui?

Sempre que uma entidade do sistema precisa ser salva, consultada ou atualizada de forma persistente no banco de dados.

## 🏷️ Convenção de Nomenclatura

- Nome da entidade em **PascalCase** + sufixo `Repository.java` (ou `DAO.java`):
  - `ProdutoRepository.java`
  - `UsuarioRepository.java`
  - `VendaRepository.java`
