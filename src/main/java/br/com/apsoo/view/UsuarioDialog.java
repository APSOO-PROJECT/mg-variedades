package br.com.apsoo.view;

import br.com.apsoo.controller.UsuarioController;
import br.com.apsoo.model.Cargo;
import br.com.apsoo.model.Usuario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

// Formulário de cadastro e edição de usuário
public final class UsuarioDialog {

    private UsuarioDialog() {
    }

    // Retorna true se o usuário foi salvo
    public static boolean exibir(UsuarioController controller, Usuario existente) {
        TextField txtNome = new TextField();
        TextField txtLogin = new TextField();
        PasswordField txtSenha = new PasswordField();
        txtSenha.setPromptText(existente == null ? "Mínimo de 6 caracteres" : "Deixe em branco para manter a atual");
        ComboBox<Cargo> cbCargo = new ComboBox<>();
        cbCargo.getItems().addAll(Cargo.values());
        cbCargo.setValue(Cargo.VENDEDOR);

        if (existente != null) {
            txtNome.setText(existente.getNome());
            txtLogin.setText(existente.getLogin());
            cbCargo.setValue(existente.getCargo());
            // Admin só pode ser promovido, nunca rebaixado
            cbCargo.setDisable(existente.isAdmin());
        }

        Campo campoNome = new Campo("Nome *", txtNome);
        GradeResponsiva.ocupar(campoNome, 2);
        GradeResponsiva grade = new GradeResponsiva(210, 14);
        grade.getChildren().addAll(campoNome, new Campo("Login *", txtLogin),
                new Campo("Cargo *", cbCargo),
                new Campo(existente == null ? "Senha *" : "Nova senha", txtSenha));

        Boolean salvo = new Janela<Boolean>(existente == null ? "Novo usuário" : "Editar usuário")
                .conteudo(grade)
                .largura(500)
                .confirmar("Salvar", () -> {
                    Usuario usuario = new Usuario();
                    usuario.setId(existente == null ? null : existente.getId());
                    usuario.setNome(txtNome.getText().trim());
                    usuario.setLogin(txtLogin.getText().trim());
                    usuario.setCargo(cbCargo.getValue());
                    usuario.setStatus(existente == null ? null : existente.getStatus());
                    controller.salvar(usuario, txtSenha.getText());
                    return true;
                })
                .exibir();
        return salvo != null && salvo;
    }
}
