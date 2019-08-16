package com.tzauto;

import com.tzauto.entity.RelationEntity;
import com.tzauto.server.MainServer;
import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@FXMLController
public class MainController implements Initializable {


    @FXML
    private TableColumn<RelationInfo, String> lot = new TableColumn<>();
    @FXML
    private TableColumn<RelationInfo, String> materialNumber = new TableColumn<>();
    @FXML
    private TableColumn<RelationInfo, String> programName = new TableColumn<>();

    @Autowired
    MainServer mainServer;








    @Override
    public void initialize(URL location, ResourceBundle resources) {

        List<RelationEntity> all = mainServer.getAll();

    }
}
