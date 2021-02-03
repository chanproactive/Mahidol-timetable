package org.mahidol;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerSubject {

    public Label lbSubjectCode;
    public Label lbSubjectName;
    public Label lbStartTime;
    public Label lbFinishTime;
    public Label lbCredits;
    public Circle colorCircle;
    public TextArea taSubjectDetails;
    private SubjectData subjectData;
    private String color;
    private int gpIndex;
    private ControllerMain main;

    public void initData (SubjectData subjectData, String color, int gpIndex, ControllerMain main){
        this.subjectData = subjectData;
        this.color =color;
        this.gpIndex = gpIndex;
        this.main = main;
        insertDataToView();
    }

    public void insertDataToView(){
        lbSubjectCode.setText(subjectData.getCode());
        lbSubjectName.setText(subjectData.getCourse());
        lbStartTime.setText((subjectData.getStartTime()/60)+":"+(subjectData.getStartTime()%60==0? "00": subjectData.getStartTime()%60));
        lbFinishTime.setText((subjectData.getFinishTime()/60)+":"+(subjectData.getFinishTime()%60==0? "00": subjectData.getFinishTime()%60));
        lbCredits.setText(String.valueOf(subjectData.getCredits()));
        Text text1 = new Text(subjectData.getDetails());
        taSubjectDetails.setText(subjectData.getDetails());
        System.out.println("color ="+color);
        colorCircle.setFill(Color.web(color));
    }

    public void btEditOnClick(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("window_add_subject.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = (Stage)((Node) actionEvent.getSource()).getScene().getWindow();
            ControllerAddSubject controller = fxmlLoader.getController();
            controller.insertDataToView(subjectData, color);
            controller.initData(main);
            controller.setMode(2, gpIndex);
            stage.setTitle("Edit subject details");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

