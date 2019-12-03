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
public class MixController implements Initializable {

    @FXML
    private TextField lot;
    @FXML
    private TextField fixtureno;
    @Autowired
    MainServer mainServer;

    public TextField getLot() {
        return lot;
    }

    public void setLot(TextField lot) {
        this.lot = lot;
    }

    public TextField getFixtureno() {
        return fixtureno;
    }

    public void setFixtureno(TextField fixtureno) {
        this.fixtureno = fixtureno;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * 进行解混
     *
     * @param actionEvent
     */
    public void action(ActionEvent actionEvent) {
        mainServer.mix(lot.getText().trim(), fixtureno.getText().trim());

    }

    public void clearData() {
        lot.setText("");
        fixtureno.setText("");
    }
}
