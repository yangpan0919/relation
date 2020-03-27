package com.tzauto;

import com.tzauto.javafxSupport.FXMLController;
import com.tzauto.server.MainServer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class AddDataController implements Initializable {


    @FXML
    private TextField lot;
    @FXML
    private RadioButton isMain;
    @FXML
    private RadioButton notMain;

    @FXML
    private TextField item2;
    @FXML
    private TextField item4;
    @FXML
    private TextField item5;
    @FXML
    private TextField item6;
    ToggleGroup tg;
    @Autowired
    MainServer mainServer;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tg = new ToggleGroup();
        isMain.setSelected(true);
        isMain.setToggleGroup(tg);
        notMain.setToggleGroup(tg);
//        tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
//            @Override
//            public void changed(ObservableValue<? extends Toggle> changed, Toggle oldVal, Toggle newVal) {
//                RadioButton temp_rb = (RadioButton) newVal;
//                System.out.println(temp_rb.getText());
//            }
//        });
    }


    public void action(ActionEvent actionEvent) {
        String selectedText = ((RadioButton) tg.getSelectedToggle()).getText();
        boolean b = mainServer.addData(lot.getText().trim(), item2.getText().trim(), item4.getText().trim(), item5.getText().trim(), item6.getText().trim(), selectedText);
        if (b) {
            lot.setText("");
            item2.setText("");
            item4.setText("");
            item5.setText("");
            item6.setText("");
            isMain.setSelected(true);
        }
    }


}
