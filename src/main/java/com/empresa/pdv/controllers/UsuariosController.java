package com.empresa.pdv.controllers;

import com.empresa.pdv.models.Usuario;
import com.empresa.pdv.services.UsuarioService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class UsuariosController {

    @FXML private TextField txtNome;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenha;
    @FXML private ComboBox<String> cbCargo;
    @FXML private Label lblTituloForm;
    @FXML private Label lblMensagem;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, Long> colId;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, String> colLogin;
    @FXML private TableColumn<Usuario, String> colCargo;
    @FXML private TableColumn<Usuario, String> colStatus;
    @FXML private TableColumn<Usuario, Void> colAcoes;

    private final UsuarioService usuarioService = new UsuarioService();
    private Usuario usuarioEmEdicao = null;

    @FXML
    public void initialize() {
        cbCargo.setItems(FXCollections.observableArrayList("VENDEDOR", "ADMIN"));
        cbCargo.getSelectionModel().selectFirst();

        configurarTabela();
        carregarUsuarios();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnStatus = new Button();

            {
                btnEditar.setStyle("-fx-cursor: hand;");
                btnStatus.setStyle("-fx-cursor: hand;");

                btnEditar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    prepararEdicao(u);
                });

                btnStatus.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    alternarStatus(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    btnStatus.setText("ATIVO".equalsIgnoreCase(u.getStatus()) ? "Desativar" : "Ativar");
                    btnStatus.getStyleClass().removeAll("danger", "accent");
                    btnStatus.getStyleClass().add("ATIVO".equalsIgnoreCase(u.getStatus()) ? "danger" : "accent");

                    HBox box = new HBox(8, btnEditar, btnStatus);
                    setGraphic(box);
                }
            }
        });
    }

    private void carregarUsuarios() {
        tblUsuarios.setItems(FXCollections.observableArrayList(usuarioService.listarUsuarios()));
    }

    @FXML
    void handleSalvar(ActionEvent event) {
        try {
            if (usuarioEmEdicao == null) {
                // Modo Cadastro
                usuarioService.cadastrarUsuario(
                    txtNome.getText(),
                    txtLogin.getText(),
                    txtSenha.getText(),
                    cbCargo.getValue()
                );
                exibirMensagem("Usuário cadastrado com sucesso!", false);
            } else {
                // Modo Edição
                usuarioEmEdicao.setNome(txtNome.getText());
                usuarioEmEdicao.setCargo(cbCargo.getValue());
                usuarioService.atualizarUsuario(usuarioEmEdicao);
                exibirMensagem("Usuário atualizado com sucesso!", false);
            }

            limparFormulario();
            carregarUsuarios();
        } catch (Exception e) {
            exibirMensagem(e.getMessage(), true);
        }
    }

    private void prepararEdicao(Usuario u) {
        usuarioEmEdicao = u;
        lblTituloForm.setText("Editar Usuário: " + u.getLogin());
        txtNome.setText(u.getNome());
        txtLogin.setText(u.getLogin());
        txtLogin.setDisable(true); // Login não pode ser alterado
        txtSenha.setDisable(true); // Alteração de senha deve ser via Perfil
        cbCargo.setValue(u.getCargo());

        btnSalvar.setText("Atualizar Usuário");
        btnCancelar.setVisible(true);
    }

    private void alternarStatus(Usuario u) {
        try {
            String novoStatus = "ATIVO".equalsIgnoreCase(u.getStatus()) ? "DESATIVADO" : "ATIVO";
            u.setStatus(novoStatus);
            usuarioService.atualizarUsuario(u);
            
            limparFormulario(); // Reseta o formulário caso estivesse editando o mesmo usuário
            carregarUsuarios();
            exibirMensagem("Status alterado com sucesso!", false);
        } catch (Exception e) {
            exibirMensagem(e.getMessage(), true);
        }
    }

    @FXML
    void handleCancelar(ActionEvent event) {
        limparFormulario();
    }

    private void limparFormulario() {
        usuarioEmEdicao = null;
        lblTituloForm.setText("Cadastrar Novo Usuário");
        txtNome.clear();
        txtLogin.clear();
        txtLogin.setDisable(false);
        txtSenha.clear();
        txtSenha.setDisable(false);
        cbCargo.getSelectionModel().selectFirst();

        btnSalvar.setText("Cadastrar Usuário");
        btnCancelar.setVisible(false);
    }

    private void exibirMensagem(String msg, boolean eErro) {
        lblMensagem.setText(msg);
        lblMensagem.setStyle(eErro ? "-fx-text-fill: -fx-color-status-danger;" : "-fx-text-fill: -fx-color-status-success;");
        lblMensagem.setVisible(true);
    }
}