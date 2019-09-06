package com.tzauto.temp;


import cn.tzauto.octopus.biz.device.domain.DeviceInfo;
import cn.tzauto.octopus.biz.device.domain.DeviceInfoExt;
import cn.tzauto.octopus.biz.device.service.DeviceService;
import cn.tzauto.octopus.biz.material.Material;
import cn.tzauto.octopus.biz.recipe.domain.Recipe;
import cn.tzauto.octopus.biz.recipe.service.RecipeService;
import cn.tzauto.octopus.biz.tooling.Tooling;
import cn.tzauto.octopus.common.dataAccess.base.mybatisutil.MybatisSqlSession;
import cn.tzauto.octopus.common.globalConfig.GlobalConstants;
import cn.tzauto.octopus.common.util.tool.FileUtil;
import cn.tzauto.octopus.common.util.tool.JsonMapper;
import cn.tzauto.octopus.common.ws.AvaryAxisUtil;
import cn.tzauto.octopus.gui.guiUtil.UiLogUtil;
import cn.tzauto.octopus.isecsLayer.domain.EquipModel;
import cn.tzauto.octopus.isecsLayer.domain.ISecsHost;
import cn.tzauto.octopus.isecsLayer.equipImpl.plasma.PlasmaHost;
import cn.tzauto.octopus.secsLayer.util.NormalConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import javax.xml.rpc.ServiceException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DownloadToolHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(DownloadToolHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String message = new String(req, "UTF-8");
        logger.info("DownloadTool message =====> " + message);
        LinkedHashMap downloadMessageMap = (LinkedHashMap) JsonMapper.fromJsonString(message, Map.class);
        String command = String.valueOf(downloadMessageMap.get("command"));
        String deviceCode = String.valueOf(downloadMessageMap.get("machineno"));
        MDC.put(NormalConstant.WHICH_EQUIPHOST_CONTEXT, deviceCode);
        EquipModel equipModel = GlobalConstants.stage.equipModels.get(deviceCode);
        if (command.equals("download")) {
            SqlSession sqlSession = MybatisSqlSession.getBatchSqlSession();
            RecipeService recipeService = new RecipeService(sqlSession);
            DeviceService deviceService = new DeviceService(sqlSession);

            String downloadresult = "";
            String userId = String.valueOf(downloadMessageMap.get("userid"));
            String partNo = String.valueOf(downloadMessageMap.get("partno"));
            String lotNo = String.valueOf(downloadMessageMap.get("lotno"));
            String lottype = String.valueOf(downloadMessageMap.get("lottype"));
            if (lottype.length() > 1) {
                lottype = lottype.substring(0, 1);
            }
            String fixtureno = String.valueOf(downloadMessageMap.get("fixtureno"));
            String materialno = String.valueOf(downloadMessageMap.get("materialno"));
            String faceno = String.valueOf(downloadMessageMap.get("faceno"));
            if (faceno.length() > 1) {
                faceno = faceno.substring(0, 1);
            }
            String lotNo2 = String.valueOf(downloadMessageMap.get("lotno2"));
            String fixtureno2 = String.valueOf(downloadMessageMap.get("fixtureno2"));
            String materialno2 = String.valueOf(downloadMessageMap.get("materialno2"));

            String checkerId = String.valueOf(downloadMessageMap.get("checkerid"));
            String recipeName = "";
            String materialNumber = "";
            String materialNumber2 = "";
            List<DeviceInfo> deviceInfos = deviceService.getDeviceInfoByDeviceCode(deviceCode);

            if (deviceInfos != null && !deviceInfos.isEmpty()) {
                DeviceInfo deviceInfo = deviceInfos.get(0);

                if (deviceInfo.getDeviceType().contains("PLASMA")) {//Plasma特殊处理
                    boolean twoLot = StringUtils.isNotEmpty(lotNo2);//是否是两个批次，同时生产
                    if (!PlasmaHost.userList.contains(userId)) {
                        logger.warn("上岗证验证不通过：" + userId);
                        new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("上岗证验证失败") + "Work permit not been grant");
                        sqlSession.close();
                        return;
                    }
                    String equipStatus = equipModel.getEquipStatus();
                    if ("Run".equals(equipStatus)) {
                        new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("设备正在运行，不能扫码") + "equip is running,please waiting!");
                        sqlSession.close();
                        return;
                    }
                    Recipe recipe;
                    DeviceInfoExt deviceInfoExt = null;
                    List<Recipe> recipes = null;

                    try {
                        if (PlasmaHost.lotList.contains(lotNo)) {
                            materialNumber = lotNo;
                            fixtureno = lotNo; //所有不用上传的批次，料号，批次，序号一致
                        } else {
                            Map<String, String> parmMap = new HashMap<>();
                            parmMap.put("lot", lotNo);
                            parmMap.put("createEmp", userId);
                            if (twoLot) {
                                parmMap.put("lot2", lotNo2);
                            }
                            Map<String, String> parmByLotNum = equipModel.getSpecificData(parmMap);
                            if (parmByLotNum.get("error") != null) {
                                new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF(parmByLotNum.get("error")) + " webservice message error!!!");
                                sqlSession.close();
                                return;
                            }
                            materialNumber = parmByLotNum.get("PartNum");
                            if (twoLot) {
                                materialNumber2 = equipModel.productionMap2.get("PartNum");
                                equipModel.lotId2 = lotNo2;
                                equipModel.materialNumber2 = materialNumber2;
                            }

                        }
                    } catch (Exception e) {
                        logger.error("批号获取料号接口异常", e);
                        new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("无法获取料号") + "can not get materialNumber");
                        sqlSession.close();
                        return;
                    }
                    if ((!equipModel.opId.equals("")) && (!equipModel.lotStartTime.equals(""))) {
                        logger.warn("扫码之后点了开始，但是没有进行uploadData,数据为：" + AvaryAxisUtil.createParm(equipModel.opId, equipModel.lotId, equipModel.productNum, equipModel.lotStartTime, equipModel.materialNumber, equipModel.recipeName) + ",在这里进行补偿上传数据。");
                        try {
                            equipModel.uploadData(null);
                        } catch (Exception e) {
                            logger.error("上传数据失败", e);
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
                        }

                    }


                    equipModel.lotId = lotNo;
                    equipModel.materialNumber = materialNumber;

                    recipeName = recipeService.queryRecipeName(materialNumber, fixtureno);
                    logger.info(lotNo + "," + materialNumber + "," + fixtureno + ",获得到的recipeName为：" + recipeName);

                    if(StringUtils.isNotEmpty(materialno)){
                        equipModel.productNum = materialno;
                    }
                    if (twoLot) {
                        String recipeName2 = recipeService.queryRecipeName(materialNumber2, fixtureno2);
                        equipModel.productNum2 = materialno2;
                        logger.info(lotNo2 + "," + materialNumber2 + "," + fixtureno2 + ",获得到的recipeName为：" + recipeName2);
                        if (StringUtils.isEmpty(recipeName) || StringUtils.isEmpty(recipeName2)) {
                            new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("无法查询到程序名") + "can not find recipeName");
                            sqlSession.close();
                            return;
                        }
                        if (!recipeName.equals(recipeName2)) {
                            new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("两个批次所对应的批次名不一致！") + "find two recipeName");
                            sqlSession.close();
                            return;
                        }
                    } else if (StringUtils.isEmpty(recipeName)) {
                        new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("无法查询到程序名") + "can not find recipeName");
                        sqlSession.close();
                        return;
                    }
                    if(deviceInfo.getDeviceType().equals("OLDPLASMA")){
                        equipModel.selectRecipe(recipeName);

                        LocalDateTime now = LocalDateTime.now();
                        equipModel.lotStartTime = now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss);//批次开始时间

                        equipModel.opId = userId;
                        equipModel.recipeName = recipeName;
                        return;
                    }

                    deviceInfoExt = deviceService.getDeviceInfoExtByDeviceCode(deviceCode);
                    recipes = recipeService.searchRecipeOrderByVerNo(recipeName, deviceCode, "Unique");
                    if (recipes == null || recipes.isEmpty()) {
                        recipes = recipeService.searchRecipeOrderByVerNo(recipeName, deviceCode, "GOLD");
                        if (recipes == null || recipes.isEmpty()) {
                            recipes = recipeService.searchRecipeOrderByVerNo(recipeName, deviceCode, "Engineer");
                            if (recipes == null || recipes.isEmpty()) {
                                recipes = recipeService.searchRecipeOrderByVerNo(recipeName, null, "Engineer");
                            }
                        }
                    }
                    if (recipes != null && !recipes.isEmpty()) {
                        recipe = recipes.get(0);
                        downloadresult = recipeService.downLoadRcp2ISECSDeviceByTypeAutomatic(deviceInfo, recipe, deviceInfoExt.getRecipeDownloadMod());
                        if (downloadresult.contains("下载Recipe失败,设备通讯异常,请稍后重试")) {
                            downloadresult = toUTF("通訊異常檢查網絡設置") + "Connect error,please check it and try later.";
                        } else if (downloadresult.equals("2")) {
                            downloadresult = toUTF("本地recipe文件有多个，请删除多余的recipe") + "Find multi recipes! Please delete the recipe which is no using";
                        }
                    } else {
                        downloadresult = toUTF("程序未上傳") + "!Can not find any recipe,please upload recipe" + recipeName;
                    }
                    logger.info("downloadresult:" + downloadresult);
                    if ("0".equals(downloadresult)) {

                        equipModel.opId = userId;
//                        recipeName = recipeName.substring(0, recipeName.length() - 4);
                        equipModel.recipeName = recipeName;
                        logger.info("下载程序成功，批次为：" + lotNo + "|" + lotNo2 + ",料号为：" + materialNumber + "|" + materialNumber2 + "，程式名为：" + recipeName + "，操作员为：" + userId);

                    }

                    new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(downloadresult);
                    sqlSession.close();
                    return;
                }

                if (!"0".equals(AvaryAxisUtil.workLicense(deviceInfo.getDeviceName(), userId))) {
                    UiLogUtil.getInstance().appendLog2SeverTab(deviceCode, "上岗证验证失败!!");
                    new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("上崗証驗證失敗") + "Work permit not been grant");
                    return;
                }
                equipModel.opId = userId;
                lotNo = equipModel.trimLot(lotNo);
                //串联sfc系统，确认产品在当站
                if ("1".equals(GlobalConstants.getProperty("SFC_CHECK")) && !checkSFC(lotNo, equipModel.tableNum)) {
                    UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "串联SFC系统失败，确认产品是否在当站!!批号：" + lotNo);
                    new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("批次不在本站") + "SFC Check failed!");
                    return;
                }
                //验证原材料
                String mstr = AvaryAxisUtil.getMaterialInfo(deviceInfo.getDeviceType(), lotNo);
                if (mstr.contains("|")) {
                    String[] mstrs = mstr.split("\\|");
                    Material material = new Material();
                    material.setCode(mstrs[0]);
                    material.setId(mstrs[0]);
                    String realMname = mstrs[1];
                    if (realMname.contains(" ")) {
                        realMname = realMname.substring(realMname.indexOf(" ") + 1);
                    }
                    if (realMname.contains("-")) {
                        realMname = realMname.substring(realMname.indexOf("-") + 1);
                    }
                    material.setName(realMname);
                    equipModel.materials.add(material);
                    if ((GlobalConstants.getProperty("MATERIAL_CHECK").equals("1") && !materialno.equals(realMname))) {
                        new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("材料驗證失敗") + "Material check error!");
                        return;
                    }
                }
                //验证治具
                if (AvaryAxisUtil.checkTooling(deviceInfo.getDeviceType(), lotNo, fixtureno)) {
                    Tooling tooling = new Tooling();
                    tooling.setId(fixtureno);
                    tooling.setCode(fixtureno);
                    equipModel.toolings.add(tooling);
                } else {
                    new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("治具驗證失敗") + "Tooling check error!");
                    return;
                }

                if (!lotNo2.equals("")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //串联sfc系统，确认产品在当站
                    if ("1".equals(GlobalConstants.getProperty("SFC_CHECK")) && !checkSFC(lotNo2, equipModel.tableNum)) {
                        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "串联SFC系统失败，确认产品是否在当站!!批号：" + lotNo2);
                        new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("批次不在本站") + "SFC Check failed!");
                        return;
                    }
                    //批次2材料验证
                    String mstr2 = AvaryAxisUtil.getMaterialInfo(deviceInfo.getDeviceType(), lotNo2);
                    if (mstr2.contains("|")) {
                        String[] mstrs = mstr2.split("\\|");

                        Material material = new Material();
                        material.setCode(mstrs[0]);
                        material.setId(mstrs[0]);
                        String realMname = mstrs[1];
                        if (mstrs[1].contains("-")) {
                            realMname = mstrs[1].substring(mstrs[1].indexOf("-") + 1);
                        }
                        material.setName(realMname);
                        equipModel.materials.add(material);
                        if ((GlobalConstants.getProperty("MATERIAL_CHECK").equals("1") && !materialno2.equals(realMname))) {
                            new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("材料驗證失敗") + "Material check error!");
                            return;
                        }
                    }
                    //批次2治具验证
                    if (AvaryAxisUtil.checkTooling(deviceInfo.getDeviceType(), lotNo2, fixtureno2)) {
                        Tooling tooling = new Tooling();
                        tooling.setId(fixtureno2);
                        tooling.setCode(fixtureno2);
                        equipModel.toolings.add(tooling);
                    } else {
                        new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("治具驗證失敗") + "!Tooling check error!");
                        return;
                    }
                }
                if (lottype.equals("0")) {
                    List<String> checkers = FileUtil.getFileBodyAsStrList(GlobalConstants.getProperty("CHECKER_ID_FILE_PATH"));
                    boolean checkerOk = false;
                    if (checkers != null) {
                        for (String checker : checkers) {
                            if (checker.equals(checkerId)) {
                                checkerOk = true;
                            }
                        }
                    }
                    if (!checkerOk) {
                        new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("審核人驗證失敗") + "!FirstCheckEmpid check error!");
                        return;
                    }
                }
                String partNoTemp = AvaryAxisUtil.getPartNumVersion(lotNo);
