module com.iyed_houhou.mazesolvationsemulation {
    requires javafx.controls;
    requires javafx.fxml;

    // Open packages containing FXML files
    opens com.iyed_houhou.mazesolvationsemulation.application.controllers to javafx.fxml;
    opens com.iyed_houhou.mazesolvationsemulation.View to javafx.fxml;

    // Export main package
    exports com.iyed_houhou.mazesolvationsemulation.application;
}



