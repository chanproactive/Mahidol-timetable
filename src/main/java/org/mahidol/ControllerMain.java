package org.mahidol;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.*;


public class ControllerMain implements Initializable {
    public VBox searchBox;
    public RadioMenuItem rbShowSubjectName;
    public RadioMenuItem rbShowSubjectCode;
    public RadioMenuItem rbShowBoth;
    public VBox vbAllInformation;
    public MenuItem miSaveToDatabase;
    public ChoiceBox<String> cbSemester;
    public TextField tfTableName;
    public GridPane gridPane;
    public ChoiceBox<String> cbYear;
    public ChoiceBox<String> cbMajor;
    public TableView<SubjectData> tbSubjectData;
    public TableColumn<SubjectData, String> tcTime;
    public TableColumn<SubjectData, String> tcCode;
    public TableColumn<SubjectData, String> tcCourse;
    public TableColumn<SubjectData, Integer> tcCredits;
    public GridPane gpTime;
    public VBox vbTableFrame;
    public GridPane gpBackGround;
    static ObservableList<SubjectData> data = FXCollections.observableArrayList();
    public Button addButton;
    public TextField sb;
    public MenuItem miUploadToCloud;
    public Menu menuAdmin;


    ConnectDB connectDB = new ConnectDB();

    int colorCounter = 0;

