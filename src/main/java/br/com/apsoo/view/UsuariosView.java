package br.com.apsoo.view;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import br.com.apsoo.controller.RegraNegocioException;
import br.com.apsoo.controller.UsuarioController;
import br.com.apsoo.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Gerenciar usuários (RF01 e RF02): somente administrador
public class UsuariosView extends VBox {

    private final TableView<Usuario> tabela = new TableView<>();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private final UsuarioController controller = new UsuarioController();

    public UsuariosView() {
        setSpacing(16);

        Label dica = new Label("Usuários desativados não conseguem entrar no sistema. "
                + "A conta de administrador padrão só pode ter a senha alterada.");
        dica.getStyleClass().add("muted");
        dica.setWrapText(true);
        Button btnNovo = Botoes.comIcone("Novo usuário", "mais", "accent");
        btnNovo.setOnAction(e -> {
            if (UsuarioDialog.exibir(controller, null)) {
                carregar();
            }
        });

        getChildren().addAll(new Barra().aEsquerda(dica).aDireita(btnNovo), montarTabela());
        VBox.setVgrow(tabela, Priority.ALWAYS);
        carregar();
    }

    private TableView<Usuario> montarTabela() {
        TableColumn<Usuario, Void> colStatus = new TableColumn<>("Status");
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                boolean ativo = getTableView().getItems().get(getIndex()).isAtivo();
                Label badge = new Label(ativo ? "Ativo" : "Desativado");
                badge.getStyleClass().addAll("badge", ativo ? "success" : "danger");
                setGraphic(badge);
            }
        });
        Tabelas.fixa(colStatus, 130);

        TableColumn<Usuario, Void> colAcoes = new TableColumn<>("");
        colAcoes.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void vazio, boolean empty) {
                super.updateItem(vazio, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Usuario usuario = getTableView().getItems().get(getIndex());

                Button btnEditar = Botoes.acao("editar", "Editar usuário", () -> editar(usuario));
                btnEditar.setDisable(usuario.isSeed());
                Button btnStatus = Botoes.acao(usuario.isAtivo() ? "desativar" : "ativar",
                        usuario.isAtivo() ? "Desativar usuário" : "Ativar usuário", () -> alterarStatus(usuario));
                btnStatus.setDisable(usuario.isSeed());
                Button btnPromover = Botoes.acao("promover", "Promover a administrador", () -> promover(usuario));
                btnPromover.setDisable(usuario.isAdmin() || !usuario.isAtivo());

                setGraphic(new HBox(2, btnEditar, btnStatus, btnPromover));
            }
        });
        Tabelas.fixa(colAcoes, 140);

        tabela.getColumns().addAll(
                Celulas.<Usuario>pessoa("Usuário", 260, u -> u.getNome() + (u.isSeed() ? " (padrão)" : ""),
                        u -> "@" + u.getLogin()),
                Celulas.<Usuario>chip("Cargo", 150, u -> u.isAdmin() ? "Administrador" : "Vendedor"),
                Tabelas.<Usuario>coluna("Cadastro", 110,
                        u -> u.getDataCadastro().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                colStatus, colAcoes);
        tabela.setItems(usuarios);
        tabela.setPlaceholder(Tabelas.vazio("Nenhum usuário cadastrado"));
        Tabelas.preparar(tabela);
        return tabela;
    }

    private void carregar() {
        try {
            usuarios.setAll(controller.listar());
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
    }

    private void editar(Usuario usuario) {
        if (UsuarioDialog.exibir(controller, usuario)) {
            carregar();
        }
    }

    private void alterarStatus(Usuario usuario) {
        boolean ativar = !usuario.isAtivo();
        if (!ativar && !Dialogos.confirmar("Desativar o usuário \"" + usuario.getNome()
                + "\"? Ele não conseguirá mais entrar no sistema.")) {
            return;
        }
        try {
            controller.alterarStatus(usuario, ativar);
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
        carregar();
    }

    private void promover(Usuario usuario) {
        if (!Dialogos.confirmar("Promover \"" + usuario.getNome() + "\" a administrador? "
                + "Essa alteração não pode ser desfeita pelo sistema.")) {
            return;
        }
        try {
            controller.promover(usuario);
        } catch (RegraNegocioException ex) {
            Dialogos.avisar(ex.getMessage());
        } catch (SQLException ex) {
            Dialogos.erro("Erro na operação: " + ex.getMessage());
        }
        carregar();
    }
}
