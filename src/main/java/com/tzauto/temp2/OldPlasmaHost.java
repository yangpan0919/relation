package com.tzauto.temp2;

import cn.tzauto.octopus.biz.recipe.domain.Recipe;
import cn.tzauto.octopus.common.globalConfig.GlobalConstants;
import cn.tzauto.octopus.common.ws.AvaryAxisUtil;
import cn.tzauto.octopus.gui.guiUtil.UiLogUtil;
import cn.tzauto.octopus.isecsLayer.domain.EquipModel;
import cn.tzauto.octopus.isecsLayer.domain.LotInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.rpc.ServiceException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OldPlasmaHost extends EquipModel {

    private static Logger logger = Logger.getLogger(OldPlasmaHost.class);
    private boolean noSelectRecipe = false;
    public static List<String> lotList;
    public static List<String> userList;

    static {
        lotList = new ArrayList<>();
        String textPath = "D:\\EAP\\notUpload.txt";
        BufferedReader br = null;
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(textPath), "UTF-8");
            br = new BufferedReader(isr);
            String tmpString = "";
            while ((tmpString = br.readLine()) != null) {
                tmpString = tmpString.trim();
                String[] split = tmpString.split(",");
                for (String s : split) {
                    lotList.add(s.trim());
                }
            }
        } catch (Exception e) {
            logger.error("plasma 的不用上传的批号加载出错！！！：" + textPath);
            AvaryAxisUtil.main.stop();
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        userList = new ArrayList<>();
        textPath = "D:\\EAP\\user.txt";
        try {
            isr = new InputStreamReader(new FileInputStream(textPath), "UTF-8");
            br = new BufferedReader(isr);
            String tmpString = "";
            while ((tmpString = br.readLine()) != null) {
                tmpString = tmpString.trim();
                String[] split = tmpString.split(",");
                for (String s : split) {
                    userList.add(s.trim());
                }
            }
        } catch (Exception e) {
            logger.error("plasma 本地员工编号验证文件加载失败！！！：" + textPath);
            AvaryAxisUtil.main.stop();
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isNoSelectRecipe() {
        return noSelectRecipe;
    }

    public void setNoSelectRecipe(boolean noSelectRecipe) {
        this.noSelectRecipe = noSelectRecipe;
    }

    public OldPlasmaHost(String devId, String remoteIpAddress, int remoteTcpPort, String deviceType, String iconPath, String equipRecipePath) {
        super(devId, remoteIpAddress, remoteTcpPort, deviceType, iconPath, equipRecipePath);
        tableNum = "SFCZ1_ZD_Plasma";
    }

    @Override
    public String getCurrentRecipeName() {
        logger.info("执行了getCurrentRecipeName方法");
        return recipeName;
    }

    @Override
    public String startEquip() {
        logger.info("执行了startEquip方法");
        return null;
    }

    @Override
    public String pauseEquip() {
        logger.info("执行了pauseEquip方法");
        return null;
    }

    @Override
    public String stopEquip() {
        logger.info("执行了stopEquip方法");
        return null;
    }

    public Map getSpecificData(Map<String, String> dataIdMap) {
        logger.info("执行了getSpecificData，参数为：" + dataIdMap);
        Map<String, String> result;
        String lot = dataIdMap.get("lot");
        String lot2 = dataIdMap.get("lot2");
        String createEmp = dataIdMap.get("createEmp");

        int start = 80000;
        int end = 203000;
        LocalDateTime now = LocalDateTime.now();

        String classInfo = "0"; //白班
        int nowTime = Integer.parseInt(now.format(AvaryAxisUtil.dtfHHmmss));
        if (start > nowTime || end < nowTime) {
            classInfo = "1";   //夜班
        }
        String result1 = "";
        try {

            result1 = AvaryAxisUtil.tableQuery(tableNum, deviceCode, classInfo);
            if (result1 == null || result1.trim().equals("null")) {
                String result2 = AvaryAxisUtil.getOrderNum(classInfo);
                if (result2 == null) {
                    logger.error("报表数据上传中，无法获取到生產單號");
                    UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "无法获取到生產單號");
                    result = new HashMap<>();
                    result.put("error", "无法获取到生產單號");
                    return result;
                }
                result1 = result2;
                String result3 = AvaryAxisUtil.insertMasterTable(result2, "1", deviceCode, tableNum, classInfo, "001", now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), createEmp, tableNum);  // 創建工號
                if (!"".equals(result3)) {
                    logger.error("插入主表數據失败" + result3);
                    UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传中，插入主表數據失败");
                    result = new HashMap<>();
                    result.put("error", "无法获取到生產單號");
                    return result;
                }

            }
            productionMap = AvaryAxisUtil.getParmByLotNum(lot);
            if (productionMap.size() == 0) {
                logger.error("根據批號獲取料號,層別,數量等信息失敗");
                result = new HashMap<>();
                result.put("error", "根據批號獲取料號,層別,數量等信息失敗");
                return result;
            }
            productionMap.put("PaperNo", result1);
            Map map = AvaryAxisUtil.getParmByLotNumAndLayer(lot, tableNum, productionMap.get("Layer"));
            if (map.size() == 0) {
                logger.error("根據 批號,層別 帶出 料號,在製層,途程序,主途程序,制程,主配件,層別名稱,第幾次過站,工令,BOM資料 失败");
                result = new HashMap<>();
                result.put("error", "根據 批號,層別 帶出 料號,在製層,途程序,主途程序,制程,主配件,層別名稱,第幾次過站,工令,BOM資料 失败");
                return result;
            }
            productionMap.putAll(map);

            if (StringUtils.isNotEmpty(lot2)) {
                result1 = AvaryAxisUtil.tableQuery(tableNum, deviceCode, classInfo);
                if (result1 == null || result1.trim().equals("null")) {
                    String result2 = AvaryAxisUtil.getOrderNum(classInfo);
                    if (result2 == null) {
                        logger.error("报表数据上传中，无法获取到生產單號");
                        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "无法获取到生產單號");
                        result = new HashMap<>();
                        result.put("error", "无法获取到生產單號");
                        return result;
                    }
                    result1 = result2;
                    String result3 = AvaryAxisUtil.insertMasterTable(result2, "1", deviceCode, tableNum, classInfo, "001", now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), createEmp, tableNum);  // 創建工號
                    if (!"".equals(result3)) {
                        logger.error("插入主表數據失败" + result3);
                        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传中，插入主表數據失败");
                        result = new HashMap<>();
                        result.put("error", "无法获取到生產單號");
                        return result;
                    }

                }
                productionMap2 = AvaryAxisUtil.getParmByLotNum(lot2);
                if (productionMap2.size() == 0) {
                    logger.error("根據批號獲取料號,層別,數量等信息失敗");
                    result = new HashMap<>();
                    result.put("error", "根據批號獲取料號,層別,數量等信息失敗");
                    return result;
                }
                productionMap2.put("PaperNo", result1);

                map = AvaryAxisUtil.getParmByLotNumAndLayer(lot2, tableNum, productionMap2.get("Layer"));
                if (map.size() == 0) {
                    logger.error("根據 批號,層別 帶出 料號,在製層,途程序,主途程序,制程,主配件,層別名稱,第幾次過站,工令,BOM資料 失败");
                    result = new HashMap<>();
                    result.put("error", "根據 批號,層別 帶出 料號,在製層,途程序,主途程序,制程,主配件,層別名稱,第幾次過站,工令,BOM資料 失败");
                    return result;
                }
                productionMap2.putAll(map);
                materialNumber2 = productionMap2.get("PartNum");


            }
        } catch (Exception e) {
            logger.error("webservice 调用失败！！！", e);
            result = new HashMap<>();
            result.put("error", "webservice 调用失败！！！");
            return result;
        }

        return productionMap;
    }

    @Override
    public String lockEquip() {
        logger.info("执行了lockEquip方法");
        return null;
    }

    @Override
    public Map uploadRecipe(String recipeName) {
        logger.info("执行了uploadRecipe方法，参数为：" + recipeName);
        Map resultMap = new HashMap();
        return resultMap;
    }

    @Override
    public String downloadRecipe(Recipe recipe) {
        logger.info("执行了downloadRecipe方法，参数为：" + recipe);
        return "0";
    }

    @Override
    public String deleteRecipe(String recipeName) {
        logger.info("执行了deleteRecipe方法");
        //不需要删除recipe，因为正在选中的程序不能删除，所以在选择程序之后再删除没有使用的程序
        return "0";
    }

    //机台选择程序其他程序之后删除不用的程序
    @Override
    public void deleteAllRcpFromDevice(String deleteRecipeName) {
        logger.info("执行了deleteAllRcpFromDevice方法，参数为：" + deleteRecipeName);
    }

    @Override
    public String selectRecipe(String recipeName) {
        logger.info("执行了selectRecipe方法,参数为：" + recipeName);
        boolean twoLot = StringUtils.isNotEmpty(lotId2);
        recipeName = recipeName.substring(0, recipeName.length() - 4);

        String tempLotId = null;
        String tempNum = null;
        if (twoLot) {
            tempLotId = this.lotId + "/" + this.lotId2;
            String temp1;
            if (this.productNum.length() == 1) {
                temp1 = "00" + this.productNum;
            } else if (this.productNum.length() == 2) {
                temp1 = "0" + this.productNum;
            } else {
                temp1 = this.productNum;
            }
            String temp2;
            if (this.productNum.length() == 1) {
                temp2 = "00" + this.productNum2;
            } else if (this.productNum.length() == 2) {
                temp2 = "0" + this.productNum2;
            } else {
                temp2 = this.productNum2;
            }
            tempNum = temp1 + temp2;
        } else {
            tempNum = this.productNum;
            tempLotId = this.lotId;
        }
        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
            try {
                boolean flag = true;
                for (int i = 1; i < 7; i++) {
                    String value = iSecsHost.executeCommand("read " + i).get(0);
                    if (value.equals(recipeName)) {
                        iSecsHost.executeCommand("playback select" + i + ".txt");
                        iSecsHost.executeCommand("replay enter.exe");
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    return "没有发现到需要的程式-->" + recipeName;
                }
                //写入数量，批次
                iSecsHost.executeCommand("write num " + tempNum);
                iSecsHost.executeCommand("write lot " + tempLotId);


                return "程式调取错误，请重试";
            } catch (Exception e) {
                logger.error("Select recipe " + recipeName + " error:" + e.getMessage());
                return "选中失败";
            }
        }
    }

    @Override
    public Map getEquipMonitorPara() {
        logger.info("执行了getEquipMonitorPara方法");
        return null;
    }

    @Override
    public Map getEquipRecipeList() {
        logger.info("执行了getEquipRecipeList方法");
        List<String> recipeNameList = new ArrayList<>();
        Map eppd = new HashMap();
        eppd.put("eppd", recipeNameList);
        return eppd;
    }

    @Override
    public String getEquipStatus() {

        return equipStatus;
    }

    @Override
    public Object clone() {
        logger.info("执行了clone方法");
        return null;
    }

    public boolean uploadData(String macstate) throws RemoteException, ServiceException, MalformedURLException {
        if (lotList.contains(lotId)) {
            logger.info("不需要进行数据上传的批次：" + lotId);
            return true;
        }
        logger.info("开始进行数据上传：" + macstate);
        if ("0".equals(GlobalConstants.getProperty("DATA_UPLOAD"))) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();

        /**
         ID	ID
         PaperNo	表單號
         MacState	設備狀態
         StartTime	開始時間
         EndTime	完成時間
         Lotnum	批號
         Layer	層別
         MainSerial	主途程序
         Partnum	料號
         WorkNo	工令
         SfcLayer	SFC層別
         LayerName	層別名稱
         Serial	途程序
         IsMain	是否主件
         OrderId	第幾次過站
         Item1	程式名
         Qty	實際產量(PNL)
         IsOk	初件/自主檢記錄
         Item2	燒焦(PNL)
         Item4	皺褶(PNL)
         Item5	異色(PNL)
         Item6	其它(PNL)
         CreateEmpid	創建人
         CreateTime	創建時間
         ModifyEmpid	最後修改人員
         ModifyTime	最後修改時間
         */

        String item2 = "";
        String item4 = "";
        String item5 = "";
        String item6 = "";
        String PartNum = productionMap.get("PartNum");
        String WorkNo = productionMap.get("WorkNo");
        String LayerName = productionMap.get("LayerName");
        String Layer = productionMap.get("Layer");
        String Serial = productionMap.get("Serial");
        String MainSerial = productionMap.get("MainSerial");
        String OrderId = productionMap.get("OrderId");
        String IsMain = productionMap.get("主配件");
        String PaperNo = productionMap.get("PaperNo");

        PartNum = PartNum == null ? "" : PartNum;
        WorkNo = WorkNo == null ? "" : WorkNo;
        LayerName = LayerName == null ? "" : LayerName;
        Layer = Layer == null ? "" : Layer;
        Serial = Serial == null ? "" : Serial;
        MainSerial = MainSerial == null ? "" : MainSerial;
        OrderId = OrderId == null ? "" : OrderId;
        IsMain = IsMain == null ? "" : IsMain;
//        "PaperNo|MacState|StartTime|EndTime|Lotnum|Layer|MainSerial|Partnum|WorkNo|SfcLayer|LayerName|Serial|OrderId|Item1|Qty|IsOk|Item2|Item4|Item5|Item6|CreateEmpid|CreateTime"
        ;
        String recipeNameTemp;
        if(recipeName.endsWith(".xml")){
            recipeNameTemp = recipeName.substring(0,recipeName.length() - 4);
        }else{
            recipeNameTemp = recipeName;
        }
        String result = null;
        if ("1".equals(GlobalConstants.getProperty("UPDATE_DATA_ONCE"))) {
            LotInfo lotInfo = needUpload(lotId, true);
            if (lotInfo != null) {
                result = AvaryAxisUtil.insertTable(PaperNo, "正常", lotInfo.getStarttime(), now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), lotId, Layer, MainSerial,
                        PartNum, WorkNo, Layer, LayerName, Serial, IsMain, OrderId, recipeNameTemp, lotInfo.getLotnum(),
                        isFirstPro ? "1" : "0", item2, item4, item5, item6, opId
                );
                if ("".equals(result)) {
                    UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传成功，明細表數據上传成功");
                } else {
                    logger.error("报表数据上传中，明細表數據插入失败：" + result);
                }
            }
        } else {
            result = AvaryAxisUtil.insertTable(PaperNo, "正常", lotStartTime, now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), lotId, Layer, MainSerial,
                    PartNum, WorkNo, Layer, LayerName, Serial, IsMain, OrderId, recipeNameTemp, productNum,
                    isFirstPro ? "1" : "0", item2, item4, item5, item6, opId
            );
            if ("".equals(result)) {
                UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传成功，明細表數據上传成功");
            } else {
                logger.error("报表数据上传中，明細表數據插入失败：" + result);
            }
        }
        if (StringUtils.isNotEmpty(lotId2) && StringUtils.isNotEmpty(productNum2)) {
            PartNum = productionMap2.get("PartNum");
            WorkNo = productionMap2.get("WorkNo");
            LayerName = productionMap2.get("LayerName");
            Layer = productionMap2.get("Layer");
            Serial = productionMap2.get("Serial");
            MainSerial = productionMap2.get("MainSerial");
            OrderId = productionMap2.get("OrderId");
            IsMain = productionMap2.get("主配件");
            PaperNo = productionMap2.get("PaperNo");

            PartNum = PartNum == null ? "" : PartNum;
            WorkNo = WorkNo == null ? "" : WorkNo;
            LayerName = LayerName == null ? "" : LayerName;
            Layer = Layer == null ? "" : Layer;
            Serial = Serial == null ? "" : Serial;
            MainSerial = MainSerial == null ? "" : MainSerial;
            OrderId = OrderId == null ? "" : OrderId;
            IsMain = IsMain == null ? "" : IsMain;
//        "PaperNo|MacState|StartTime|EndTime|Lotnum|Layer|MainSerial|Partnum|WorkNo|SfcLayer|LayerName|Serial|OrderId|Item1|Qty|IsOk|Item2|Item4|Item5|Item6|CreateEmpid|CreateTime"
            if ("1".equals(GlobalConstants.getProperty("UPDATE_DATA_ONCE"))) {
                LotInfo lotInfo = needUpload(lotId, false);
                if (lotInfo != null) {
                    result = AvaryAxisUtil.insertTable(PaperNo, "正常", lotInfo.getStarttime(), now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), lotId2, Layer, MainSerial,
                            PartNum, WorkNo, Layer, LayerName, Serial, IsMain, OrderId, recipeNameTemp, lotInfo.getLotnum(),
                            isFirstPro ? "1" : "0", item2, item4, item5, item6, opId
                    );
                    if ("".equals(result)) {
                        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传成功，明細表數據上传成功");
                        return true;
                    }
                }
            } else {
                result = AvaryAxisUtil.insertTable(PaperNo, "正常", lotStartTime, now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), lotId2, Layer, MainSerial,
                        PartNum, WorkNo, Layer, LayerName, Serial, IsMain, OrderId, recipeNameTemp, productNum2,
                        isFirstPro ? "1" : "0", item2, item4, item5, item6, opId
                );
                if ("".equals(result)) {
                    UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传成功，明細表數據上传成功");
                    return true;
                }
            }

        }
