package br.com.apsoo.view;

import java.sql.SQLException;

import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.controller.Sessao;
import br.com.apsoo.controller.UsuarioController;
import br.com.apsoo.model.Usuario;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Redefinição da própria senha (RF03), disponível para qualquer usuário
public class MinhaContaView extends VBox {

    private final PasswordField txtAtual = new PasswordField();
    private final PasswordField txtNova = new PasswordField();
    private final PasswordField txtConfirmacao = new PasswordField();
    private final Label lblErro = new Label();
    private final UsuarioController controller = new UsuarioController();

    public MinhaContaView() {
        setSpacing(16);
        Usuario usuario = Sessao.getUsuarioLogado();

        // Cartão com os dados da conta
        StackPane avatar = new StackPane(new Label(usuario.getNome().substring(0, 1).toUpperCase()));
        avatar.getStyleClass().add("avatar");
        Label nome = new Label(usuario.getNome());
        nome.getStyleClass().add("subtitle");
        Label detalhes = new Label("Login: " + usuario.getLogin() + "  |  "
                + (usuario.isAdmin() ? "Administrador" : "Vendedor"));
        detalhes.getStyleClass().add("muted");
        HBox cabecalho = new HBox(14, avatar, new VBox(2, nome, detalhes));
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        VBox cartaoConta = new VBox(cabecalho);
        cartaoConta.getStyleClass().add("card");

        // Cartão de redefinição de senha
        Label titulo = new Label("Redefinir senha");
        titulo.getStyleClass().add("subtitle");
        Label dica = new Label("Informe a senha atual e escolha uma nova senha com pelo menos 6 caracteres.");
        dica.getStyleClass().add("muted");
        dica.setWrapText(true);

        txtAtual.setPromptText("Senha atual");
        txtNova.setPromptText("Mínimo de 6 caracteres");
        txtConfirmacao.setPromptText("Repita a nova senha");
        lblErro.setWrapText(true);
        lblErro.managedProperty().bind(lblErro.textProperty().isNotEmpty()); // sem texto, não ocupa espaço
        lblErro.visibleProperty().bind(lblErro.textProperty().isNotEmpty());

        Button btnRedefinir = Botoes.comIcone("Redefinir senha", "cadeado", "accent");
        btnRedefinir.setDefaultButton(true);
        btnRedefinir.setOnAction(e -> redefinir());

        VBox cartaoSenha = new VBox(14, titulo, dica,
                new Campo("Senha atual *", txtAtual),
                new Campo("Nova senha *", txtNova),
                new Campo("Confirmar nova senha *", txtConfirmacao),
                lblErro, btnRedefinir);
        cartaoSenha.getStyleClass().add("card");

        cartaoConta.setMaxWidth(520);
        cartaoSenha.setMaxWidth(520);
        getChildren().addAll(cartaoConta, cartaoSenha);
    }

    private void redefinir() {
        lblErro.getStyleClass().remove("erro");
        try {
            controller.redefinirSenha(txtAtual.getText(), txtNova.getText(), txtConfirmacao.getText());
            txtAtual.clear();
            txtNova.clear();
            txtConfirmacao.clear();
            lblErro.setText("");
            Dialogos.informar("Senha redefinida com sucesso.");
        } catch (RegraNegocioException ex) {
            lblErro.getStyleClass().add("erro");
            lblErro.setText(ex.getMessage());
        } catch (SQLException ex) {
            lblErro.getStyleClass().add("erro");
            lblErro.setText("Erro na operação: " + ex.getMessage());
        }
    }
}
