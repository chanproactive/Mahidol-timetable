package org.mahidol;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerAddSubject implements Initializable {

    public TextField tfSubjectCode;
    public TextField tfSubjectName;
    public TextField tfRoom;
    public Label st;
    public ChoiceBox<Integer> cbStartTimeH;
    public ChoiceBox<String> cbStartTimeM;
    public ChoiceBox<Integer> cbFinishTimeH;
    public ChoiceBox<String> cbFinishTimeM;
    public ChoiceBox<String> cbDay;
    public Label ft;
    public Label day;
    public ColorPicker colorPicker;
    public Slider sdCredits;
    public TextArea taSubjectDetails;
    public Label lbError;
    public Button btDone;
    ControllerMain main;
    private int mode = 1;
    private SubjectData sd;
    private int gpIndex;

    @Override
    //init data
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbStartTimeH.getItems().addAll(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        cbFinishTimeH.getItems().addAll(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        cbStartTimeM.getItems().addAll("00", "30");
        cbFinishTimeM.getItems().addAll("00", "30");
        cbDay.getItems().addAll("Mon", "Tue", "Wed", "Thu", "Fri");
        cbStartTimeH.setValue(8);
        cbFinishTimeH.setValue(11);
        cbStartTimeM.setValue("30");
        cbFinishTimeM.setValue("30");
        cbDay.setValue("Mon");
        colorPicker.setValue(Color.web(randomColor()));
    }
    //mode 1 = add, mode 2 = edit
    public void setMode(int mode, int gpIndex) {
        this.mode = mode;
        this.gpIndex = gpIndex;
    }

    public void initData(ControllerMain main) {
        this.main = main;
    }

    public void insertDataToView(SubjectData subjectData, String color) {
        if(color.equals("")){
            color = randomColor();
        }
        this.sd = subjectData;
        colorPicker.setValue(Color.web(color));
        tfSubjectCode.setText(subjectData.getCode());
        tfSubjectName.setText(subjectData.getCourse());
        cbDay.setValue(mapDayBack(subjectData.getDay()));
        cbStartTimeH.setValue(subjectData.getStartTime() / 60);
        cbStartTimeM.setValue(String.valueOf(subjectData.getStartTime() % 60 == 0 ? "00" : subjectData.getStartTime() % 60));
        cbFinishTimeH.setValue(subjectData.getFinishTime() / 60);
        cbFinishTimeM.setValue(String.valueOf(subjectData.getFinishTime() % 60 == 0 ? "00" : subjectData.getFinishTime() % 60));
        tfRoom.setText(subjectData.getRoom());
        sdCredits.setValue(subjectData.getCredits());
        taSubjectDetails.setText(subjectData.getDetails());
    }

    public void btAddOnclick(ActionEvent event) {
        ObservableList<SubjectData> data = ControllerMain.data;
        String course = tfSubjectName.getText();
        String code = tfSubjectCode.getText();
        int credits = (int) sdCredits.getValue();
        String room = tfRoom.getText();
        String color;
        int day = mapDay((cbDay.getValue()));
        int startTime = cbStartTimeH.getValue() * 60 + Integer.parseInt(cbStartTimeM.getValue());
        int finishTime = cbFinishTimeH.getValue() * 60 + Integer.parseInt(cbFinishTimeM.getValue());
        String detail = taSubjectDetails.getText();
        int classDuration = (finishTime - startTime) / 30;

        //Check blank subject code
        if (code.equals("")) {
            lbError.setText("Subject code is required");
            return;
        }

        //Time validate for each mode
        if (mode == 2) {
            //skip checking itself in mode2
            for (SubjectData i : data) {
                if (i == sd) {
                } else {
                    if (checkOverlapTime(day, startTime, finishTime, i)) return;
                }
            }
        }
        //Time validate for mode 1
        else {
            for (SubjectData i : data) {
                if (checkOverlapTime(day, startTime, finishTime, i)) return;
            }
        }

        //Class duration validation
        if (classDuration <= 0) {
            lbError.setText("invalid time!");
            return;
        }
        lbError.setText("");

        //convert color
        StringBuilder sbColor = new StringBuilder(String.valueOf(colorPicker.getValue()));
        sbColor.delete(0, 2);
        sbColor.delete(6, 8);
        sbColor.insert(0, "#");
        color = sbColor.toString();

        //Pack information to subject data
        SubjectData subjectData = new SubjectData(
                code,
                course,
                credits,
                startTime,
                finishTime,
                room,
                day,
                detail
        );

        //if mode = 2 remove the subjectData first
        if (mode == 2) {
            data.remove(gpIndex - 5);
            main.gridPane.getChildren().remove(gpIndex);
            main.gpRemoveColumn();
        }

        data.add(subjectData);
        main.updateSubjectData(subjectData, color);
        Stage stage = (Stage) btDone.getScene().getWindow();
        stage.close();
    }

    private boolean checkOverlapTime(int day, int startTime, int finishTime, SubjectData i) {
        if ((startTime >= i.getStartTime()) & (startTime < i.getFinishTime()) & (day == i.getDay())) {
            lbError.setText("Time overlap!");
            return true;
        } else if ((finishTime > i.getStartTime()) & (finishTime <= i.getFinishTime()) & (day == i.getDay())) {
            lbError.setText("Time overlap!");
            return true;
        }
        return false;
    }

    private int mapDay(String day) {
        int mappedDay;
        if (day.equals("Mon")) mappedDay = 1;
        else if (day.equals("Tue")) mappedDay = 2;
        else if (day.equals("Wed")) mappedDay = 3;
        else if (day.equals("Thu")) mappedDay = 4;
        else if (day.equals("Fri")) mappedDay = 5;
        else mappedDay = 1;
        return mappedDay;
    }

    public String mapDayBack(int day) {
        String mappedDay = "";
        if (day == 1) mappedDay = "Mon";
        else if (day == 2) mappedDay = "Tue";
        else if (day == 3) mappedDay = "Wed";
        else if (day == 4) mappedDay = "Thu";
        else if (day == 5) mappedDay = "Fri";
        return mappedDay;
    }

    private String randomColor() {
        int r = (int) (Math.random() * 12);
        String[] perSetColor = {
                "#99cccc", "#99b3ff", "#b399ff", "#cc99cc", "#e699b3", "#ff9999",
                "#ffb380", "#ffcc99", "#ffe680", "#ffff80", "#e6e699", "#99cc99"
        };
        return perSetColor[r];
    }

}
