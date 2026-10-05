package br.com.apsoo;

import br.com.apsoo.controller.Sessao;
import br.com.apsoo.view.Janela;
import br.com.apsoo.view.LoginView;
import br.com.apsoo.view.PrincipalView;
import br.com.apsoo.view.Tema;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage janela;

    @Override
    public void start(Stage stage) {
        this.janela = stage;
        janela.setTitle("MG Variedades");
        janela.setMinWidth(420);
        janela.setMinHeight(560);

        Scene cena = new Scene(new LoginView(this::mostrarPrincipal), 1200, 760);
        Tema.aplicar(cena);
        janela.setScene(cena);
        janela.show();
    }

    private void mostrarPrincipal() {
        trocarTela(new PrincipalView(this::mostrarLogin));
    }

    // Logout: encerra a sessão e volta para o login (RNF10)
    private void mostrarLogin() {
        Sessao.encerrar();
        Janela.registrarAnfitriao(null);
        trocarTela(new LoginView(this::mostrarPrincipal));
    }

    private void trocarTela(Parent tela) {
        janela.getScene().setRoot(tela);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
