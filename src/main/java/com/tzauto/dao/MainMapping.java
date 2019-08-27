package com.tzauto.dao;

import com.tzauto.entity.RelationEntity;

import java.util.List;

/**
 * Created by Administrator on 2019/8/16.
 */

public interface MainMapping {

    List<RelationEntity> getAll();

    void delete(int id);

    void add(RelationEntity relationEntity);

    void update(RelationEntity relationEntity);

    RelationEntity query(RelationEntity relationEntity);

    String queryUser(String name, String passWord);
}
