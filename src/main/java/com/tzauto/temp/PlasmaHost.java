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
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
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

public class PlasmaHost extends EquipModel {

    private static Logger logger = Logger.getLogger(PlasmaHost.class);
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

    public PlasmaHost(String devId, String remoteIpAddress, int remoteTcpPort, String deviceType, String iconPath, String equipRecipePath) {
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
        //不需要删除recipe，因为正在选中的程序不能删除，所以在选择程序之后再删除没有使用的程序
        return "0";
//        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
//            try {
//                List<String> result = iSecsHost.executeCommand("dos \"del /q " + equipRecipePath + "\\*.xml*\"");
//                for (String str : result) {
//                    if ("done".equals(str)) {
//                        return "0";
//                    }
//                }
//                return "删除失败";
//            } catch (Exception e) {
//                logger.error("Delete recipe " + recipeName + " error:" + e.getMessage());
//                return "删除失败";
//            }
//        }
    }

    //机台选择程序其他程序之后删除不用的程序
    @Override
    public void deleteAllRcpFromDevice(String deleteRecipeName) {
        logger.info("执行了deleteAllRcpFromDevice方法，参数为：" + deleteRecipeName);
        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
            try {
                iSecsHost.executeCommand("dos \"del /q " + equipRecipePath + "\\" + deleteRecipeName + "\"");
            } catch (Exception e) {
                logger.error("Delete recipe " + recipeName + " error:" + e.getMessage());
            }
        }
    }

    @Override
    public String selectRecipe(String recipeName) {
        logger.info("执行了selectRecipe方法,参数为：" + recipeName);
        synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
            try {
                boolean num = StringUtils.isNotEmpty(productNum);
                boolean twoLot = StringUtils.isNotEmpty(lotId2);

                String tempMaterialNumber = null;
                String tempLotId = null;
                String tempNum = null;
                if (twoLot) {
                    tempMaterialNumber = this.materialNumber + "/" + this.materialNumber2;
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
                } else if (num) {
                    tempMaterialNumber = this.materialNumber;
                    tempLotId = this.lotId;
                    tempNum = this.productNum;
                } else {
                    tempMaterialNumber = this.materialNumber;
                    tempLotId = this.lotId;
                }

                iSecsHost.executeCommand("playback materialNumber.txt");
                Thread.sleep(500);

                iSecsHost.executeCommand("write mnum " + tempMaterialNumber);

//                Thread.sleep(500);
                iSecsHost.executeCommand("playback ok.txt");
                Thread.sleep(500);
                iSecsHost.executeCommand("playback lot.txt");
                Thread.sleep(500);

                iSecsHost.executeCommand("write mnum " + tempLotId);

//                Thread.sleep(500);
                iSecsHost.executeCommand("playback ok.txt");
                if (num) {
                    Thread.sleep(500);
                    iSecsHost.executeCommand("playback num.txt");
                    Thread.sleep(500);

                    iSecsHost.executeCommand("write mnum " + tempNum);

//                Thread.sleep(500);
                    iSecsHost.executeCommand("playback ok.txt");
                }

                if (noSelectRecipe) {
                    logger.info("程序已经是那个了，所以不用换了");
                    return "0";
                }
                Thread.sleep(500);

                iSecsHost.executeCommand("playback recipe1.txt");//调程式第一步
                Thread.sleep(500);
                iSecsHost.executeCommand("playback recipe2.txt");//调程式第二步
                Thread.sleep(500);
//                iSecsHost.executeCommand("playback recipe3.txt");//调程式第三步
                iSecsHost.executeCommand("dialog \"Recipe Select\" listbox " + recipeName.substring(0, recipeName.length() - 4));
                //0x3399ff//蓝色  533 127 535 129
                //0x3399ff//蓝色  533 165 535 167
                List<String> pchkColors = this.iSecsHost.executeCommand("readrectcolor 533 127 535 129");
                for (String startColorTemp : pchkColors) {
                    if (startColorTemp.equals("0x3399ff")) {
                        iSecsHost.executeCommand("playback select1.txt");
//                        Thread.sleep(500);
                        iSecsHost.executeCommand("playback recipe4.txt");//调程式第四步
                        Thread.sleep(500);
                        iSecsHost.executeCommand("playback back.txt");//调程式第五步
                        return "0";
                    }
                }

                pchkColors = this.iSecsHost.executeCommand("readrectcolor 533 165 535 167");
                for (String startColorTemp : pchkColors) {
                    if (startColorTemp.equals("0x3399ff")) {
                        iSecsHost.executeCommand("playback select2.txt");
//                        Thread.sleep(500);
                        iSecsHost.executeCommand("playback recipe4.txt");//调程式第四步
                        Thread.sleep(500);
                        iSecsHost.executeCommand("playback back.txt");//调程式第五步
                        return "0";
                    }
                }

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

                boolean flag = true;

                List<String> hrunColors = this.iSecsHost.executeCommand("readrectcolor 21 503 23 505");
//                21,503 ,23,505      0x7fff00 绿色     0xff0000 红色
//                21,447,23,449
                for (String startColorTemp : hrunColors) {
                    if (startColorTemp.equals("0x7fff00")) {
                        equipStatus = "Idle";
                        flag = false;
                        break;
                    }
                }
//
                List<String> pchkColors = this.iSecsHost.executeCommand("readrectcolor 21 447 23 449");
                for (String startColorTemp : pchkColors) {
                    if (startColorTemp.equals("0x7fff00")) {
                        equipStatus = "Run";
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    logger.error("机台不是开始状态，也不是停止转态");
                    return null;
                }
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

        String result = AvaryAxisUtil.insertTable(PaperNo, "正常", lotStartTime, now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), lotId, Layer, MainSerial,
                PartNum, WorkNo, Layer, LayerName, Serial, IsMain, OrderId, recipeName, productNum,
                isFirstPro ? "1" : "0", item2, item4, item5, item6, opId
        );
        if ("".equals(result)) {
            UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传成功，明細表數據上传成功");
        }else{
            logger.error("报表数据上传中，明細表數據插入失败：" + result);
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

            result = AvaryAxisUtil.insertTable(PaperNo, "正常", lotStartTime, now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), lotId, Layer, MainSerial,
                    PartNum, WorkNo, Layer, LayerName, Serial, IsMain, OrderId, recipeName, productNum,
                    isFirstPro ? "1" : "0", item2, item4, item5, item6, opId
            );
            if ("".equals(result)) {
                UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传成功，明細表數據上传成功");
                return true;
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
}
