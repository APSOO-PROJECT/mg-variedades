package com.empresa.pdv.controllers;

import com.empresa.pdv.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblErro;
    @FXML private Button btnEntrar;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        lblErro.setVisible(false);
    }

    @FXML
    void handleEntrar(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String senha = txtSenha.getText();

        try {
            authService.autenticar(usuario, senha);
            redirecionarParaDashboard();
        } catch (Exception e) {
            exibirErro(e.getMessage());
        }
    }

    @FXML
    void handleEsqueceuSenha(ActionEvent event) {
        exibirErro("Entre em contato com o Administrador para redefinir sua senha.");
    }

    private void exibirErro(String mensagem) {
        lblErro.setText(mensagem);
        lblErro.setVisible(true);
    }

    private void redirecionarParaDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/empresa/pdv/views/dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) btnEntrar.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 760);
            
            String globalsCss = getClass().getResource("/com/empresa/pdv/styles/globals.css").toExternalForm();
            scene.getStylesheets().add(globalsCss);

            stage.setScene(scene);
            stage.setTitle("MG Variedades - Painel Principal");
            stage.show();
        } catch (Exception e) {
            exibirErro("Erro ao carregar a tela principal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}