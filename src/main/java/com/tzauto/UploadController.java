package com.tzauto;

import com.tzauto.javafxSupport.FXMLController;
import com.tzauto.server.MainServer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class UploadController implements Initializable {


    @FXML
    private TextField lot;

    @FXML
    private TextField endTime;

    @FXML
    //主途程序字段，存储的是，
    private TextField mainSerial;

    @Autowired
    MainServer mainServer;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    public void action(ActionEvent actionEvent) {
        mainServer.upload(lot.getText().trim(), endTime.getText().trim(), mainSerial.getText().trim());
    }


}
