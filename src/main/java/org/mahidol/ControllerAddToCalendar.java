package org.mahidol;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ControllerAddToCalendar implements Initializable {
    public DatePicker dpStartDate;
    public DatePicker dpEndDate;
    public Label lbError;
    private ObservableList<SubjectData> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dpStartDate.setValue(LocalDate.now());
    }

    public void getData(ObservableList<SubjectData> data){
        this.data = data;
    }

    public void AddToCalendar(ActionEvent event) throws Exception {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();
        if(startDate == null || endDate == null){
            lbError.setText("start date and end date is required");
            return;
        }
        else if(endDate.isBefore(startDate)||endDate.isEqual(startDate)){
            lbError.setText("invalid time interval");
            return;
        }
        else if(ChronoUnit.MONTHS.between(startDate,endDate)>12){
            lbError.setText("Time interval should be within 1 year");
            return;
        }
        lbError.setText("");

        GoogleCalendar calendar = new GoogleCalendar();
        calendar.getConnection();

        final LocalDate START_DATE = startDate;
        Thread t = new Thread(new Runnable() {
            public void run() {
                LocalDate start_date;
                for(SubjectData subjectData: data){
                    start_date = mapDayOfWeek(START_DATE, subjectData.getDay());
                    LocalTime startTime = LocalTime.of(subjectData.getStartTime()/60,subjectData.getStartTime()%60);
                    LocalTime endTime = LocalTime.of(subjectData.getFinishTime()/60,subjectData.getFinishTime()%60);
                    try {
                        calendar.insertCalendar(subjectData,start_date,endDate,startTime,endTime);
                        System.out.println(subjectData.getCode());
                        System.out.println(start_date);
                        System.out.println(endDate);
                        System.out.println(startTime);
                        System.out.println(endTime);
                        System.out.println(" ");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }});
   t.start();
        Stage stage = (Stage) dpStartDate.getScene().getWindow();
        stage.close();
    }

    public LocalDate mapDayOfWeek(LocalDate startDate, int day){
        int calenderDay = startDate.getDayOfWeek().getValue();
        if(day>=calenderDay){
          startDate = startDate.plusDays(day-calenderDay);
        }
        else {
            startDate = startDate.plusDays(7+day-calenderDay);
        }
        return startDate;
    }

}
