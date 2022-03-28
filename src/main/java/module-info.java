module g3.project.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.scripting;
    requires java.sql;
    requires jython.slim;
    requires nu.xom;
    requires org.jfxtras.styles.jmetro;
    requires com.jthemedetector;
    
    requires org.fxmisc.flowless;
    requires org.fxmisc.richtext;
    requires org.fxmisc.undo;
    
    opens g3.project.core to javafx.fxml;
    opens g3.project.ui to javafx.fxml;
    exports g3.project.core;
}
