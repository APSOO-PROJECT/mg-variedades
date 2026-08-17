module com.empresa.pdv {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;
    requires atlantafx.base;

    // Permite que o JavaFX acesse os Controllers via reflexão
    opens com.empresa.pdv.controllers to javafx.fxml;
    opens com.empresa.pdv to javafx.fxml;

    // Exporta os pacotes principais
    exports com.empresa.pdv;
    exports com.empresa.pdv.controllers;
}