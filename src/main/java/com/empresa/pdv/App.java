package com.empresa.pdv;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
public void start(Stage stage) throws Exception {
    // 1. Tema do AtlantaFX
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/empresa/pdv/dashboard.fxml"));
    Parent root = loader.load();

    Scene scene = new Scene(root, 1280, 760);

    // 2. Carrega nosso CSS global com variáveis e classes personalizadas
    String globalsCss = getClass().getResource("/com/empresa/pdv/styles/globals.css").toExternalForm();
    scene.getStylesheets().add(globalsCss);

    stage.setTitle("MG Variedades - Gestão de Loja");
    stage.setScene(scene);
    stage.show();
}

    public static void main(String[] args) {
        launch();
    }
}