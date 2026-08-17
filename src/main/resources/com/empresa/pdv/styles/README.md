# 🎨 Camada de Estilos Globais (CSS)

Esta pasta abriga a folha de estilo centralizada da aplicação (`globals.css`).

## 📌 Responsabilidade

- Armazenar tokens de design (variáveis de cores, fontes, raios de borda, espaçamentos).
- Sobrescrever ou estender componentes nativos do tema **AtlantaFX (PrimerLight)**.
- Definir classes utilitárias reutilizáveis em múltiplos arquivos FXML.

## 🛠️ Como Utilizar

- Declare variáveis globais dentro do bloco `.root`:
  ```css
  .root {
    -fx-primary-color: #2563eb;
    -fx-card-bg: #ffffff;
  }
  ```

* Aplique nos FXMLs através do `styleClass="card-panel"`.

## 🚀 Quando editar esta pasta?

- Apenas quando for necessário ajustar a paleta de cores global da marca, alterar a tipografia padrão ou criar uma nova classe utilitária de layout.
