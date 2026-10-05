package br.com.apsoo.view;

import java.sql.SQLException;

import br.com.apsoo.controller.LoginController;
import br.com.apsoo.controller.RegraNegocioException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView extends StackPane {

    private final TextField txtLogin = new TextField();
    private final PasswordField txtSenha = new PasswordField();
    private final Label lblErro = new Label();
    private final Button btnEntrar = Botoes.comIcone("Entrar", "confirmar", "accent");
    private final LoginController loginController = new LoginController();
    private final Runnable aoEntrar;

    public LoginView(Runnable aoEntrar) {
        this.aoEntrar = aoEntrar;
        getStyleClass().add("login-fundo");
        setPadding(new Insets(16));

        // Faixa azul no topo do cartão, com o logo em branco (como o arquivo original)
        StackPane faixa = new StackPane(Logo.completo(210, Logo.BRANCO));
        faixa.getStyleClass().add("login-faixa");

        Label titulo = new Label("Bem-vindo");
        titulo.getStyleClass().add("title");
        Label subtitulo = new Label("Entre com seu login e senha para continuar");
        subtitulo.getStyleClass().add("muted");
        subtitulo.setWrapText(true);

        txtLogin.setPromptText("Seu login");
        txtSenha.setPromptText("Sua senha");
        lblErro.getStyleClass().add("erro");
        lblErro.setWrapText(true);
        lblErro.setManaged(false);
        lblErro.setVisible(false);

        btnEntrar.setDefaultButton(true);
        btnEntrar.setMaxWidth(Double.MAX_VALUE);

        VBox corpo = new VBox(16, new VBox(4, titulo, subtitulo),
                new Campo("Login", txtLogin), new Campo("Senha", txtSenha), lblErro, btnEntrar);
        corpo.setPadding(new Insets(28, 32, 32, 32));

        VBox card = new VBox(faixa, corpo);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 0;"); // a faixa azul ocupa a largura toda do cartão
        card.setMaxWidth(420);
        card.setMaxHeight(VBox.USE_PREF_SIZE);

        setAlignment(Pos.CENTER);
        getChildren().add(card);

        btnEntrar.setOnAction(e -> executarLogin());
        Platform.runLater(txtLogin::requestFocus);
    }

    private void executarLogin() {
        mostrarErro(null);
        try {
            loginController.autenticar(txtLogin.getText(), txtSenha.getText());
            aoEntrar.run();
        } catch (RegraNegocioException ex) {
            mostrarErro(ex.getMessage());
            txtSenha.clear();
            txtSenha.requestFocus();
        } catch (SQLException ex) {
            mostrarErro("Erro na operação: " + ex.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        lblErro.setText(mensagem == null ? "" : mensagem);
        lblErro.setVisible(mensagem != null);
        lblErro.setManaged(mensagem != null);
    }
}
