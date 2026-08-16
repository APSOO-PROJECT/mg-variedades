module com.empresa.pdv {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;

    // Permite que o JavaFX acesse as controllers e a classe principal
    opens com.empresa.pdv to javafx.fxml;
    opens com.empresa.pdv.controllers to javafx.fxml;

    exports com.empresa.pdv;
    exports com.empresa.pdv.controllers;
}