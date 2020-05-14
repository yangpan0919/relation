package com.tzauto;

import com.tzauto.server.MainServer;
import com.tzauto.javafxSupport.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class ParmController implements Initializable {

    public ObservableList<RelationInfo> list = FXCollections.observableArrayList();

    @FXML
    private TextField deviceCode;
    @FXML
    private TextField materialNumber;
    @FXML
    private TextField recipeName;
    @FXML
    private TextField fixtureno;

    public TextField getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(TextField deviceCode) {
        this.deviceCode = deviceCode;
    }

    public TextField getMaterialNumber() {
        return materialNumber;
    }

    public void setMaterialNumber(TextField materialNumber) {
        this.materialNumber = materialNumber;
    }

    public TextField getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(TextField recipeName) {
        this.recipeName = recipeName;
    }

    @Autowired
    MainServer mainServer;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        test();
    }


    public void action(ActionEvent actionEvent) {
        MainController.relationEntity.setDeviceCode(deviceCode.getText().trim());
        MainController.relationEntity.setMaterialNumber(materialNumber.getText().trim());
        MainController.relationEntity.setFixtureno(fixtureno.getText().trim());
        MainController.relationEntity.setRecipeName(recipeName.getText().trim());
        mainServer.active(MainController.relationEntity);
    }

    public void test() {
        try {
            deviceCode.setText(MainController.relationEntity.getDeviceCode());
            materialNumber.setText(MainController.relationEntity.getMaterialNumber());
            recipeName.setText(MainController.relationEntity.getRecipeName());
            fixtureno.setText(MainController.relationEntity.getFixtureno());
        } catch (Exception e) {

        }
    }


}
