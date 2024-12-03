module org.example.finalrequirements_infoman {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires jbcrypt;

    opens com.jrees.finalrequirements_infoman.controllers to javafx.fxml, javafx.base;
    exports com.jrees.finalrequirements_infoman.controllers;

    opens com.jrees.finalrequirements_infoman to javafx.fxml;
    exports com.jrees.finalrequirements_infoman;
}