//                if (deviceInfo.getDeviceType().contains("SCREEN")) {
                try {
//                    EquipModel equipModel = equipModel;
                    equipModel.lotCount = AvaryAxisUtil.getLotQty(lotNo);
                    equipModel.lotId = lotNo;
                    equipModel.isFirstPro = "0".equals(lottype);
                    equipModel.equipState.setWorkLot(lotNo);
                    if ("1".equals(GlobalConstants.getProperty("FIRST_PRODUCTION_NEED_CHECK")) && AvaryAxisUtil.isInitialPart(partNoTemp, deviceCode, "0")) {
                        if ("1".equals(lottype)) {
                            UiLogUtil.getInstance().appendLog2SeverTab(deviceCode, "需要开初件!!");
                            new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("初件檢查失敗") + "!Need check isfirst!");
                            return;
                        }
                    }
                    if ("1".equals(GlobalConstants.getProperty("FIRST_PRODUCTION_CHECK")) && !AvaryAxisUtil.firstProductionIsOK(deviceInfo.getDeviceName(), lotNo, partNoTemp, "SFCZ4_ZD_DIExposure")) {
                        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "初件检查未通过!!");
                        new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg(toUTF("初件檢查失敗"));
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Exception", e);
                    e.printStackTrace();
                }

                if (deviceInfo.getDeviceType().contains("HITACHI-LASERDRILL")) {

                    recipeName = equipModel.organizeRecipe(faceno, lotNo);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!lotNo2.equals("")) {
                        String recipeName2 = equipModel.organizeRecipe(faceno, lotNo2);
                        if (!recipeName.equals(recipeName2)) {
                            logger.error("两个批号关联的程序名不一致！");
                            UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "LOT1:" + lotNo + "-->" + recipeName + "LOT2:" + lotNo2 + "-->" + recipeName2);
                            new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode).sendSocketMsg("Two lot with different partno!");
                            return;
                        }
                    }
                } else {
                    recipeName = equipModel.organizeRecipe(partNoTemp, lotNo);
                }
                if (recipeName.contains("Can not")) {
                    downloadresult = recipeName;
                } else {
                    Recipe recipe = new Recipe();
                    DeviceInfoExt deviceInfoExt = deviceService.getDeviceInfoExtByDeviceCode(deviceCode);
                    if ("1".equals(GlobalConstants.getProperty("DOWNLOAD_RCP_FROM_CIM"))) {
                        downloadresult = AvaryAxisUtil.downLoadRecipeFormCIM(deviceCode, recipeName);
                        if (downloadresult.contains("PASS")) {
                            return;
                        }
                    } else {
                        if (GlobalConstants.getProperty("EQUIP_NO_RECIPE").contains(deviceInfo.getDeviceType())) {
                            recipe.setRecipeName(recipeName);
                            recipe.setId(recipeName);
                        } else {
                            List<Recipe> recipes = recipeService.searchRecipeOrderByVerNo(recipeName, deviceCode, "Unique");
                            if (recipes == null || recipes.isEmpty()) {
                                recipes = recipeService.searchRecipeOrderByVerNo(recipeName, deviceCode, "GOLD");
                                if (recipes == null || recipes.isEmpty()) {
                                    recipes = recipeService.searchRecipeOrderByVerNo(recipeName, deviceCode, "Engineer");
                                    if (recipes == null || recipes.isEmpty()) {
                                        recipes = recipeService.searchRecipeOrderByVerNo(recipeName, null, "Engineer");
                                    }
                                }
                            }
                            if (recipes != null && !recipes.isEmpty()) {
                                recipe = recipes.get(0);
                                downloadresult = recipeService.downLoadRcp2ISECSDeviceByTypeAutomatic(deviceInfo, recipe, deviceInfoExt.getRecipeDownloadMod());
                                if (downloadresult.contains("下载Recipe失败,设备通讯异常,请稍后重试")) {
                                    downloadresult = toUTF("通訊異常檢查網絡設置") + "Connect error,please check it and try later.";
                                }
                            } else {
                                downloadresult = toUTF("程序未上傳") + "!Can not find any recipe,please upload recipe" + recipeName;
                            }
                        }
                    }
                    logger.info("downloadresult:" + downloadresult);
                    if ("0".equals(downloadresult)) {
                        equipModel.partNo = partNoTemp;
                        equipModel.lotId = lotNo;
                        equipModel.equipState.setWorkLot(lotNo);
                        equipModel.lotCount = AvaryAxisUtil.getLotQty(lotNo);
                        deviceInfoExt.setLotId(lotNo);
                        if (!lotNo2.equals("")) {
                            deviceInfoExt.setLotId(lotNo + "/" + lotNo2);
                            equipModel.lotId = lotNo + "/" + lotNo2;
                        }
                        deviceInfoExt.setPartNo(partNo);
                        deviceInfoExt.setRecipeName(recipeName);
                        deviceInfoExt.setRecipeId(recipe.getId());
                        deviceService.modifyDeviceInfoExt(deviceInfoExt);
                        sqlSession.commit();
                    }
                }
            } else {
                downloadresult = "Can not find any device by MachineNo " + deviceCode;
            }

            sqlSession.close();
            if (downloadresult.contains("请联系该工段的")) {
                downloadresult = toUTF("程式未審核") + "The recipe:" + recipeName + " was not approved";
            }
