package com.tzauto.temp2;

import cn.tzauto.octopus.biz.device.domain.Station;

public interface StationMapper {
    int deleteByPrimaryKey(String id);

    int insert(Station record);

    int insertSelective(Station record);

    Station selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Station record);

    int updateByPrimaryKey(Station record);

    LotInfo queryLotNum(String lot);

    void insertLotInfo(LotInfo info);

    void updateLotInfo(LotInfo lotInfo);

    void lotInfoBak(LotInfo lotInfo);

    void deleteLotInfo(String lotid);
}