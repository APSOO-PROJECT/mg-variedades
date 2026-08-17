# 🧪 Camada de Testes Automatizados (JUnit)

Esta pasta contém todas as suítes de testes automatizados unitários e de integração da aplicação.

## 📌 Responsabilidade

- Validar o comportamento de regras de negócio isoladas sem a necessidade de subir a interface gráfica (JavaFX).
- Garantir que alterações no código ou refatorações não quebrem funcionalidades já implementadas (prevenção de regressão).
- Testar cálculos financeiros, validações de prazos de devolução e regras de permissão de acesso.

## 🛠️ Tecnologias Utilizadas

- **JUnit 5 (Jupiter):** Framework de testes automatizados.
- **Mockito (Opcional):** Para simulação (mock) de dependências como repositórios de banco de dados.

## ⛔ O que NÃO deve ser feito aqui

- Não crie testes que dependam de intervenção manual do usuário no teclado ou mouse.
- Evite depender do banco de dados de produção; utilize dados fictícios (_mocks_) ou um banco em memória durante as suítes de teste.

## 🚀 Como Executar os Testes

Para rodar todos os testes do projeto via terminal, utilize o comando do Maven:

```bash
mvn test

```

## 🚀 Quando criar um arquivo aqui?

Sempre que uma nova classe de regra de negócio (`Service`) ou componente Utilitário (`Utils`) for criado ou alterado.

## 🏷️ Convenção de Nomenclatura

- Nome do componente testado em **PascalCase** + sufixo `Test.java`:
- `CaixaServiceTest.java`
- `VendaServiceTest.java`
- `AuthServiceTest.java`