//        String result = AvaryAxisUtil.insertTable(result1, "正常", lotStartTime, now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), lotId, map4.get("Layer"), map5.get("MainSerial"),
//                map5.get("PartNum"), map5.get("WorkNo"), map4.get("Layer"), map5.get("LayerName"), map5.get("Serial"), map5.get("OrderId"), scsl, power,
//                item3, item4, item5, item6, isFirstPro ? "1" : "0"
//        );

//        String result = AvaryAxisUtil.insertTable();
        if ("".equals(result)) {
            return true;
        }
        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传中，明細表數據插入失败：" + result);
        return false;

    }

    @Override
    public List<String> getEquipAlarm() {
        logger.info("执行了getEquipAlarm方法");
        return null;
    }

    public static void main(String[] args) {
        OldPlasmaHost oldPlasmaHost = new OldPlasmaHost(null,null,8088,"PLASMA",null,null);
        System.out.println(oldPlasmaHost);
        oldPlasmaHost.productNum = "60";
        oldPlasmaHost.productionMap = new HashMap<>();
        oldPlasmaHost.productionMap2 = new HashMap<>();
        oldPlasmaHost.productionMap.put("Qty","237");

        oldPlasmaHost.productNum2 = "69";
        oldPlasmaHost.productionMap2.put("Qty","240");
        oldPlasmaHost.needUpload("M910",false);
    }


}
