package com.tzauto;

import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class MainController implements Initializable {


    @FXML
    private Label helloLabel;

    @FXML
    private TextField nameField;
    @FXML
    private TextArea text;
    @FXML
    ChoiceBox angleNum;

    public TextArea getText() {
        return text;
    }

    public void setText(TextArea text) {
        this.text = text;
    }

    @FXML
    public void exportclick(Event event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择文件");
//        FileChooser fileChooser = new FileChooser();
        Stage stage = new Stage();
        final File selectedDirectory = chooser.showDialog(stage);
        if (selectedDirectory != null) {
            nameField.setText(selectedDirectory.getAbsolutePath());
        }

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameField.setEditable(false);
        ObservableList<String> options = FXCollections.observableArrayList("0", "90", "180", "270");
        angleNum.setItems(options);
        angleNum.setValue("0");
    }
}
