package org.mahidol;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class DialogGoogleSignIn {
    public Button btOK;

    public void OKClick(ActionEvent event) throws Exception {
        GoogleSheet googleSheet = new GoogleSheet();
        Runnable r = new Runnable() {
            public void run() {
                try {
                    googleSheet.readGoogleSheet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(r).start();
        Stage stage = (Stage) btOK.getScene().getWindow();
        stage.close();
    }

    public void btCancel(ActionEvent event) {
        Stage stage = (Stage) btOK.getScene().getWindow();
        stage.close();
    }
}
