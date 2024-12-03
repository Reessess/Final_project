module org.example.finalrequirements_infoman {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.finalrequirements_infoman to javafx.fxml;
    exports org.example.finalrequirements_infoman;
}