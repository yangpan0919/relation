package com.tzauto.server;

import com.tzauto.dao.MainMapping;
import com.tzauto.entity.RelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Administrator on 2019/8/16.
 */
@Service
public class MainServer {

    @Autowired
    MainMapping mainMapping;

    public List<RelationEntity> getAll() {
        return mainMapping.getAll();
    }
}
