package org.mahidol;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main extends Application {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("window_main.fxml"));

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        Parent root = loader.load();
        primaryStage.setTitle("Mahidol timetable generator");
        primaryStage.setScene(new Scene(root, 830, 620));
        primaryStage.show();
        checkGoogleSignIn();

    }

    //Save data to data base before closing
    @Override
    public void stop() throws Exception {
        ControllerMain controller = loader.getController();
        controller.deleteDB("user_data","user");
        controller.saveToDatabase("user_data","user");
    }

    public void checkGoogleSignIn() throws Exception {
            File file = new File("tokens\\sheetTokens\\StoredCredential");
            if(file.exists()){
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;
                int length = 0;
                while ((st = br.readLine()) != null){
                    length += st.length();
                }
                br.close();
               if(length<100){
                   showGoogleSignInDialog();
               }
               else{
                   GoogleSheet googleSheet = new GoogleSheet();
                   Runnable r = new Runnable() {
                       public void run() {
                           try {
                               Thread.sleep(5000);
                               googleSheet.readGoogleSheet();
                           } catch (Exception e) {
                               e.printStackTrace();
                           }
                       }
                   };
                   new Thread(r).start();
                }
            }
            else {
                showGoogleSignInDialog();
            }
        }
        private void showGoogleSignInDialog(){
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dialog_google_sign_in.fxml"));
                Parent root1 = fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Alert");
                stage.setScene(new Scene(root1));
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    public static void main(String[] args) {
        launch(args);
    }
}


