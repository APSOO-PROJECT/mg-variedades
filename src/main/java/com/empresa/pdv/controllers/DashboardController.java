package com.empresa.pdv.controllers;

import com.empresa.pdv.services.AuthService;
import com.empresa.pdv.models.Usuario;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Button btnSair;
    @FXML private Button btnUsuarios;
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        Usuario usuarioLogado = AuthService.getUsuarioAutenticado();

        if (usuarioLogado != null && !"ADMIN".equalsIgnoreCase(usuarioLogado.getCargo())) {
            if (btnUsuarios != null) {
                btnUsuarios.setVisible(false);
                btnUsuarios.setManaged(false);
            }
        }
    }

    @FXML
    void handleNavegarHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/empresa/pdv/views/dashboard.fxml"));
            BorderPane root = loader.load();
            
            // Pega apenas a VBox inicial da Home e coloca no contentArea
            StackPane homeContent = (StackPane) root.getCenter();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(homeContent.getChildren().get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleNavegarUsuarios(ActionEvent event) {
        carregarView("/com/empresa/pdv/views/usuarios.fxml");
    }

    private void carregarView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent novaView = loader.load();

            // Limpa o conteúdo central anterior e insere a nova tela
            contentArea.getChildren().clear();
            contentArea.getChildren().add(novaView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        // 1. Limpa a sessão do usuário
        AuthService.logout();

        // 2. Redireciona para a tela de Login
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/empresa/pdv/views/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnSair.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 760);

            String globalsCss = getClass().getResource("/com/empresa/pdv/styles/globals.css").toExternalForm();
            scene.getStylesheets().add(globalsCss);

            stage.setScene(scene);
            stage.setTitle("MG Variedades — Acesso ao Sistema");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}