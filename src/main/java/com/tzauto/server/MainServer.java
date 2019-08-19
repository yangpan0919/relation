package com.tzauto.server;

import com.tzauto.CommonUiUtil;
import com.tzauto.MainController;
import com.tzauto.ParmView;
import com.tzauto.RelationApplication;
import com.tzauto.dao.MainMapping;
import com.tzauto.entity.RelationEntity;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Administrator on 2019/8/16.
 */
@Service
public class MainServer {

    @Autowired
    MainController mainController;
    @Autowired
    MainMapping mainMapping;
    @Autowired
    ParmView parmView;

    public List<RelationEntity> getAll() {
        return mainMapping.getAll();
    }

    public void delete(int id) {
        mainMapping.delete(id);

        mainController.flushData();
    }

    public void active(RelationEntity relationEntity) {

        if(relationEntity.getLot().trim().equals("")||relationEntity.getRecipeName().trim().equals("")||relationEntity.getMaterialNumber().trim().equals("")){
            CommonUiUtil.alert(Alert.AlertType.INFORMATION, "请填写内容！！！");
            return;
        }
        if(relationEntity.getId() == null){
            mainMapping.add(relationEntity);
        }else{
            mainMapping.update(relationEntity);
        }
        Object presenter = parmView.getPresenter();
        Node viewWithoutRootContainer = parmView.getViewWithoutRootContainer();

        mainController.flushData();
    }
}
