# 🛒 MG Variedades — Gestão de Loja

O **MG Variedades** é um sistema desktop moderno para gestão de pontos de venda em estabelecimentos varejistas de alto fluxo. O projeto foi projetado com arquitetura modular JavaFX (MVC), aproveitando componentes e temas utilitários para entregar uma interface fluida, responsiva e com estética SaaS/Web contemporânea.

---

## 🛠️ Tech do Projeto & Arquitetura

- **Linguagem:** Java 25
- **Interface Gráfica:** JavaFX 25
- **Build System & Dependências:** Apache Maven
- **Design System / Tema:** [AtlantaFX](https://mkpaz.github.io/atlantafx/) (Tema _PrimerLight_)
- **Biblioteca de Ícones:** [Ikonli](https://kordamp.org/ikonli/) (Pacote _Feather Icons_)
- **Arquitetura de UI:** MVC (FXML + Controllers + Services + Repositories) + Java Module System (JPMS)

---

## 📋 Pré-requisitos (O que instalar na máquina)

Antes de clonar e rodar o projeto, certifique-se de ter os seguintes componentes instalados:

1. **JDK 25** (ou JDK 21+ com suporte a JavaFX)
   - Configure as variáveis de ambiente `JAVA_HOME` e adicione a pasta `bin` ao `Path`.
2. **Apache Maven 3.8+**
   - Verifique a instalação rodando `mvn -v` no terminal.
3. **VS Code** (Recomendado) com as extensões:
   - _Extension Pack for Java_ (Microsoft)
   - _Maven for Java_ (Microsoft)

---

## 🚀 Como Rodar o Projeto

1. **Clone o repositório:**

   ```bash
   git clone [https://github.com/APSOO-PROJECT/mg-variedades.git](https://github.com/APSOO-PROJECT/mg-variedades.git)
   cd mg-variedades
   ```

````

2. **Execute a aplicação via Maven:**
```bash
mvn clean javafx:run

````

---

## 📁 Estrutura de Pastas e Arquitetura

Organização técnica em camadas seguindo a arquitetura **MVC**:

```text
mg-variedades/
├── .github/                 # Workflows de CI, templates de Issue e Pull Request
├── .vscode/                 # Configurações de execução e ambiente do VS Code
├── src/
│   └── main/
│       ├── java/
│       │   └── com/empresa/pdv/
│       │       ├── controllers/     # Controladores de UI e eventos de tela (FXML)
│       │       │   ├── CaixaController.java
│       │       │   ├── CategoriasController.java
│       │       │   ├── ConsultaProdutosController.java
│       │       │   ├── DashboardController.java
│       │       │   ├── DevolucoesController.java
│       │       │   ├── LoginController.java
│       │       │   ├── ProdutosController.java
│       │       │   ├── RelatoriosController.java
│       │       │   ├── UsuariosController.java
│       │       │   └── VendasController.java
│       │       ├── models/          # Entidades do domínio (Produto, Venda, Usuario, etc.)
│       │       ├── repository/      # Persistência e acesso a dados (DAOs / SQL)
│       │       ├── services/        # Regras de negócio e validações
│       │       │   ├── AuthService.java
│       │       │   ├── CaixaService.java
│       │       │   ├── CategoriaService.java
│       │       │   ├── DevolucaoService.java
│       │       │   ├── ProdutoService.java
│       │       │   ├── ReportExportService.java
│       │       │   ├── UsuarioService.java
│       │       │   └── VendaService.java
│       │       ├── utils/           # Formatadores, helpers de data/moeda e gerenciador de sessão
│       │       ├── App.java         # Entry Point principal do JavaFX
│       │       └── SystemInfo.java
│       │   └── module-info.java    # Configuração de reflexão e Módulos Java (JPMS)
│       └── resources/
│           └── com/empresa/pdv/     # Arquivos de Visão (FXML) e Ativos Visuais
│               ├── styles/
│               │   └── globals.css  # Variáveis de cores, tokens e utilitários globais
│               └── views/           # Telas FXML do sistema
│                   ├── caixa.fxml
│                   ├── categorias.fxml
│                   ├── consulta_produtos.fxml
│                   ├── dashboard.fxml
│                   ├── devolucoes.fxml
│                   ├── login.fxml
│                   ├── produtos.fxml
│                   ├── relatorios.fxml
│                   ├── usuarios.fxml
│                   └── vendas.fxml
├── pom.xml                  # Dependências Maven (JavaFX, AtlantaFX, Ikonli)
└── README.md

```

---

## 🎨 Padrões de Front-End & Estilização

Utilizamos o **AtlantaFX** combinado com um sistema de variáveis globais em CSS no `globals.css` para garantir a consistência visual da aplicação:

- **Zero Cores Hardcoded:** É estritamente proibido definir cores diretamente nos arquivos FXML via propriedades inline (ex: `style="-fx-background-color: #FFFFFF;"`). Toda a estilização deve consumir as classes utilitárias ou variáveis globais.
- **Uso do `globals.css`:** Utilize a propriedade `styleClass=""` nos elementos FXML para aplicar layouts e tokens visuais (ex: `styleClass="card-panel"`, `styleClass="text-heading"`).
- **Classes Utilitárias AtlantaFX:** Faça uso das classes nativas para botões e componentes (ex: `accent`, `flat`, `danger`, `success`).
- **Ícones Ikonli (Feather Icons):**
- Todos os ícones devem utilizar a tag `<FontIcon />`.
- O prefixo obrigatório para ícones da coleção Feather é **`fth-`** (exemplo: `fth-shopping-cart`, `fth-user`, `fth-home`, `fth-box`).

---

## 🤝 Fluxo de Contribuição & Git

Para manter o repositório organizado e profissional, siga rigorosamente as orientações:

### 1. Padrão de Commits (Conventional Commits)

Escreva mensagens de commit claras utilizando os prefixos definidos:

- `feat:` Para novas funcionalidades de tela ou código (ex: `feat: adiciona tela de cadastro de produtos`).
- `fix:` Para correção de bugs (ex: `fix: corrige carregamento do FXML de vendas`).
- `style:` Alterações visuais, CSS ou ajustes de layout que não alteram lógica Java.
- `docs:` Alterações na documentação (`README.md`, comentários de código).
- `arch:` Alterações na estrutura de pacotes ou arquitetura do projeto.

### 2. Fluxo de Branches e Pull Requests (PR)

1. **Nunca faça commit diretamente na branch `main`.**
2. Crie uma branch nomeada a partir da issue atribuída a você no GitHub Projects:

```bash
git checkout -b feature/nome-da-sua-task

```

3. Realize os commits necessários e envie sua branch para o repositório remoto:

```bash
git push origin feature/nome-da-sua-task

```

4. Abra um **Pull Request (PR)** apontando para a branch `main`.
5. Preencha o template de PR detalhadamente e aguarde a revisão e aprovação antes de realizar o merge.
