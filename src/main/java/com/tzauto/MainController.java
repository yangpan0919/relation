package com.tzauto;

import com.tzauto.entity.RelationEntity;
import com.tzauto.server.MainServer;
import de.felixroske.jfxsupport.FXMLController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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

    public  ObservableList<RelationInfo> list = FXCollections.observableArrayList();

    @FXML
    private TableView<RelationInfo> dataTable;     //tableView
    @FXML
    private TableColumn<RelationInfo, String> lot = new TableColumn<>();
    @FXML
    private TableColumn<RelationInfo, String> materialNumber = new TableColumn<>();
    @FXML
    private TableColumn<RelationInfo, String> recipeName = new TableColumn<>();

    @Autowired
    MainServer mainServer;








    @Override
    public void initialize(URL location, ResourceBundle resources) {

        dataTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RelationInfo>() {//单击事件
            @Override
            public void changed(ObservableValue<? extends RelationInfo> observable, RelationInfo oldValue, RelationInfo newValue) {
                System.out.println(newValue.getLot());
            }
        });

        dataTable.setRowFactory( tv -> {
            TableRow<RelationInfo> row = new TableRow<RelationInfo>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    RelationInfo relationInfo = row.getItem();
                    System.out.println(relationInfo.getLot());
                    System.out.println(relationInfo.getMaterialNumber());
                    System.out.println(relationInfo.getRecipeName());
                }
            });
            return row ;
        });
        lot.setCellValueFactory(celldata -> celldata.getValue().lotProperty());
        materialNumber.setCellValueFactory(celldata -> celldata.getValue().materialNumberProperty());
        recipeName.setCellValueFactory(celldata -> celldata.getValue().recipeNameProperty());

        List<RelationEntity> all = mainServer.getAll();

        for (int i = 0; i < all.size(); i++) {
            RelationEntity dataTableProperty = all.get(i);

            RelationInfo property = new RelationInfo(dataTableProperty.getLot(),dataTableProperty.getMaterialNumber(),
                    dataTableProperty.getRecipeName(),dataTableProperty.getId());
            list.add(property);
        }
        dataTable.setItems(list);
        System.out.println(all);

    }

    public void delete(ActionEvent actionEvent) {
        System.out.println("delete");
        System.out.println(dataTable.getSelectionModel().getFocusedIndex());
        dataTable.getSelectionModel().getSelectedItems().forEach(x->{
            System.out.println(x.getLot());
            System.out.println(x.getMaterialNumber());
            System.out.println(x.getRecipeName());
        });
    }

    public void add(ActionEvent actionEvent) {
        System.out.println("add");
    }

    public void update(ActionEvent actionEvent) {
        System.out.println("update");
    }
}