    public ControllerMain() throws Exception {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbYear.getItems().addAll("1", "2", "3", "4");
        cbSemester.getItems().addAll("1", "2");
        cbMajor.getItems().addAll("ME", "IN", "BM", "BE");
        cbYear.setValue("1");
        cbSemester.setValue("1");
        cbMajor.setValue("ME");
        tcCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        tcCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        tcCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));
        tcTime.setCellValueFactory(new PropertyValueFactory<>("stringTime"));
        try {
            loadDatabase("user_data","user");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //add search box items
        SortedSet<String> menuItem = new TreeSet<>();
        try {
         ResultSet rs =  connectDB.readDB("subject_data","");
            while (rs.next()) {
                menuItem.add(rs.getString("code"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        AutoCompleteTextField tfSearchBar = new AutoCompleteTextField(menuItem, this);
        tfSearchBar.setPromptText("Search subjects here");
        tfSearchBar.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER){
                try {
                    getSubjectDataWithCode(tfSearchBar.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        searchBox.getChildren().add(tfSearchBar);
    }

    public void getSubjectDataWithCode(String code) throws Exception {
        ResultSet rs = connectDB.getSubjectDataFromCode(code);
        SubjectData sd = new SubjectData(
                rs.getString("code"),
                rs.getString("course"),
                rs.getInt("credits"),
                rs.getInt("startTime"),
                rs.getInt("finishTime"),
                rs.getString("room"),
                rs.getInt("day"),
                rs.getString("details")
        );
        ConnectDB.conn.close();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("window_add_subject.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            ControllerAddSubject controller = fxmlLoader.getController();
            controller.initData(this);
            controller.insertDataToView(sd, "");
            stage.setTitle("Add subject details");
            stage.setScene(new Scene(root1));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Manage subject data
    public void insertSubjectToGridPane(SubjectData subjectData, String color) {
        if (color.equals("")) color = perSetColor();
        String finalColor = color;
        double mappedStartTime = mapTime(subjectData.getStartTime());
        double mapFinishTime = mapTime(subjectData.getFinishTime());
        int gpSize = gridPane.getColumnConstraints().size();
        if (mapFinishTime > gpSize) {
            for (int i = 1; i <= mapFinishTime - gpSize; i++) {
                gpAddColumn();
            }
        }
        //create subject node
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color:" + color);
        vbox.getStyleClass().add("nodeRadius");
        int classDuration = (subjectData.getFinishTime() - subjectData.getStartTime()) / 30;
        Label lbSubjectCode = new Label(subjectData.getCode());
        Label lbSubjectName = new Label(subjectData.getCourse());
        Label lbRoom = new Label(subjectData.getRoom());
        vbox.getChildren().addAll(lbSubjectCode, lbSubjectName, lbRoom);
        gridPane.add(vbox, (int) mappedStartTime, subjectData.getDay() - 1, classDuration, 1);

        //add mouse event to the subject node
        vbox.setOnMouseClicked(event ->
                {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("window_subject.fxml"));
                            Parent root1 = fxmlLoader.load();
                            Stage stage = new Stage();
                            stage.initModality(Modality.APPLICATION_MODAL);
                            ControllerSubject controller = fxmlLoader.getController();
                            controller.initData(subjectData, finalColor, gridPane.getChildren().indexOf(vbox), ControllerMain.this);
                            stage.setTitle("Subject details");
                            stage.setScene(new Scene(root1));
                            stage.showAndWait();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        //handle node contextMenu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Delete");
        MenuItem menuItem2 = new MenuItem("Edit");

        //handle contextMenu delete
        menuItem1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                gridPane.getChildren().remove(vbox);
                data.remove(subjectData);
                if (mapFinishTime > 18) {
                    gpRemoveColumn();
                }

            }
        });


        //handle contextMenu edit
        menuItem2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("window_add_subject.fxml"));
                    Parent root1 = fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    ControllerAddSubject controller = fxmlLoader.getController();
                    controller.insertDataToView(subjectData, finalColor);
                    controller.initData(ControllerMain.this);
                    controller.setMode(2, gridPane.getChildren().indexOf(vbox));
                    stage.setTitle("Edit subject details");
                    stage.setScene(new Scene(root1));
                    stage.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        contextMenu.getItems().addAll(menuItem1, menuItem2);
        vbox.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(vbox, event.getScreenX(), event.getScreenY());
            }
        });
    }

    public void updateSubjectData(SubjectData subjectData, String color) {
        tbSubjectData.setItems(data);
        insertSubjectToGridPane(subjectData, color);
    }

    private int mapTime(int rawTime) {
        return (int) ((rawTime * 1.0 / 60 - 8.0) * 2 + 1);
    }

    private String perSetColor() {
        String[] perSetColor = {
                "#ffe680", "#82a694","#99b3ff", "#98c0de","#b399ff","#b3dbdd", "#cc99cc", "#e699b3", "#ff9999",
                "#ffb380", "#ffcc99", "#99cccc", "#ffff80", "#dfe6c5","#e6e699", "#99cc99"
        };
        colorCounter++;
        if (colorCounter == 12) {
            colorCounter = 0;
        }
        return perSetColor[colorCounter];
    }


    //ActionEvent from user
    public void ClearData(ActionEvent actionEvent) {
        data.clear();
        int size = gridPane.getChildren().size() - 1;
        if (size >= 5) {
            gridPane.getChildren().subList(5, size + 1).clear();
        }
    }

    public void adminLogin(ActionEvent actionEvent) throws Exception {
        GoogleSheet googleSheet = new GoogleSheet();
        ArrayList<String> passwords = googleSheet.readUserInfo();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Admin password");
        dialog.setHeaderText(null);
        dialog.setContentText("Admin password");
        Optional<String> result = dialog.showAndWait();
        for(String password: passwords){
            if (result.isPresent()) {
                if (result.get().equals(password)) {
                    menuAdmin.setDisable(false);
                }
            }
        }

    }

    public void onGetPresetData(ActionEvent actionEvent) throws Exception {
        loadDatabase("subject_data",cbMajor.getValue() + cbYear.getValue() + cbSemester.getValue());
    }

    public void saveAsPng(ActionEvent actionEvent) {
        Node node = vbTableFrame;
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setTransform(new Scale(2, 2));
        WritableImage snapshot = node.snapshot(snapshotParameters, null);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as");
        fileChooser.setInitialFileName(tfTableName.getText());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        File file = fileChooser.showSaveDialog(node.getScene().getWindow());
        try {
            ImageIO.write(Objects.requireNonNull(SwingFXUtils.fromFXImage(snapshot, null)), "png", file);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void addSubjectDetails(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("window_add_subject.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            ControllerAddSubject controller = fxmlLoader.getController();
            controller.initData(this);
            stage.setTitle("Add subject details");
            stage.setScene(new Scene(root1));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Database handling
    public void OnSaveToDataBaseClick() throws Exception {
        String parameter = tfTableName.getText();
        deleteDB("subject_data",parameter);
        saveToDatabase("subject_data",parameter);
    }

    public void saveToDatabase(String tableName, String parameter) throws Exception {
        if (parameter.equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alert");
            alert.setContentText("The table name cannot be empty");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        for (SubjectData sd : data) {
            connectDB.insertDB(sd, tableName, parameter);
        }
    }

    public void loadLastDatabase(ActionEvent actionEvent) throws Exception {
        loadDatabase("user_data","user");
    }

    public void loadDatabase(String tableName,String parameter) throws Exception {
        ResultSet rs = connectDB.readDB(tableName, parameter);
        while (rs.next()) {
            data.add(new SubjectData(
                    rs.getString("code"),
                    rs.getString("course"),
                    rs.getInt("credits"),
                    rs.getInt("startTime"),
                    rs.getInt("finishTime"),
                    rs.getString("room"),
                    rs.getInt("day"),
                    rs.getString("details")
            ));
        }
        ConnectDB.conn.close();
        tbSubjectData.setItems(data);
        for (SubjectData sd : data) {
            insertSubjectToGridPane(sd, "");
        }
    }

    public void deleteDB(String tableName, String parameter) throws Exception {
        connectDB.delete(tableName, parameter);
    }

    //View option
    public void onRadioSelect(ActionEvent event) {
        int size = gridPane.getChildren().size() - 1;
        //Show only subject name
        if (rbShowSubjectName.isSelected()) {
            for (int i = size; i > 4; i--) {
                VBox vBox = (VBox) gridPane.getChildren().get(i);
                vBox.getChildren().get(0).setVisible(false);
                vBox.getChildren().get(0).setManaged(false);
                vBox.getChildren().get(1).setVisible(true);
                vBox.getChildren().get(1).setManaged(true);
            }
        }
        //Show only subject code
        if (rbShowSubjectCode.isSelected()) {
            for (int i = size; i > 4; i--) {
                Node node = gridPane.getChildren().get(i);
                VBox vBox = (VBox) node;
                vBox.getChildren().get(0).setVisible(true);
                vBox.getChildren().get(0).setManaged(true);
                vBox.getChildren().get(1).setVisible(false);
                vBox.getChildren().get(1).setManaged(false);
            }
        }
        //Show both subject code and subject name
        if (rbShowBoth.isSelected()) {
            for (int i = size; i > 4; i--) {
                VBox vBox = (VBox) gridPane.getChildren().get(i);
                vBox.getChildren().get(0).setVisible(true);
                vBox.getChildren().get(0).setManaged(true);
                vBox.getChildren().get(1).setVisible(true);
                vBox.getChildren().get(1).setManaged(true);
            }
        }
    }

    //Manage gridPane
    public void gpAddColumn() {
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.SOMETIMES);
        col.setMinWidth(10);
        col.setPrefWidth(100);
        gridPane.getColumnConstraints().add(col);
        int size = gridPane.getColumnConstraints().size();
        gpBackGround.getColumnConstraints().get(size - 1).setMinWidth(10);
        gpBackGround.getColumnConstraints().get(size - 1).setPrefWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.SOMETIMES);
        col2.setMinWidth(10);
        col2.setPrefWidth(100);
        gpTime.getColumnConstraints().add(col);
        int tableTime = gpTime.getColumnConstraints().size();
        if (tableTime % 2 == 1) {
            String mappedTableTime = ((tableTime + 1) / 2 + 7) + ":00";
            Label time = new Label(mappedTableTime);
            gpTime.add(time, tableTime - 1, 0, 2, 1);
            GridPane.setHalignment(time, HPos.CENTER);
        }

    }

    public void gpRemoveColumn() {
        int gpSize = gridPane.getColumnConstraints().size();
            int maxFinishTime = 18;
            for (SubjectData sd : data) {
                maxFinishTime = Integer.max(mapTime(sd.getFinishTime()), maxFinishTime);
            }
            int d = gpSize - maxFinishTime;
            System.out.println(d);
            for (int i = 1; i <= d; i++) {
                int size = gridPane.getColumnConstraints().size();
                int size2 = gpTime.getColumnConstraints().size();
                gridPane.getColumnConstraints().remove(size - 1);
                gpBackGround.getColumnConstraints().get(size - 1).setMinWidth(0);
                gpBackGround.getColumnConstraints().get(size - 1).setPrefWidth(Control.USE_COMPUTED_SIZE);
                gpTime.getColumnConstraints().remove(size2 - 1);
                if (size2 % 2 == 1) {
                    gpTime.getChildren().remove(gpTime.getChildren().size() - 1);
                }
            }

    }

    //Manage google service
    public void addToGoogleCalendar(ActionEvent event)  {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("window_add_to_calendar.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            ControllerAddToCalendar controller = fxmlLoader.getController();
            controller.getData(data);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteCalendar(ActionEvent event) throws Exception {
        GoogleCalendar calendar = new GoogleCalendar();
        calendar.deleteCalendar();
    }

    public void accessGoogleSheet(ActionEvent event) throws Exception {
        GoogleSheet googleSheet = new GoogleSheet();
        googleSheet.writeGoogleSheet();
    }

    public void onResetAppClick(ActionEvent event) throws Exception {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Are you sure to reset the app?");
        alert.setContentText("All data related to Google service will be deleted");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            File sheetToken = new File("tokens\\sheetTokens\\StoredCredential");
            if(sheetToken.delete()) System.out.println("sheetToken deleted successfully");
            File calendarToken = new File("tokens\\calendarTokens\\StoredCredential");
            if(calendarToken.delete()) System.out.println("calendarToken deleted successfully");
            deleteDB("user_data","user");
        } else {
        }


    }

    public void onReportProblemClick(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report problem");
        alert.setHeaderText(null);
        alert.setContentText("please contact chanproactive@outlook.com");
        alert.showAndWait();
    }
}
