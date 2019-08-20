package com.tzauto.temp;

import cn.tzauto.octopus.biz.recipe.domain.Recipe;
import cn.tzauto.octopus.biz.recipe.domain.RecipePara;
import cn.tzauto.octopus.biz.recipe.service.RecipeService;
import cn.tzauto.octopus.common.dataAccess.base.mybatisutil.MybatisSqlSession;
import cn.tzauto.octopus.common.globalConfig.GlobalConstants;
import cn.tzauto.octopus.common.resolver.TransferUtil;
import cn.tzauto.octopus.common.resolver.hitachi.LaserDrillUtil;
import cn.tzauto.octopus.common.util.ftp.FtpUtil;
import cn.tzauto.octopus.common.ws.AvaryAxisUtil;
import cn.tzauto.octopus.gui.guiUtil.UiLogUtil;
import cn.tzauto.octopus.isecsLayer.domain.EquipModel;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlasmaHost extends EquipModel {

    private static Logger logger = Logger.getLogger(PlasmaHost.class);

    public PlasmaHost(String devId, String remoteIpAddress, int remoteTcpPort, String deviceType, String iconPath, String equipRecipePath) {
        super(devId, remoteIpAddress, remoteTcpPort, deviceType, iconPath, equipRecipePath);
        tableNum = "SFCZ1_ZD_Plasma";
    }

    @Override
    public String getCurrentRecipeName() {
        logger.info("执行了getCurrentRecipeName方法");
        return null;
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

    @Override
    public String lockEquip() {
        logger.info("执行了lockEquip方法");
        return null;
    }

    @Override
    public Map uploadRecipe(String recipeName) {
        logger.info("执行了uploadRecipe方法，参数为：" + recipeName);
        Map resultMap = new HashMap();
        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
            try {
//                String localftpip = GlobalConstants.clientInfo.getClientIp();
                String localftpip = GlobalConstants.ftpIP;
                String ftpip = GlobalConstants.ftpIP;
                String ftpPort = GlobalConstants.ftpPort;
                String ftpUser = GlobalConstants.ftpUser;
                String ftpPwd = GlobalConstants.ftpPwd;
                Recipe recipe = setRecipe(recipeName);
                SqlSession sqlSession = MybatisSqlSession.getSqlSession();
                String ftpRecipePath = new RecipeService(sqlSession).organizeUploadRecipePath(recipe);
                sqlSession.close();
                String equipRecipePathtmp = equipRecipePath;
                boolean ocrUploadOk = true;

                TransferUtil.setPPBody(recipeName, 0, GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipeName + "temp/TMP");
                if (!FtpUtil.uploadFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipeName + "temp/TMP", GlobalConstants.ftpPath + deviceCode + recipeName + "temp/", "TMP", ftpip, ftpPort, ftpUser, ftpPwd)) {

                }
                //                List<String> result = iSecsHost.executeCommand("ftp " + localftpip + " "
//                        + ftpUser + " " + ftpPwd + " " + equipRecipePathtmp + "  " + GlobalConstants.ftpPath + deviceCode + recipeName + "temp/" + " \"mput "
//                        + recipeName + "\"");
                List<String> result = sendCmdMsg2Equip("ftp " + localftpip + " "
                        + ftpUser + " " + ftpPwd + " " + equipRecipePathtmp + "  " + GlobalConstants.ftpPath + deviceCode + recipeName + "temp/" + " \"mput "
                        + recipeName + "\"");
                if (result == null) {
                    resultMap.put("uploadResult", "上传失败,上传Recipe:" + recipeName + " 超时,请检查FTP服务及网络是否正常.");
                } else {
                    for (String uploadstr : result) {
                        if ("done".equals(uploadstr)) {
                            List<RecipePara> recipeParaList = new ArrayList<>();
//                            try {
//                                if (FtpUtil.downloadFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + recipe.getRecipeName(), GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + recipe.getRecipeName(), ftpip, ftpPort, ftpUser, ftpPwd)) {
//
//                                }
//                                Map paraMap = LaserDrillUtil.transferFromFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipeName + "temp/" + recipeName);
//                                recipeParaList = LaserDrillUtil.transferFromDB(paraMap, deviceType);
//                                toolName = String.valueOf(paraMap.get("HEADER"));
//                                iSecsHost.executeCommand("ftp " + localftpip + " "
//                                        + ftpUser + " " + ftpPwd + " " + equipRecipePathtmp + "  " + GlobalConstants.ftpPath + deviceCode + recipeName + "temp/" + " \"mput "
//                                        + toolName + "\"");
//                                if (FtpUtil.downloadFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + toolName, GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + toolName, ftpip, ftpPort, ftpUser, ftpPwd)) {
//
//                                }
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
                            resultMap.put("recipe", recipe);
                            resultMap.put("deviceCode", deviceCode);
                            resultMap.put("recipeFTPPath", ftpRecipePath);
                            resultMap.put("recipeParaList", recipeParaList);
                            resultMap.put("uploadResult", "0");
                        }
                        if (uploadstr.contains("Not connected")) {
                            ocrUploadOk = false;
                        }
                    }
                }
                if (!ocrUploadOk) {
                    UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "上传Recipe:" + recipeName + " 时,FTP连接失败,请检查FTP服务是否开启.");
                    resultMap.put("uploadResult", "上传失败,上传Recipe:" + recipeName + " 时,FTP连接失败.");
                }
            } catch (Exception e) {
                logger.error("Get equip status error:" + e.getMessage());
            }
        }
        return resultMap;
    }

    @Override
    public String downloadRecipe(Recipe recipe) {
        logger.info("执行了downloadRecipe方法，参数为：" + recipe);
        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
            SqlSession sqlSession = MybatisSqlSession.getSqlSession();
            try {
//                String localftpip = GlobalConstants.clientInfo.getClientIp();
                String localftpip = GlobalConstants.ftpIP;
                String ftpip = GlobalConstants.ftpIP;
                String ftpUser = GlobalConstants.ftpUser;
                String ftpPwd = GlobalConstants.ftpPwd;
                String ftpPort = GlobalConstants.ftpPort;
                String ftpPath = new RecipeService(sqlSession).organizeRecipeDownloadFullFilePath(recipe);
                String ftpPathTmp = ftpPath.substring(0, ftpPath.lastIndexOf("/") + 1);
                if (!FtpUtil.connectFtp(ftpip, ftpPort, ftpUser, ftpPwd)) {
                    return "下载Recipe:" + recipe.getRecipeName() + "时,FTP连接失败,请检查FTP服务是否开启.";
                }
                String tool12NameTemp = "";
                if (recipe.getVersionType().equalsIgnoreCase("Engineer")) {
                    if (FtpUtil.downloadFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + recipe.getRecipeName(), ftpPathTmp + recipe.getRecipeName() + "_V" + recipe.getVersionNo(), ftpip, ftpPort, ftpUser, ftpPwd)) {
//                        Map paraMap = LaserDrillUtil.transferFromFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + recipe.getRecipeName());
//
//                        tool12NameTemp = String.valueOf(paraMap.get("HEADER"));
                        FtpUtil.downloadFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + tool12NameTemp, ftpPathTmp + tool12NameTemp + "_V" + recipe.getVersionNo(), ftpip, ftpPort, ftpUser, ftpPwd);
                    }

                } else {

                    if (FtpUtil.downloadFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + recipe.getRecipeName(), ftpPathTmp + recipe.getRecipeName(), ftpip, ftpPort, ftpUser, ftpPwd)) {
                        Map paraMap = LaserDrillUtil.transferFromFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + recipe.getRecipeName());

                        tool12NameTemp = String.valueOf(paraMap.get("HEADER"));
                        FtpUtil.downloadFile(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + tool12NameTemp, ftpPathTmp + tool12NameTemp, ftpip, ftpPort, ftpUser, ftpPwd);

                    }

                }

                List<String> result = sendCmdMsg2Equip("ftp " + localftpip + " "
                        + ftpUser + " " + ftpPwd + " " + equipRecipePath + " " + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + " \"mget " + recipe.getRecipeName() + "\"");