//            Channel channel = ctx.channel();
//            AttributeKey attrKey = AttributeKey.valueOf("123456789");
//            Attribute<Object> attr = channel.attr(attrKey);
//            String eqpIp = ctx.channel().remoteAddress().toString().split(":")[0].replaceAll("/", "");
//            attr.set(downloadresult);
//            buf = channel.alloc().buffer(downloadresult.getBytes().length);
//            buf.writeBytes(downloadresult.getBytes());
//            channel.writeAndFlush(buf);
            ISecsHost iSecsHost = new ISecsHost(equipModel.remoteIPAddress, GlobalConstants.getProperty("DOWNLOAD_TOOL_RETURN_PORT"), "", deviceCode);
            iSecsHost.sendSocketMsg(downloadresult);


        }
        if (command.equals("startmiantain")) {
            String time = String.valueOf(downloadMessageMap.get("time"));
            time = time.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");

            equipModel.pmState.setPM(true);
            equipModel.pmState.setStartTime(time);
            UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "开始保养.");
        }
        if (command.equals("endmiantain")) {
            String time = String.valueOf(downloadMessageMap.get("time"));
            time = time.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
            equipModel.pmState.setPM(false);
            equipModel.pmState.setEndTime(time);
            UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "结束保养.");
            try {
                equipModel.uploadData("保養");
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private String toUTF(String s) {
        try {
            return new String(s.getBytes(), "utf-8");
        } catch (Exception e) {
            try {
                return new String(s.getBytes("gbk"), "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                return s;
            }
        }

    }

    private boolean checkSFC(String lotId, String tableNum) {
        Map<String, String> map4 = new HashMap<>();
        Map<String, String> map5 = new HashMap<>();
        try {
            if (lotId.contains("/")) {
                lotId = lotId.split("/")[0];
            }
            map4 = AvaryAxisUtil.getParmByLotNum(lotId);
            if (map4.size() == 0) {
                logger.error("根據批號獲取料號,層別,數量等信息失敗");
                return false;
            }
            map5 = AvaryAxisUtil.getParmByLotNumAndLayer(lotId, tableNum, map4.get("Layer"));
            if (map5.size() == 0) {
                logger.error("根據 批號,層別 帶出 料號,在製層,途程序,主途程序,制程,主配件,層別名稱,第幾次過站,工令,BOM資料 失败");
                //報錯:獲取途程信息失敗
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
