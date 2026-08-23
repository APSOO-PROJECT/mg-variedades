package com.empresa.pdv.controllers;

import com.empresa.pdv.exceptions.RegraNegocioException;
import com.empresa.pdv.models.Categoria;
import com.empresa.pdv.services.CategoriaService;
import com.empresa.pdv.services.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CategoriasController {
    private final CategoriaService categoriaService = ServiceRegistry.categoriaService();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    @FXML
    private TableView<Categoria> tabelaCategorias;
    @FXML
    private TableColumn<Categoria, Long> colunaId;
    @FXML
    private TableColumn<Categoria, String> colunaNome;
    @FXML
    private TableColumn<Categoria, String> colunaTipo;
    @FXML
    private TextField campoNome;

    @FXML
    public void initialize() {
        colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaTipo.setCellValueFactory(cell -> javafx.beans.property.SimpleStringProperty
                .simpleStringProperty(cell.getValue().isSeed() ? "Padrão" : "Personalizada"));
        tabelaCategorias.setItems(categorias);
        carregarCategorias();
    }

    @FXML
    private void cadastrarCategoria() {
        try {
            categoriaService.cadastrar(campoNome.getText());
            campoNome.clear();
            carregarCategorias();
        } catch (RegraNegocioException exception) {
            new Alert(Alert.AlertType.WARNING, exception.getMessage()).showAndWait();
        }
    }

    public Categoria cadastrarCategoria(String nome) {
        return categoriaService.cadastrar(nome);
    }

    public List<Categoria> listarCategorias() {
        return categoriaService.listarTodos();
    }

    private void carregarCategorias() {
        categorias.setAll(categoriaService.listarTodos());
    }
}