//                result = sendCmdMsg2Equip("ftp " + localftpip + " "
//                        + ftpUser + " " + ftpPwd + " " + equipRecipePath + " " + GlobalConstants.ftpPath + deviceCode + recipe.getRecipeName() + "temp/" + " \"mget " + tool12NameTemp + "\"");

                if (result == null) {
                    return "下载Recipe:" + recipe.getRecipeName() + "超时,请检查FTP服务及网络是否正常.";
                }
                for (String str : result) {
                    if ("done".equals(str)) {
                        return "0";
                    }
                    if (str.contains("Not connected")) {
                        return "下载Recipe:" + recipe.getRecipeName() + "时,FTP连接失败,请检查FTP服务是否开启.";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Download recipe " + recipe.getRecipeName() + " error:" + e.getMessage());
                return "下载失败,出现异常:" + e.getMessage();
            } finally {
                sqlSession.close();
                this.deleteTempFile(recipe.getRecipeName());
            }
        }
        return "Download recipe " + recipe.getRecipeName() + " failed";
    }

    @Override
    public String deleteRecipe(String recipeName) {
        logger.info("执行了deleteRecipe方法");
        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
            try {
                List<String> result = iSecsHost.executeCommand("dos \"del /q " + equipRecipePath + "\\*.xml*\"");
                for (String str : result) {
                    if ("done".equals(str)) {
                        return "0";
                    }
                }
                return "删除失败";
            } catch (Exception e) {
                logger.error("Delete recipe " + recipeName + " error:" + e.getMessage());
                return "删除失败";
            }
        }
    }

    @Override
    public String selectRecipe(String recipeName) {
        logger.info("执行了selectRecipe方法");
        //todo
        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
            try {
                List<String> result = iSecsHost.executeCommand("playback selrecipe.txt");
                Thread.sleep(900);
                iSecsHost.executeCommand("write lot " + this.lotId);
//                iSecsHost.executeCommand("dialog \"Lot No\" write " + lotId);
                iSecsHost.executeCommand("replay enter.exe");
                Thread.sleep(1800);
                iSecsHost.executeCommand("playback clearpartno.txt");
                iSecsHost.executeCommand("dialog \"OPEN FILE\" write " + equipRecipePath + "\\" + recipeName);
                result = iSecsHost.executeCommand("dialog \"OPEN FILE\" action \"&Open\"");
                for (String str : result) {
                    if ("done".equals(str)) {
                        ppExecName = recipeName;
                        return "0";
                    }
                    if (str.contains("rror")) {
                        return "选中失败";
                    }
                }
                return "选中失败";
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
        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
            List<String> result = new ArrayList<>();
            try {
                result = iSecsHost.executeCommand("dos \"dir " + equipRecipePath + " /a/w\"");
            } catch (Exception e) {
                return eppd;
            }
            if (result != null && result.size() > 1) {
                for (String str : result) {
                    if (!str.contains(".xml")) {
                        continue;
                    }
                    if (str.contains("xml")) {
                        String[] recipeNameTemps = str.split("\\s");
                        for (String temp : recipeNameTemps) {
                            if (temp.contains(".xml")) {
                                recipeNameList.add(temp.trim());
                            }
                        }
                    }
                }
            }
        }
        eppd.put("eppd", recipeNameList);
        return eppd;
    }

    @Override
    public String getEquipStatus() {
        logger.info("执行了getEquipStatus方法");
        String preEquipStatusTemp = equipStatus;
        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
            try {

//                List<String> hrunColors = this.iSecsHost.executeCommand("readrectcolor 1160 846 1170 848");
//                for (String startColorTemp : hrunColors) {
//                    if (startColorTemp.equals("0x99ff00")) {
//                        equipStatus = "HRUN";
//                    }
//                }
//
//                List<String> pchkColors = this.iSecsHost.executeCommand("readrectcolor 1160 878 1170 880");
//                for (String startColorTemp : pchkColors) {
//                    if (startColorTemp.equals("0x99ff00")) {
//                        equipStatus = "PCHK";
//                        this.pmState.setPM(true);
//                    }
//                }
//
//                List<String> gchkColors = this.iSecsHost.executeCommand("readrectcolor 1160 983 1170 985");
//                for (String startColorTemp : gchkColors) {
//                    if (startColorTemp.equals("0x99ff00")) {
//                        equipStatus = "GCHK";
//                    }
//                }
            } catch (Exception e) {
                logger.error("Get equip status error:" + e.getMessage());
            }
        }
        Map map = new HashMap();
        map.put("EquipStatus", equipStatus);
        if (!equipStatus.equals(preEquipStatusTemp)) {
            preEquipStatus = preEquipStatusTemp;
            changeEquipPanel(map);
        }
