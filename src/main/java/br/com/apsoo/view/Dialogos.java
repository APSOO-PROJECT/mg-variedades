package br.com.apsoo.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

// Mensagens do sistema (substituem o JOptionPane do Swing); aparecem na tela ou em janela separada,
// conforme a preferência do usuário
public final class Dialogos {

    private Dialogos() {
    }

    public static void informar(String mensagem) {
        mostrar("Informação", "confirmar", "success", mensagem);
    }

    public static void avisar(String mensagem) {
        mostrar("Atenção", "alerta", "warning", mensagem);
    }

    public static void erro(String mensagem) {
        mostrar("Erro", "alerta", "danger", mensagem);
    }

    public static boolean confirmar(String mensagem) {
        Boolean resposta = new Janela<Boolean>("Confirmação")
                .conteudo(corpo("alerta", "", mensagem))
                .largura(440)
                .confirmar("Sim", () -> true)
                .cancelar("Não")
                .exibir();
        return resposta != null && resposta;
    }

    private static void mostrar(String titulo, String icone, String estilo, String mensagem) {
        new Janela<Boolean>(titulo)
                .conteudo(corpo(icone, estilo, mensagem))
                .largura(440)
                .confirmar("OK", () -> true)
                .semCancelar()
                .exibir();
    }

    // Ícone em quadro colorido ao lado do texto da mensagem
    private static HBox corpo(String icone, String estilo, String mensagem) {
        StackPane quadro = new StackPane(Icones.de(icone, 22));
        quadro.getStyleClass().add("icon-chip");
        if (!estilo.isEmpty()) {
            quadro.getStyleClass().add(estilo);
        }

        Label texto = new Label(mensagem);
        texto.setWrapText(true);
        HBox.setHgrow(texto, Priority.ALWAYS);

        HBox corpo = new HBox(14, quadro, texto);
        corpo.setAlignment(Pos.CENTER_LEFT);
        return corpo;
    }
}
