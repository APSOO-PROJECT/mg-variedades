# 🛒 MG Variedades — Gestão de Loja

O **MG Variedades** é um sistema desktop moderno para gestão de pontos de venda em estabelecimentos varejistas de alto fluxo. O projeto foi projetado com arquitetura modular JavaFX, aproveitando componentes e temas utilitários para entregar uma interface fluida, responsiva e com estética SaaS/Web contemporânea.

---

## 🛠️ Tec do Projeto & Arquitetura

- **Linguagem:** Java 25
- **Interface Gráfica:** JavaFX 25
- **Build System & Dependências:** Apache Maven
- **Design System / Tema:** [AtlantaFX](https://mkpaz.github.io/atlantafx/) (Tema _PrimerLight_)
- **Biblioteca de Ícones:** [Ikonli](https://kordamp.org/ikonli/) (Pacote _Feather Icons_)
- **Arquitetura de UI:** FXML + Controllers + Java Module System (JPMS)

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
   git clone [https://github.com/enzovalencuela/mg-variedades.git](https://github.com/enzovalencuela/mg-variedades.git)
   cd mg-variedades
   ```

2. **Execute a aplicação via Maven:**

```bash
mvn clean javafx:run

```

---

## 📁 Estrutura de Pastas

```text
mg-variedades/
├── .github/                # Templates de Issue e Pull Request
├── .vscode/                # Configurações do VS Code
├── src/
│   └── main/
│       ├── java/
│       │   ├── com/empresa/pdv/
│       │   │   ├── controllers/    # Controladores FXML (Regra de UI)
│       │   │   │   └── DashboardController.java
│       │   │   ├── App.java        # Entry Point da Aplicação
│       │   │   └── SystemInfo.java
│       │   └── module-info.java   # Configuração de Módulos (JPMS)
│       └── resources/
│           └── com/empresa/pdv/    # Arquivos de Visão (FXML) e Ativos
│               ├── styles/
│               │   └── globals.css # Variáveis CSS e classes utilitárias
│               └── dashboard.fxml
├── pom.xml                 # Gerenciamento de dependências Maven
└── README.md

```

---

## 🎨 Padrões de Front-End & Estilização

Utilizamos o **AtlantaFX** combinado com um sistema de variáveis globais em CSS para garantir a consistência visual da aplicação.

- **Zero Cores Hardcoded:** É estritamente proibido definir cores diretamente nos arquivos FXML via propriedades in-line como `style="-fx-background-color: #FFFFFF;"`. Toda a estilização deve consumir as classes utilitárias ou variáveis definidas no `globals.css`.
- **Uso do `globals.css`:** Para definir fundos de cartões, rótulos e espaçamentos padrão, utilize `styleClass=""` nos elementos FXML (ex: `styleClass="card-panel"`, `styleClass="text-heading"`).
- **Classes Utilitárias AtlantaFX:** Combine as classes do AtlantaFX para componentes nativos (ex: `accent`, `flat`, `danger`).
- **Ícones Ikonli (Feather):**
- Todos os ícones devem utilizar a tag `<FontIcon />`.
- O prefixo obrigatório para ícones da coleção Feather é **`fth-`** (exemplo: `fth-shopping-cart`, `fth-user`, `fth-home`).

---

## 🤝 Fluxo de Contribuição & Git

Para manter o repositório organizado, siga as regras abaixo:

### 1. Padrão de Commits (Conventional Commits)

Sempre escreva mensagens de commit claras utilizando os prefixos:

- `feat:` Para novas funcionalidades de tela ou código (ex: `feat: adiciona tela de cadastro de produtos`).
- `fix:` Para correção de bugs (ex: `fix: corrige carregamento do FXML de vendas`).
- `style:` Alterações visuais ou ajustes de layout que não alteram código Java.
- `docs:` Alterações na documentação (`README.md`, comentários).

### 2. Fluxo de Pull Requests (PR)

1. **Nunca faça commit direto na branch `main`.**
2. Crie uma branch a partir da issue atribuída a você:

```bash
git checkout -b feature/nome-da-sua-task

```

3. Faça os commits e envie sua branch:

```bash
git push origin feature/nome-da-sua-task

```

4. Abra um **Pull Request (PR)** para a branch `main`.
5. Garanta que o template de PR foi preenchido corretamente e solicite a revisão do Líder de Front-End antes de realizar o merge.
