module org.mahidol {
    requires javafx.controls;
    requires javafx.fxml;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.jackson2;
    requires google.api.services.sheets.v4.rev581;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires google.api.services.calendar.v3.rev305;
    requires java.desktop;
    requires java.sql;
    requires sqlite.jdbc;



    opens org.mahidol to javafx.fxml;
    exports org.mahidol;
}
