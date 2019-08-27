package com.tzauto.server;

import com.tzauto.*;
import com.tzauto.dao.MainMapping;
import com.tzauto.entity.RelationEntity;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLNonTransientException;
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
    @Autowired
    LoginView loginView;
    @Autowired
    MainView mainView;
    @Autowired
    LoginController loginController;

    public List<RelationEntity> getAll() {
        List<RelationEntity> all = mainMapping.getAll();
        all.stream().forEach(x->{
            x.setRecipeName(x.getRecipeName().substring(0,x.getRecipeName().length()-4));
        });
        return all;
    }

    public void delete(int id) {
        mainMapping.delete(id);

        mainController.flushData();
    }

    public void active(RelationEntity relationEntity) {

        if (relationEntity.getRecipeName().equals("")
                || relationEntity.getMaterialNumber().equals("") || relationEntity.getFixtureno().equals("")) {
            CommonUiUtil.alert(Alert.AlertType.INFORMATION, "请将内容填写完整！！！");
            return;
        }

        RelationEntity query = mainMapping.query(relationEntity);

        if (relationEntity.getId() == null) {
            if (query != null) {
                CommonUiUtil.alert(Alert.AlertType.INFORMATION, "相同的批号，料号，序号的记录已存在，删除后可进行添加！！！");
                return;
            }
            relationEntity.setRecipeName(relationEntity.getRecipeName()+".xml");
            mainMapping.add(relationEntity);
        } else {
            if (query != null && (!query.getId().equals(relationEntity.getId()))) {
                CommonUiUtil.alert(Alert.AlertType.INFORMATION, "相同的批号，料号，序号的记录已存在，删除后可进行修改！！！");
                return;
            }
            relationEntity.setRecipeName(relationEntity.getRecipeName()+".xml");
            mainMapping.update(relationEntity);
        }

        parmView.getStage().close();

        mainController.flushData();
    }

    public void login() {
        String name =loginController.getUserName().getText();
        String passWord =loginController.getPassword().getText();
        if(name.equals("")||passWord.equals("")){
            CommonUiUtil.alert(Alert.AlertType.INFORMATION,"请输入用户名或密码!");
            return ;
        }
        name =  mainMapping.queryUser(name,passWord);
        if(name == null){
            CommonUiUtil.alert(Alert.AlertType.INFORMATION,"用户名或密码错误!");
            return ;
        }
        loginView.getStage().close();
        RelationApplication.showView(MainView.class,null,"关系维护",null, Modality.NONE);
    }
}
