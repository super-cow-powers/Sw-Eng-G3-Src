module g3.project.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires nu.xom;
    requires org.jfxtras.styles.jmetro;
    requires com.jthemedetector;
    
    opens g3.project.core to javafx.fxml;
    opens g3.project.ui to javafx.fxml;
    exports g3.project.core;
}
