module g3.project.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.scripting;
    requires java.sql;
    requires jython.slim;
    requires nu.xom;
    requires org.jfxtras.styles.jmetro;
    requires com.jthemedetector;
    requires java.xml;
    requires org.fxmisc.flowless;
    requires org.fxmisc.richtext;
    requires org.fxmisc.undo;
    
    opens g3.project.xmlIO to java.xml;
    opens g3.project.core to javafx.fxml, jython.slim;
    opens g3.project.elements to jython.slim;
    opens g3.project.ui to javafx.fxml;
    exports g3.project.core;
    exports g3.project.elements;
    exports g3.project.ui;
    exports g3.project.network;
}
