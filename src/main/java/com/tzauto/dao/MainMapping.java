package com.tzauto.dao;

import com.tzauto.entity.LotInfo;
import com.tzauto.entity.MixInfo;
import com.tzauto.entity.RelationEntity;
import org.apache.ibatis.annotations.Param;

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

    LotInfo queryLot(String lot);

    LotInfo queryLotTwoParam(@Param("lot") String lot, @Param("mainSerial") String mainSerial);

    void lotInfoBak(String lot);

    void lotInfoBakTwoParam(@Param("lot") String lot, @Param("mainSerial") String mainSerial);

    void deleteLot(String lot);

    void deleteLotTwoParam(@Param("lot") String lot, @Param("mainSerial") String mainSerial);

    List<String> deviceCodeList();

    MixInfo selectMix(String lot, String layer);

    void updateMix(String lot, String layer, int complete);
}