//        return equipStatus;
        return null;
    }

    @Override
    public Object clone() {
        logger.info("执行了clone方法");
        return null;
    }

    public boolean uploadData(String macstate) throws RemoteException, ServiceException, MalformedURLException {
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
        String result1 = getMainTableName();
        if (result1.equals("1")) {
            return false;
        }
        Map<String, String> productionMap = AvaryAxisUtil.getProductionMap(lotId, tableNum, deviceCode);
        String item3 = "";
        String item4 = "";
        String item5 = "";
        String item6 = "";
        String scsl = "";
        String power = "";
        String result = AvaryAxisUtil.insertTable(result1, "正常", lotStartTime, now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), lotId, productionMap.get("Layer"), productionMap.get("MainSerial"),
                productionMap.get("PartNum"), productionMap.get("WorkNo"), productionMap.get("Layer"), productionMap.get("LayerName"), productionMap.get("Serial"), productionMap.get("OrderId"), scsl, power,
                item3, item4, item5, item6, isFirstPro ? "1" : "0"
        );
//        String result = AvaryAxisUtil.insertTable(result1, "正常", lotStartTime, now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), lotId, map4.get("Layer"), map5.get("MainSerial"),
//                map5.get("PartNum"), map5.get("WorkNo"), map4.get("Layer"), map5.get("LayerName"), map5.get("Serial"), map5.get("OrderId"), scsl, power,
//                item3, item4, item5, item6, isFirstPro ? "1" : "0"
//        );

//        String result = AvaryAxisUtil.insertTable();
        if ("".equals(result)) {
            UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传成功，明細表數據上传成功");
            return true;
        }
        logger.error("报表数据上传中，明細表數據插入失败：" + result);
        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传中，明細表數據插入失败：" + result);
        return false;

    }

    @Override
    public List<String> getEquipAlarm() {
        logger.info("执行了getEquipAlarm方法");
        return null;
    }
}
