
package com.tzauto.temp;

import cn.tzauto.octopus.biz.alarm.domain.AlarmRecord;
import cn.tzauto.octopus.biz.device.domain.DeviceInfo;
import cn.tzauto.octopus.common.globalConfig.GlobalConstants;
import cn.tzauto.octopus.common.util.tool.JsonMapper;
import cn.tzauto.octopus.secsLayer.util.NormalConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class EquipAlarmHandler extends ChannelInboundHandlerAdapter {

    //    private static final Logger logger = Logger.getLogger(EquipAlarmHandler.class);
    private static final Logger logger = Logger.getLogger("EquipAlarmHandler");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String message = new String(req, "UTF-8");

        //get device code
//        Map<String, String> map = new HashMap();
//        for (Map.Entry<String, EquipModel> equipmodelEntry : GlobalConstants.stage.equipModels.entrySet()) {
//            EquipModel equipModel = equipmodelEntry.getValue();
//            ConcurrentLinkedQueue<ISecsHost> isecsHosts = equipModel.iSecsHostList;
//            for (ISecsHost isecsHost : isecsHosts) {
//                map.put(isecsHost.ip, isecsHost.deviceCode);
//            }
//        }

        String eqpIp = ctx.channel().remoteAddress().toString().split(":")[0].replaceAll("/", "");
        String deviceCode = "";//map.get(eqpIp);
        for (DeviceInfo deviceInfo : GlobalConstants.deviceInfos) {
            if (deviceInfo.getDeviceIp().equals(eqpIp)) {
                deviceCode = deviceInfo.getDeviceCode();
            }
        }

        if (deviceCode == null || deviceCode.equals("")) {
            MDC.put(NormalConstant.WHICH_EQUIPHOST_CONTEXT, "error");
            logger.info("alarm message ip=====> " + eqpIp + " ===devicecode===>" + deviceCode + " ====>" + msg);
            return;
        }
        MDC.put(NormalConstant.WHICH_EQUIPHOST_CONTEXT, deviceCode);
        logger.info("alarm message ip=====> " + eqpIp + "===devicecode===>" + deviceCode);

        DeviceInfo deviceInfo = GlobalConstants.stage.hostManager.getDeviceInfo(null, deviceCode);

        logger.info("alarm message =====> " + message);

        if(deviceInfo.getDeviceType().equals("OLDPLASMA")){
            EquipModel equipModel = GlobalConstants.stage.equipModels.get(deviceCode);
            if(StringUtils.isNotEmpty(equipModel.opId)){
                try {
                    equipModel.uploadData(null);
                } catch (Exception e) {
                    logger.error("日志解析后上传数据报错：",e);
                } finally {

                    //一批结束之后进行数据重置
                    equipModel.opId = "";
                    equipModel.lotStartTime = "";

                    equipModel.productNum = "";
                    equipModel.productNum2 = "";

                    equipModel.lotId = "";
                    equipModel.lotId2 = "";
                    equipModel.materialNumber = "";
                    equipModel.materialNumber2 = "";
                    equipModel.recipeName = "";

                    Map statusmap = new HashMap();
                    statusmap.put("EquipStatus", "Idle");
                    equipModel.changeEquipPanel(statusmap);
                }
            }
            return;
        }


        List<String> alarmStringList = new ArrayList<>();
        alarmStringList.add(message);

        List<AlarmRecord> alarmRecordList = setAlarmRecord(deviceInfo, alarmStringList);

        if (!GlobalConstants.isLocalMode && alarmRecordList != null && alarmRecordList.size() != 0) {
            //实时发送alarm记录至服务端
            Map alarmRecordMap = new HashMap();
            alarmRecordMap.put("msgName", "ArAlarmRecord");
            alarmRecordMap.put("deviceCode", deviceCode);
            alarmRecordMap.put("alarmRecord", JsonMapper.toJsonString(alarmRecordList));
            alarmRecordMap.put("alarmDate", GlobalConstants.dateFormat.format(new Date()));
            GlobalConstants.C2SAlarmQueue.sendMessage(alarmRecordMap);
            logger.info("Send alarmRecords to server..." + alarmRecordMap.toString());
        }
    }

    private List<AlarmRecord> setAlarmRecord(DeviceInfo deviceInfo, List<String> alarmStringList) {
        List<AlarmRecord> alarmRecords = new ArrayList<>();
        if (alarmStringList != null && alarmStringList.size() > 0) {
            for (String str : alarmStringList) {
                AlarmRecord alarmRecord = GlobalConstants.stage.equipModels.get(deviceInfo.getDeviceCode()).delAlarmMsg(str);
                if (alarmRecord != null)
                    alarmRecords.add(alarmRecord);
            }
        }
        return alarmRecords;
    }


    private String toUTF(String s) {
        try {
            return new String(s.getBytes("utf-8"), "utf-8");
        } catch (Exception e) {
            try {
                return new String(s.getBytes("gbk"), "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                return s;
            }
        }

    }
}
