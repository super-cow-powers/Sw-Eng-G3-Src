module g3.project.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires nu.xom;
    
    opens g3.project.core to javafx.fxml;
    exports g3.project.core;
}
