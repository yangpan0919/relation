package com.tzauto.temp2;

import cn.tzauto.octopus.biz.alarm.domain.AlarmRecord;
import cn.tzauto.octopus.biz.device.domain.DeviceInfo;
import cn.tzauto.octopus.biz.device.domain.DeviceInfoExt;
import cn.tzauto.octopus.biz.device.domain.DeviceOplog;
import cn.tzauto.octopus.biz.device.domain.UnitFormula;
import cn.tzauto.octopus.biz.material.Material;
import cn.tzauto.octopus.biz.pm.PMState;
import cn.tzauto.octopus.biz.recipe.domain.Attach;
import cn.tzauto.octopus.biz.recipe.domain.Recipe;
import cn.tzauto.octopus.biz.recipe.domain.RecipePara;
import cn.tzauto.octopus.biz.recipe.domain.RecipeTemplate;
import cn.tzauto.octopus.biz.recipe.service.RecipeService;
import cn.tzauto.octopus.biz.tooling.LaserCrystal;
import cn.tzauto.octopus.biz.tooling.Tooling;
import cn.tzauto.octopus.common.dataAccess.base.mybatisutil.MybatisSqlSession;
import cn.tzauto.octopus.common.globalConfig.GlobalConstants;
import cn.tzauto.octopus.common.util.tool.BigDecimalArithmetic;
import cn.tzauto.octopus.common.util.tool.FileUtil;
import cn.tzauto.octopus.common.ws.AvaryAxisUtil;
import cn.tzauto.octopus.common.ws.AxisUtility;
import cn.tzauto.octopus.gui.guiUtil.UiLogUtil;
import cn.tzauto.octopus.gui.main.EapClient;
import cn.tzauto.octopus.gui.widget.equipstatuspane.EquipStatusPane;
import cn.tzauto.octopus.secsLayer.domain.EquipNodeBean;
import cn.tzauto.octopus.secsLayer.domain.EquipPanel;
import cn.tzauto.octopus.secsLayer.domain.EquipState;
import cn.tzauto.octopus.secsLayer.util.NormalConstant;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public abstract class EquipModel extends Thread {
    private static Logger logger = Logger.getLogger(EquipModel.class.getName());
    public static final int COMMUNICATING = 1;
    public static final int NOT_COMMUNICATING = 0;
    protected int commState = NOT_COMMUNICATING;
    public String controlState = NormalConstant.CONTROL_REMOTE_ONLINE;
    private int alarmState = 0;
    protected String protocolType;
    protected String deviceId;
    public EquipState equipState;
    public String deviceType;//设备类型
    protected String manufacturer;//生产厂商
    public String deviceCode;//设备代码
    protected String ProcessProgramID;
    protected int recipeType;
    protected String iconPath;
    protected String recipePath = "";
    public String ppExecName = "--";
    public String equipStatus = "--";
    public String preEquipStatus = "";
    public String lotId = "--";
    public String materialNumber = "";  //料号
    public String productNum2 = "";//第二个数量
    public String lotId2 = "";//第二个批次
    public String materialNumber2 = "";//第二个料号
    public boolean flag = true;//第一次扫码
    public Map<String, String> productionMap;//存放第一个批次需要上传的信息
    public Map<String, String> productionMap2;//存放第二个需要上传的信息
    public String recipeName = "";  //程序名
    public String productNum; //每批生产数量
    protected long LastComDate = 0;//最后一次通信时间
    public boolean holdSuccessFlag = true;
    protected boolean holdFlag = false;
    public ISecsHost iSecsHost;
    protected boolean startUp;
    public String remoteIPAddress;
    protected int remoteTCPPort;
    protected boolean isInitState;
    protected String equipRecipePath;
    private Map<String, String> passport = new HashMap<>();
    private Map<String, String> candidates = new HashMap<>();
    public ConcurrentLinkedQueue<ISecsHost> iSecsHostList = new ConcurrentLinkedQueue<ISecsHost>();
    public String preAlarm = "";
    public int notGetAlarmCount = 0;
    public String partNo = "";
    public String deviceName = "";
    public String lotCount = "";
    //是否初件
    public boolean isFirstPro = false;
    public boolean firstLot = true; // 第一批次，标志位    手动重传数据后标志为重置
    public boolean isEngineerMode = false;
    public boolean isLocalMode = false;
    public String tableNum = "";
    public String lotStartTime = ""; //批次开始时间
    public String idleStartTime = ""; //过程状态开始时间
    public List<Material> materials = new ArrayList<>();
    public List<Tooling> toolings = new ArrayList<>();
    public PMState pmState;
    public LaserCrystal laserCrystalPower;
    public LaserCrystal laserCrystalAccuracy;
    public String partRecipe;
    public int produceNum = 0;
    public String opId = "";
    public String checkerId = "";


    public EquipModel(String devId, String remoteIpAddress, int remoteTcpPort, String deviceType, String iconPath, String equipRecipePath) {
        this.deviceId = devId;
        this.remoteIPAddress = remoteIpAddress;
        this.remoteTCPPort = remoteTcpPort;
        equipState = new EquipState();
        this.deviceType = deviceType;
        this.deviceCode = devId;
        this.iconPath = iconPath;
        isInitState = false;
        equipState.setControlState(NormalConstant.CONTROL_REMOTE_ONLINE);
        this.equipRecipePath = equipRecipePath;
        passport.put("Status", "idle");
        passport.put("UserLevel", "99");
        candidates.put("WaitTime", "");
        candidates.put("UserLevel", "99");
        candidates.put("Waiter", "");
        pmState = new PMState("system");
        laserCrystalPower = new LaserCrystal();
        laserCrystalAccuracy = new LaserCrystal();
        lotStartTime = LocalDateTime.now().format(AvaryAxisUtil.dtfyyyyMMddHHmmss);
        tableNum = AvaryAxisUtil.getTableName(deviceType);
    }

    public abstract String getCurrentRecipeName();

    public abstract String startEquip();

    public abstract String pauseEquip();

    public abstract String stopEquip();

    public abstract String lockEquip();

    public abstract Map uploadRecipe(String recipeName);

    public abstract String downloadRecipe(Recipe recipe);

    public abstract String deleteRecipe(String recipeName);

    public abstract String selectRecipe(String recipeName);

    public abstract Map getEquipMonitorPara();

    public abstract Map getEquipRecipeList();

    public abstract String getEquipStatus();

    @Override
    public abstract Object clone();

    public abstract List<String> getEquipAlarm();

    @Override
    public void run() {
//        while (!this.isInterrupted()) {
        if (iSecsHost != null && iSecsHost.isConnect && commState == NOT_COMMUNICATING) {
            if (testOcrConnect()) {
                Map map = new HashMap();
                map.put("ControlState", controlState);
                changeEquipPanel(map);
                getEquipRealTimeState();
                iSecsHostList.remove(iSecsHost);
                iSecsHostList.add(iSecsHost);
            }
        }
//        }
    }

    public void initialize() {
        iSecsHost = null;
        iSecsHost = initComm(remoteIPAddress, remoteTCPPort, deviceType, deviceCode);
        iSecsHostList.clear();
//        if (iSecsHost.isConnect) {
//            this.equipState.setCommOn(true);
//            commState = 1;
////            new Thread(new Runnable() {
////                @Override
////                public void run() {
////                    getEquipRealTimeState();
////                }
////            }).start();
//        } else {
        this.equipState.setCommOn(false);
        commState = 0;
//        }

    }

    public void initialize(ISecsHost iSecsHosttmp) {
        iSecsHost = null;
        iSecsHost = initComm(iSecsHosttmp.ip, remoteTCPPort, iSecsHosttmp.deviceTypeCode, iSecsHosttmp.deviceCode);
//        if (iSecsHost.isConnect) {
//            this.equipState.setCommOn(true);
//            commState = 1;
//        } else {
//            this.equipState.setCommOn(false);
//            commState = 0;
//        }
    }

    public EquipState getEquipState() {
        return this.equipState;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public boolean isStartUp() {
        return startUp;
    }

    public void setStartUp(boolean startUp) {
        this.startUp = startUp;
    }

    public boolean refreshInitState() {
        return true;
    }

    /*
    这里直接调用getEquipMonitorPara方法，如果子类有其他用处则重写getSpecificData方法
    主要用于点检取值
     */
    public Map getSpecificData(Map<String, String> dataIdMap) {
        //this.getEquipMonitorPara();
        return this.getEquipMonitorPara();
    }

    public void changeEquipPanel(Map resultMap) {
        ArrayList<EquipNodeBean> equipBeans = EapClient.equipBeans;
        while (GlobalConstants.stage == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
//                java.util.logging.Logger.getLogger(EquipModel.class.getName()).log(Level.SEVERE, null, ex);
                logger.warn(ex);
            }
        }
        for (EquipNodeBean equipNodeBean : equipBeans) {
            if (equipNodeBean.getDeviceCode().equals(deviceCode)) {


//                EquipStatusPane equipStatusPane = EapClient.getThePane(deviceCode);
                EquipStatusPane equipStatusPane = EapClient.equipStatusPanes.get(deviceCode);
                EquipPanel oldPanel = (EquipPanel) equipNodeBean.getEquipPanelProperty();
                EquipPanel newPanel = (EquipPanel) equipNodeBean.getEquipPanelProperty().clone();
                if (resultMap.get("PPExecName") != null) {
                    newPanel.setRunningRcp(resultMap.get("PPExecName").toString());
                }
                if (resultMap.get("EquipStatus") != null) {
                    newPanel.setRunState(resultMap.get("EquipStatus").toString());
                }
                if (resultMap.get("AlarmState") != null) {
                    if (oldPanel.getAlarmState() == 2 && resultMap.get("AlarmState").equals(1)) { //报警变红就不能变黄了，除非已消警
                        newPanel.setAlarmState(oldPanel.getAlarmState());
                    } else {
                        newPanel.setAlarmState(Integer.parseInt(resultMap.get("AlarmState").toString()));
                    }
                }
                if (resultMap.get("ControlState") != null) {
                    newPanel.setControlState(resultMap.get("ControlState").toString());
                }
                if (resultMap.get("WorkLot") != null) {
                    newPanel.setWorkLot(resultMap.get("WorkLot").toString());
                }
                if (resultMap.get("NetState") != null) {
                    newPanel.setNetState(Integer.parseInt(resultMap.get("NetState").toString()));
                }
                if (resultMap.get("CommState") != null) {
                    commState = Integer.parseInt(resultMap.get("CommState").toString());
                    newPanel.setNetState(commState);
                    EquipState equipState = equipNodeBean.getEquipStateProperty();
                    if (commState == 1) {
                        equipState.setCommOn(true);
                    } else {
                        equipState.setCommOn(false);
                    }
                    equipNodeBean.setEquipStateProperty(equipState);
                }
                equipNodeBean.setEquipPanelProperty(newPanel);

                if ("".equals(newPanel.getRunningRcp())) {
                    equipStatusPane.setRunningRcp("--");
                } else {
                    equipStatusPane.setRunningRcp(newPanel.getRunningRcp());
                }

                break;
            }
        }
    }

    protected Recipe setRecipe(String recipeName) {
        Recipe recipe = new Recipe();
        SqlSession sqlSession = MybatisSqlSession.getSqlSession();
        DeviceService deviceService = new DeviceService(sqlSession);
        RecipeService recipeService = new RecipeService(sqlSession);
        List<DeviceInfo> deviceInfotmp = deviceService.getDeviceInfoByDeviceCode(deviceCode);
        DeviceInfo deviceInfo = deviceInfotmp.get(0);
//        DeviceType deviceType = deviceService.getDeviceTypeMap().get(deviceInfo.getDeviceTypeId());
        recipe.setClientId(GlobalConstants.getProperty("clientId"));
        if (GlobalConstants.sysUser != null && GlobalConstants.sysUser.getId() != null) {
            recipe.setCreateBy(GlobalConstants.sysUser.getId());
            recipe.setUpdateBy(GlobalConstants.sysUser.getId());
        } else {
            recipe.setCreateBy("System");
            recipe.setUpdateBy("System");
        }
        recipe.setDeviceCode(deviceInfo.getDeviceCode());
        recipe.setDeviceId(deviceInfo.getId());
        recipe.setDeviceName(deviceInfo.getDeviceName());
        recipe.setDeviceTypeCode(deviceInfo.getDeviceType());
        recipe.setDeviceTypeId(deviceInfo.getDeviceTypeId());
        recipe.setDeviceTypeName(deviceInfo.getDeviceType());
        recipe.setProdCode("");//LGA
        recipe.setProdId("");//LGA001-12138
        recipe.setProdName("");//LGA_0.48*0.68
        recipe.setRecipeCode("");
        recipe.setRecipeDesc("");
        recipe.setRecipeName(recipeName);
        recipe.setRecipeStatus("Create");
        recipe.setRecipeType("N");
        recipe.setSrcDeviceId(GlobalConstants.getProperty("clientId"));
        //如果已存在，版本号加1     
        List<Recipe> recipes = recipeService.searchRecipeByPara(recipeName, deviceCode, "Engineer", null);
        Recipe recipeTmp = null;
        if (recipes != null && recipes.size() > 0) {
            recipeTmp = recipes.get(0);
        }
        if (recipeTmp != null) {
            recipe.setVersionNo(recipeTmp.getVersionNo() + 1);
            recipe.setTotalCnt(recipeTmp.getTotalCnt() + 1);
            recipe.setUpdateCnt(recipeTmp.getUpdateCnt() + 1);
        } else {
            recipe.setVersionNo(0);
            recipe.setTotalCnt(0);
            recipe.setUpdateCnt(0);
        }
        recipe.setVersionType("Engineer");
        recipe.setCreateDate(new Date());
        sqlSession.close();
        return recipe;
    }

    protected void clear() {
        iSecsHost = null;
        this.deviceCode = null;
        this.deviceId = null;
        this.deviceType = null;
        this.remoteIPAddress = null;
        this.protocolType = null;
        commState = NOT_COMMUNICATING;
        controlState = NormalConstant.CONTROL_OFFLINE;

    }

    public String getMTBA() {
        return "";
    }

    public List<RecipePara> getRecipeParasFromMonitorMap() {
        Map monitorParaMap = getEquipMonitorPara();
        SqlSession sqlSession = MybatisSqlSession.getSqlSession();
        RecipeService recipeService = new RecipeService(sqlSession);
        List<RecipeTemplate> recipeTemplates = recipeService.searchRecipeTemplateMonitor(deviceCode);
        DeviceService deviceService = new DeviceService(sqlSession);
        Map<String, Map<String, UnitFormula>> unitFormulaMap = deviceService.getAllUnitFormula();
        sqlSession.close();
        List<RecipePara> recipeParas = new ArrayList<>();
        for (RecipeTemplate recipeTemplate : recipeTemplates) {
            RecipePara recipePara = new RecipePara();
            recipePara.setParaName(recipeTemplate.getParaName());

            recipePara.setParaCode(recipeTemplate.getParaCode());
            String[] paraNameAtOCRs = new String[1];
            if (recipeTemplate.getParaDesc().contains(",")) {
                paraNameAtOCRs = recipeTemplate.getParaDesc().split(",");
            } else {
                paraNameAtOCRs[0] = recipeTemplate.getParaDesc();
            }
            String setValue = "";
            if (paraNameAtOCRs.length > 1) {
                for (int i = 0; i < paraNameAtOCRs.length; i++) {
                    if (monitorParaMap.get(paraNameAtOCRs[i]) != null) {
                        setValue = String.valueOf(monitorParaMap.get(paraNameAtOCRs[i]));
                    }
                }
            } else {
                setValue = String.valueOf(monitorParaMap.get(recipeTemplate.getParaDesc()));
            }
            if (setValue == null || setValue.equals("null") || "".equals(setValue.trim())) {
                continue;
            }
            if (recipeTemplate.getDeviceVariableUnit() != null && !"".equals(recipeTemplate.getDeviceVariableUnit().trim())) {
                String formula = "";
                try {
                    formula = unitFormulaMap.get(recipeTemplate.getDeviceVariableUnit()).get(recipeTemplate.getParaUnit()).getFormulaDesc();
                } catch (Exception E) {
                    formula = "";
                    logger.error("未设置：" + recipeTemplate.getDeviceVariableUnit() + "的单位转换关系.");
                    recipePara.setSetValue(setValue);
                }
                if (!"".equals(formula)) {
                    String setValueT = paraFormula(setValue, formula);
                    recipePara.setSetValue(setValueT);
                    logger.info("参数名:" + recipePara.getParaName() + " 从设备上获取到的值:" + setValue + " 转化后的值:" + setValueT);
                }
            } else {
                recipePara.setSetValue(setValue);
            }
            recipeParas.add(recipePara);
        }
        return recipeParas;
    }

    private String paraFormula(String para, String formula) {
        double paraDouble = Double.parseDouble(para);
        String result = "";
        if (formula.contains("*")) {
            String[] formulas = formula.split("\\*");
            result = String.valueOf(BigDecimalArithmetic.mul(paraDouble, Double.parseDouble(formulas[1])));
        }
        if (formula.contains("/")) {
            String[] formulas = formula.split("/");
            result = String.valueOf(BigDecimalArithmetic.div(paraDouble, Double.parseDouble(formulas[1])));
        }
        if (formula.contains("+")) {
            String[] formulas = formula.split("\\+");
            result = String.valueOf(BigDecimalArithmetic.add(paraDouble, Double.parseDouble(formulas[1])));
        }
        if (formula.contains("-")) {
            String[] formulas = formula.split("-");
            result = String.valueOf(BigDecimalArithmetic.sub(paraDouble, Double.parseDouble(formulas[1])));
        }
        if (result.contains("E")) {
            BigDecimal bd = new BigDecimal(result);
            result = bd.toPlainString();
        }
        String flag = result.substring(result.length() - 2, result.length());
        if (".0".equals(flag)) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    public DeviceOplog setDeviceOplog(String formerDeviceStatus, String lotId) {
        DeviceOplog deviceOplog = new DeviceOplog();
        deviceOplog.setId(UUID.randomUUID().toString());
        deviceOplog.setDeviceCode(deviceCode);
        deviceOplog.setCurrRecipeName(ppExecName);
        deviceOplog.setDeviceCeid(String.valueOf("0"));
        deviceOplog.setCurrLotId(lotId);
        deviceOplog.setOpTime(new Date());
        deviceOplog.setOpDesc("机台状态从" + formerDeviceStatus + "切换为" + equipStatus);
        deviceOplog.setOpType("eqpt.EqptStatusChange");
        deviceOplog.setFormerDeviceStatus(formerDeviceStatus);
        deviceOplog.setCurrDeviceStatus(equipStatus);
        deviceOplog.setCreateBy("System");
        deviceOplog.setCreateDate(new Date());
        deviceOplog.setUpdateBy("System");
        deviceOplog.setUpdateDate(new Date());
        deviceOplog.setDelFlag("0");
        deviceOplog.setVerNo(0);
        return deviceOplog;
    }

    public boolean startCheck() {
        getCurrentRecipeName();
        if (!specialCheck()) {
            return false;
        }
        boolean pass = true;
        String checkRecultDesc = "";
        String checkRecultDescEng = "";
        if ("1".equals(GlobalConstants.getProperty("START_CHECK_LOCKFLAG"))) {
            if (this.checkLockFlagFromServerByWS(deviceCode)) {
                checkRecultDesc = "检测到设备被Server要求锁机,设备将被锁!";
                UiLogUtil.getInstance().appendLog2SeverTab(deviceCode, "检测到设备被Server要求锁机,设备将被锁!");
                pass = false;
            }
        }

        SqlSession sqlSession = MybatisSqlSession.getSqlSession();
        RecipeService recipeService = new RecipeService(sqlSession);
        DeviceService deviceService = new DeviceService(sqlSession);
        DeviceInfoExt deviceInfoExt = deviceService.getDeviceInfoExtByDeviceCode(deviceCode);
        if (deviceInfoExt == null) {
            logger.error("数据库中确少该设备模型配置；DEVICE_CODE:" + deviceCode);
            UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "工控上不存在设备:" + deviceCode + "模型信息，不允许开机！请联系ME处理！");
            checkRecultDesc = "工控上不存在设备:" + deviceCode + "模型信息，不允许开机！请联系ME处理！";
            pass = false;
        } else {
            String trackInRcpName = deviceInfoExt.getRecipeName();
            if (!ppExecName.equals(trackInRcpName)) {
                UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "已选程序与领料程序不一致，设备被锁定！请联系ME处理！领料程序：" + trackInRcpName + " 已选程序 " + ppExecName);
                pass = false;
                checkRecultDesc = "已选程序与领料程序不一致,设备被锁定！请联系ME处理！领料程序:" + trackInRcpName + " 已选程序:" + ppExecName;
                checkRecultDescEng = "The current recipe <" + ppExecName + "> in equipment is different from CIM system <" + trackInRcpName + ">,equipment will be locked.";
            }
        }
        if ("1".equals(GlobalConstants.getProperty("START_CHECK_RECIPE_VERSION"))) {
            Recipe execRecipe = recipeService.getExecRecipe(ppExecName, deviceCode);
            if (execRecipe == null) {
                UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "工控上不存在: " + ppExecName + " 的Unique或Gold版本,将无法执行开机检查.请联系PE处理！");
                checkRecultDesc = "工控上不存在: " + ppExecName + " 的Unique或Gold版本,将无法执行开机检查.请联系PE处理!";
                checkRecultDescEng = " There's no GOLD or Unique version of current recipe <" + ppExecName + "> , equipment will be locked.";
                pass = false;
            }
        }

        Map mqMap = new HashMap();
        mqMap.put("msgName", "eqpt.StartCheckWI");
        mqMap.put("deviceCode", deviceCode);
        mqMap.put("recipeName", ppExecName);
        mqMap.put("EquipStatus", equipStatus);
        mqMap.put("lotId", lotId);
        if (pass) {
            if ("1".equals(GlobalConstants.getProperty("START_CHECK_RECIPE_PARA"))) {
                //MonitorService monitorService = new MonitorService(sqlSession);
                // List<RecipePara> equipRecipeParas = getRecipeParasFromMonitorMap();
                List<RecipePara> equipRecipeParas = getRecipeParasFromMonitorMap();
                List<RecipePara> recipeParasdiff = checkRcpPara(deviceInfoExt.getRecipeId(), deviceCode, equipRecipeParas, "");
                try {
                    String eventDesc = "";
                    String eventDescEng = "";
                    if (recipeParasdiff != null && recipeParasdiff.size() > 0) {
                        this.stopEquip();
                        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "开机参数检查未通过!");
                        for (RecipePara recipePara : recipeParasdiff) {
                            eventDesc = "开机Check参数异常参数编码为:" + recipePara.getParaCode() + ",参数名:" + recipePara.getParaName() + "其异常设定值为:" + recipePara.getSetValue()
                                    + ",默认值为：" + (recipePara.getDefValue() == null ? "未设置" : recipePara.getDefValue())
                                    + "其最小设定值为：" + (recipePara.getMinValue() == null ? "未设置" : recipePara.getMinValue())
                                    + ",其最大设定值为：" + (recipePara.getMaxValue() == null ? "未设置" : recipePara.getMaxValue());
                            String eventDescEngtmp = " Para_Code:" + recipePara.getParaCode() + ",Para_name:" + recipePara.getParaName() + ",Set_value:" + recipePara.getSetValue() + ",MIN_value:" + recipePara.getMinValue() + ",MAX_value:" + recipePara.getMaxValue() + "/r/n";
                            UiLogUtil.getInstance().appendLog2EventTab(deviceCode, eventDesc);
                            checkRecultDesc = checkRecultDesc + eventDesc;
                            eventDescEng = eventDescEng + eventDescEngtmp;
                        }
                        //monitorService.saveStartCheckErroPara2DeviceRealtimePara(recipeParasdiff, deviceCode);//保存开机check异常参数
                        sendMessage2Eqp("Recipe parameter error,start check failed!The equipment has been stopped! Error parameter:/r/n" + eventDescEng);
                        pass = false;
                    } else if (recipeParasdiff == null) {
                        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "开机参数检查不通过！");
                        eventDesc = "设备：" + deviceCode + " 开机Check参数异常";
                        logger.info("设备：" + deviceCode + " 开机Check失败");
                        pass = false;
                        checkRecultDesc = eventDesc;
                    } else {
                        UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "开机参数检查通过！");
                        eventDesc = "设备：" + deviceCode + " 开机Check参数没有异常";
                        logger.info("设备：" + deviceCode + " 开机Check成功");
                        pass = true;
                        checkRecultDesc = eventDesc;
                    }
                    sqlSession.commit();
                } catch (Exception e) {
                    logger.error("Exception:", e);
                } finally {
                    sqlSession.close();
                }
            }

        } else {
            if (!"".equalsIgnoreCase(checkRecultDescEng)) {
                sendMessage2Eqp(checkRecultDescEng);
            }
            UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "开机检查条件不具备,检查未通过!");
        }
        mqMap.put("eventDesc", checkRecultDesc);
//        GlobalConstants.C2SLogQueue.sendMessage(mqMap);
        return pass;
    }

    public boolean checkLockFlagFromServerByWS(String deviceCode) {
        boolean lockFlag = false;
        if (!GlobalConstants.isLocalMode) {
            Map checkServerLockResult = AxisUtility.getLockFlagAndRemarks("SysAuto", deviceCode);
            String lockFlagStr = String.valueOf(checkServerLockResult.get("lockFlag"));
            if ("Y".equals(lockFlagStr)) {
                lockFlag = true;
                String lockReason = String.valueOf(checkServerLockResult.get("remarks"));
                UiLogUtil.getInstance().appendLog2SeverTab(deviceCode, "检测到设备被Server设置为锁机,锁机原因为: " + lockReason);
                sendMessage2Eqp("The equipment holded by server,hold reason:" + lockReason);
                //holdDeviceAndShowDetailInfo("Equipment locked because of " + lockReason);
                SqlSession sqlSession = MybatisSqlSession.getSqlSession();
                DeviceService deviceService = new DeviceService(sqlSession);
                DeviceInfoExt deviceInfoExt = deviceService.getDeviceInfoExtByDeviceCode(deviceCode);
                deviceInfoExt.setLockFlag("Y");
                deviceInfoExt.setRemarks(lockReason);
                deviceService.modifyDeviceInfoExt(deviceInfoExt);
                sqlSession.commit();
                sqlSession.close();
            }
        }
        return lockFlag;
    }

    public boolean getPassport(int level) {
        if (!this.isConnect()) {
            logger.debug(deviceCode + "连接异常.强制回收");
            returnPassport();
            return false;
        }
        boolean get = false;
        String currentUserLevel = passport.get("UserLevel");
//        String passPortStatus = passport.get("Status");
        logger.debug(deviceCode + "当前passport状态===>" + passport.get("Status"));
        if (level >= Integer.valueOf(currentUserLevel)) {
            if (candidates.size() > 4) {
                //异常情况导致此candidates加入了多个等待者，在此重置
                candidates.put("WaitTime", "");
                candidates.put("UserLevel", "99");
                candidates.put("Waiter", "");
            }
            if (candidates.get("WaitTime") != null && !"".equals(candidates.get("WaitTime"))) {
                String waitTimetmp = candidates.get("WaitTime");
                if (Long.parseLong(waitTimetmp) > 20000) {
                    //这里超过20秒肯定是触发异常了，强制回收
                    logger.debug(deviceCode + "前一流程占用超时.强制回收");
                    candidates.remove("WaitTime");
                    candidates.put("WaitTime", null);
                    returnPassport();
                    return true;
                }
            }
            logger.debug(deviceCode + " 高或同优先级用户在使用通道,排队请求被驳回 在用者:" + currentUserLevel + "请求者:" + level);
            get = false;
        } else {
            if ("inUse".equals(passport.get("Status"))) {
                if (!"".equals(candidates.get("Waiter"))) {
                    String currentWaitUserLevel = candidates.get("UserLevel");
                    if (level >= Integer.valueOf(currentWaitUserLevel)) {
                        logger.debug(deviceCode + " 正在排队的等级优先与请求者,请求被驳回,排队者:" + currentWaitUserLevel + "请求者:" + level);
                        return false;
                    } else {
                        logger.debug(deviceCode + " 请求者等级优先于正在排队者,排队者被替换,排队者:" + currentWaitUserLevel + "请求者:" + level);
                        candidates.put("UserLevel", String.valueOf(level));
                        candidates.put("Waiter", UUID.randomUUID().toString());
                    }
                } else {
                    logger.debug(deviceCode + " 排队位置为空,直接进入排队");
                    candidates.put("UserLevel", String.valueOf(level));
                    candidates.put("Waiter", UUID.randomUUID().toString());
                }

                logger.debug(deviceCode + " 在用:" + passport.get("UserLevel") + "  " + candidates.get("UserLevel") + " 进入等待");
                String waiter = candidates.get("Waiter");
                String waiterLevel = candidates.get("UserLevel");
                logger.debug(deviceCode + "当前排队者:" + waiter + " 等级:" + waiterLevel);
                for (int i = 0; i < 100; i++) {
                    logger.debug(deviceCode + "第" + i + "次获取Passport");
                    try {
                        if (!waiter.equals(candidates.get("Waiter"))) {
                            logger.debug(deviceCode + " 排队者 " + waiter + "等级" + waiterLevel + "排队时被高等级者插队.插队者:" + candidates.get("Waiter") + "等级" + candidates.get("UserLevel"));
                            return false;
                        }
                        Thread.sleep(500);
                        candidates.put("WaitTime" + waiter, String.valueOf(500 * i));
                        candidates.put("WaitTime", String.valueOf(500 * i));
                        logger.debug(this.deviceCode + " try  getpassport currentUserLevel:" + passport.get("UserLevel") + " The user in line level:" + level + "wait time:" + String.valueOf(500 * i));
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                    String waitTime = candidates.get("WaitTime" + waiter);
                    if (level == 1) {
                        if (Long.parseLong(waitTime) > 5000) {
                            logger.debug("强制回收passport,保证level:" + level + "使用");
                            candidates.remove("WaitTime" + waiter);
                            candidates.put("WaitTime", null);
                            this.returnPassport();
                            get = true;
                            break;
                        }
                    }
                    //等待超时

                    if (Long.parseLong(waitTime) > 20000) {
                        logger.debug(deviceCode + "getpassport 等待超时.");
                        candidates.remove("WaitTime" + waiter);
                        candidates.put("WaitTime", null);
                        get = false;
                        break;
                    }
                    if ("idle".equals(passport.get("Status"))) {
                        candidates.remove("WaitTime" + waiter);
                        candidates.put("WaitTime", null);
                        passport.put("UserLevel", String.valueOf(level));
                        passport.put("Status", "inUse");
                        get = true;
                        break;
                    }
                }

            } else {
                passport.put("UserLevel", String.valueOf(level));
                passport.put("Status", "inUse");
                get = true;
            }
        }

        return get;
    }

    public void returnPassport() {
//        String currentUserLevel = passport.get("UserLevel");
        if ("inUse".equals(passport.get("Status"))) {
            passport.put("UserLevel", "99");
            passport.put("Status", "idle");
            logger.debug(this.deviceCode + "   returnPassport currentUserLevel" + passport.get("UserLevel") + "  passPortStatus " + passport.get("Status"));
        }

    }

    protected boolean backMainScreen() {
        String result = "";
        try {
            result = sendMsg2Equip("playback gotoworkscreen.txt").get(0);
            if ("done".equals(result)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean backWorkScreen() {
        String result = "";
        try {
            result = sendMsg2Equip("playback gotoworkscreen.txt").get(0);
            if ("done".equals(result)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public List<Attach> getRecipeAttachInfo(Recipe recipe) {
        SqlSession sqlSession = MybatisSqlSession.getSqlSession();
        String recipeName = recipe.getRecipeName();
        String ftpRecipePath = new RecipeService(sqlSession).organizeUploadRecipePath(recipe);
        List<Attach> attachs = new ArrayList<>();
        Attach attach = new Attach();
        attach.setId(UUID.randomUUID().toString());
        attach.setRecipeRowId(recipe.getId());
        attach.setAttachPath(ftpRecipePath);
        attach.setAttachName(recipeName + "_V" + recipe.getVersionNo());
        attach.setAttachType("");
        attach.setSortNo(0);
        if (GlobalConstants.sysUser != null) {
            attach.setCreateBy(GlobalConstants.sysUser.getId());
            attach.setUpdateBy(GlobalConstants.sysUser.getId());
        } else {
            attach.setCreateBy("System");
            attach.setUpdateBy("System");
        }
        attachs.add(attach);
        sqlSession.close();
        return attachs;
    }

    public List<String> sendMsg2Equip(String command) {
        final String executCommand = command;
        List<String> result = null;
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Callable<List<String>> call = new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                //开始执行耗时操作  
                return iSecsHost.executeCommand(executCommand);
            }
        };

        Future<List<String>> future = exec.submit(call);
        try {
            result = future.get(GlobalConstants.msgWaitTime, TimeUnit.MILLISECONDS);
            logger.debug("Execute future task...");
        } catch (Exception e) {
            future.cancel(true);//取消该Future里关联的Callable任务
            logger.error("Exception occur " + e.getMessage());
            return null;
        } finally {
            // 关闭线程池  
            exec.shutdown();
        }
        return result;
    }

    protected void deleteTempFile(String recipeName) {
        if ("1".equals(GlobalConstants.getProperty("DELETE_TEMPFILE_AFTER_DOWNLOAD"))) {
            try {
                logger.info("延迟2秒后删除临时文件...");
                Thread.sleep(1500);
            } catch (Exception e) {
            }
            File file = new File(GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipeName + "temp/");
            FileUtil.deleteAllFilesOfDir(file);
            logger.info("删除临时文件:" + GlobalConstants.localRecipePath + GlobalConstants.ftpPath + deviceCode + recipeName + "temp/");
            File downloadfile = new File(GlobalConstants.ftpPath + deviceCode + recipeName + "temp/");
            FileUtil.deleteAllFilesOfDir(downloadfile);
            logger.info("删除临时文件:" + GlobalConstants.localRecipePath + deviceCode + recipeName + "temp/");
            File uploadloadfile = new File(GlobalConstants.localRecipePath.replaceAll("/", "") + "/temp/" + deviceCode + recipeName + "temp/");
            FileUtil.deleteAllFilesOfDir(uploadloadfile);
            logger.info("删除临时文件:" + GlobalConstants.localRecipePath + "/temp/" + deviceCode + recipeName + "temp/");
            FileUtil.delAllTempFile("D:\\" + deviceCode + recipeName + "temp/");
        }

    }

    public boolean uploadRcpFile2FTP(String localRcpPath, String remoteRcpPath, Recipe recipe) {
        return false;
    }

    public void deleteAllRcpFromDevice(String currentRecipeName) {
        List<String> recipeNameList = (List<String>) getEquipRecipeList().get("eppd");
        for (String recipeName : recipeNameList) {
            if (recipeName.equals(currentRecipeName)) {
                continue;
            }
            String resultString = deleteRecipe(recipeName);
            if ("0".equals(resultString)) {
                UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "Recipe:[" + recipeName + "]删除成功.");
            } else {
                UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "Recipe:[" + recipeName + "]" + resultString);
            }
        }
    }

    public boolean getPassport() {
        if (!isConnect()) {
            return false;
        }
        if (getPassport(1)) {
            return true;
        } else {
            logger.debug("设备:" + deviceCode + "直接获取passport失败,尝试循环多次处理...");
            for (int i = 0; i < 5; i++) {
                logger.debug("第" + (i + 1) + "次尝试...");
                boolean pass = getPassport(1);
                if (!pass) {
                    logger.debug("获取失败,等待1秒后再次获取");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                } else {
                    logger.debug("获取成功,结束循环");
                    return true;
                }
            }
            logger.debug("设备:" + deviceCode + "获取passport失败,通讯资源正在被占用");
            return true;
        }
    }

    protected boolean specialCheck() {
        return true;
    }

    public void sendMessage2Eqp(String message) {
        SqlSession sqlSession = MybatisSqlSession.getSqlSession();
        DeviceService deviceService = new DeviceService(sqlSession);
        DeviceInfoExt deviceInfoExt = deviceService.getDeviceInfoExtByDeviceCode(deviceCode);
        sqlSession.close();
        if (deviceInfoExt != null && "Y".equals(deviceInfoExt.getLockSwitch())) {
            synchronized (iSecsHost.iSecsConnection.getSocketClient()) {
                try {
                    if (message.length() > 300) {
                        logger.info("准备向设备发送的消息为:" + message + ",长度超过限制，将被截取");
                        message = message.substring(0, 300) + "...";
                        logger.info("实际向设备发送的消息为:" + message);
                    }
                    iSecsHost.executeCommand("message " + message);
                } catch (Exception e) {
                    logger.error("向设备发送消息时发生异常:" + e.getMessage());
                }
            }
        }
    }

    public boolean isConnect() {
        return this.iSecsHost.isConnect;
    }

    public void setIsConnect(boolean isConnect) {
        this.iSecsHost.isConnect = isConnect;
    }

    public Map getEquipRealTimeState() {
        try {
            equipStatus = getEquipStatus();
            ppExecName = getCurrentRecipeName();
        } catch (NullPointerException e) {
            logger.error("获取设备信息失败,检查通讯状况");
        }
        Map map = new HashMap();
        map.put("PPExecName", ppExecName);
        map.put("EquipStatus", equipStatus);
        map.put("ControlState", controlState);
        map.put("CommState", 1);
        changeEquipPanel(map);
        return map;
    }

    /**
     * 下载前检查，‘0’表示ok
     *
     * @param recipeName
     * @return
     */
    public String checkBeforeDownload(String recipeName) {
        String checkResult = checkCommState();
        if ("0".equals(checkResult)) {
            checkResult = checkEquipStatus();
        }
        if ("0".equals(checkResult)) {
            checkResult = checkPPExecName(recipeName);
        }
        if ("0".equals(checkResult)) {
            checkResult = checkControlState();
        }
        return checkResult;
    }

    protected String checkCommState() {
        Map map = getEquipRealTimeState();
        if (map == null || map.isEmpty()) {
            return "无法获取设备实时状态,请重试并检查设备通讯状态!下载失败！";
        }
        return "0";
    }

    protected String checkEquipStatus() {
        if (NormalConstant.STATUS_RUN.equalsIgnoreCase(equipStatus)) {
            return "设备正在运行，不可调整Recipe！下载失败！";
        }
        return "0";
    }

    protected String checkPPExecName(String recipeName) {
        if (recipeName.equals(ppExecName)) {
        }
        return "0";
    }

    protected String checkControlState() {
        if (NormalConstant.CONTROL_REMOTE_ONLINE.equalsIgnoreCase(controlState)) {
        }
        return "0";
    }

    protected List<RecipePara> checkRcpPara(String recipeId, String deviceCode, List<RecipePara> equipRecipeParas, String masterCompareType) {
        SqlSession sqlsession = MybatisSqlSession.getSqlSession();
        RecipeService recipeService = new RecipeService(sqlsession);
        List<RecipePara> diffRecipeParas = recipeService.checkRcpPara(recipeId, deviceCode, equipRecipeParas, "");
        sqlsession.close();
        return diffRecipeParas;
    }

    protected boolean isGetLegalRecipeName(String recipeName) {
        if ("".equals(recipeName) || "--".equals(recipeName)) {
            return false;
        }
        for (int i = 0; i < recipeName.length(); i++) {
            char a = recipeName.charAt(i);
            if (Character.isLowerCase(a)) {
                return false;
            }
        }
        return true;
    }

    private ISecsHost initComm(String remoteIPAddress, int remoteTCPPort, String deviceType, String deviceCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                iSecsHost = new ISecsHost(remoteIPAddress, String.valueOf(remoteTCPPort), deviceType, deviceCode);
                if (iSecsHost != null && iSecsHost.isConnect && commState == NOT_COMMUNICATING) {
                    if (testOcrConnect()) {
                        Map map = new HashMap();
                        map.put("ControlState", controlState);
                        changeEquipPanel(map);
                        getEquipRealTimeState();
                        iSecsHostList.remove(iSecsHost);
                        iSecsHostList.add(iSecsHost);
                    }
                }
            }
        }).start();
        return iSecsHost;
    }

    public boolean testOcrConnect() {
        List list = sendMsg2Equip("curscreen");
        if (list != null) {
            this.equipState.setCommOn(true);
            commState = 1;
            if (list.contains("Error 0101: Target process service stop.")) {
                this.controlState = NormalConstant.CONTROL_LOCAL_ONLINE;
                equipState.setControlState(NormalConstant.CONTROL_LOCAL_ONLINE);
            } else {
                this.controlState = NormalConstant.CONTROL_REMOTE_ONLINE;
                equipState.setControlState(NormalConstant.CONTROL_REMOTE_ONLINE);
            }

            return true;
        } else {
            this.controlState = NormalConstant.CONTROL_OFFLINE;
            equipState.setControlState(NormalConstant.CONTROL_OFFLINE);
            this.equipState.setCommOn(false);
            commState = 0;

            return false;
        }
    }


    public String organizeRecipe(String partNum, String lotNo) {
        return "";
    }

    public List<String> sendCmdMsg2Equip(String command) {
        final String executCommand = command;
        List<String> result = null;
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Callable<List<String>> call = new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                //开始执行耗时操作
                return iSecsHost.executeCommand(executCommand);
            }
        };

        Future<List<String>> future = exec.submit(call);
        try {
            result = future.get(GlobalConstants.msgWaitTime, TimeUnit.MILLISECONDS);
            logger.debug("Execute future task...");
        } catch (Exception e) {
            future.cancel(true);//取消该Future里关联的Callable任务
            logger.error("Exception occur " + e.getMessage());
        } finally {
            // 关闭线程池
            exec.shutdown();
        }
        return result;
    }

    public boolean uploadData(String macstate) throws RemoteException, ServiceException, MalformedURLException {
//        uploadReportDetail()
        return true;
    }

    protected Map getParmByLotNum() {
        try {
            return AvaryAxisUtil.getParmByLotNum(lotId);
        } catch (Exception e) {
            return null;
        }
    }

    protected String getMainTableName() {
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
                    UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传中，无法获取到生產單號");
                }
                result1 = result2;
                String result3 = AvaryAxisUtil.insertMasterTable(result2, "1", deviceCode, tableNum, classInfo, "001", now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss), "eapsystem", tableNum);  //system临时代替，  創建工號
                if (!"".equals(result3)) {
                    logger.error("报表数据上传中，插入主表數據失败" + result3);
                    UiLogUtil.getInstance().appendLog2EventTab(deviceCode, "报表数据上传中，插入主表數據失败");
                }

            }
        } catch (Exception e) {
            return "1";
        }
        return result1;
    }

    protected List setNormalData(String MacState, Map<String, String> productionMap) {
        if (productionMap == null) {
            productionMap = new HashMap<>();
        }
        List paraValueList = new ArrayList();
        paraValueList.add(MacState);
        if (MacState.equals("保養") || MacState.equals("待料")) {
            if (MacState.equals("待料")) {
//                paraValueList.add(idleStartTime);
                paraValueList.add(lotStartTime);
                LocalDateTime now = LocalDateTime.now();
                lotStartTime = now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss);
                paraValueList.add(lotStartTime);
            } else {
                paraValueList.add(pmState.getStartTime());
                paraValueList.add(pmState.getEndTime());
            }
            paraValueList.add("");
            paraValueList.add(0);
            paraValueList.add("");
            paraValueList.add("");
            paraValueList.add("");
            paraValueList.add(0);
            paraValueList.add("");
            paraValueList.add(0);
            paraValueList.add(0);
            paraValueList.add(0);

        } else {
            paraValueList.add(lotStartTime);
            LocalDateTime now = LocalDateTime.now();
            lotStartTime = now.format(AvaryAxisUtil.dtfyyyyMMddHHmmss);
            paraValueList.add(lotStartTime);
            if (lotId.length() > 20) {
                paraValueList.add(lotId.replaceAll("/", ""));
            } else {
                paraValueList.add(lotId);
            }
            paraValueList.add(productionMap.get("Layer") == null ? 0 : productionMap.get("Layer"));
            paraValueList.add(productionMap.get("MainSerial") == null ? "" : productionMap.get("MainSerial"));
            paraValueList.add(productionMap.get("PartNum") == null ? "" : productionMap.get("PartNum"));
            paraValueList.add(productionMap.get("WorkNo") == null ? "" : productionMap.get("WorkNo"));
            paraValueList.add(productionMap.get("Layer") == null ? 0 : productionMap.get("Layer"));
            paraValueList.add(productionMap.get("LayerName") == null ? "" : productionMap.get("LayerName"));
            paraValueList.add(productionMap.get("Serial") == null ? 0 : productionMap.get("Serial"));
            paraValueList.add(productionMap.get("IsMain") == null ? 0 : productionMap.get("IsMain"));
            paraValueList.add(productionMap.get("OrderId") == null ? 0 : productionMap.get("OrderId"));
        }
        return paraValueList;

    }

    public AlarmRecord delAlarmMsg(String s) {
        return null;
    }

    public String trimLot(String lotNo) {
        return lotNo;
    }

    protected String getClassInfo(LocalDateTime now) {
        int start = 80000;
        int end = 203000;

        String classInfo = "0"; //白班
        int nowTime = Integer.parseInt(now.format(AvaryAxisUtil.dtfHHmmss));
        if (start > nowTime || end < nowTime) {
            classInfo = "1";   //夜班
        }
        return classInfo;
    }

    public List<String> getEquipLog() {
        return null;
    }

    /**
     * @param lot     批次号
     * @param isFirst true 两个批次信息的第一次， false  第二个
     * @return
     */
    public LotInfo needUpload(String lot, boolean isFirst) {
        SqlSession sqlSession = MybatisSqlSession.getBatchSqlSession();
        DeviceService deviceService = new DeviceService(sqlSession);
        try {
            LotInfo lotInfo = deviceService.selectLotInfo(lot);
            if (lotInfo == null) {
                lotInfo = new LotInfo();
                lotInfo.setLotid(lot);
                lotInfo.setStarttime(lotStartTime);
                if (isFirst) {
                    lotInfo.setLotnum(productNum);

                    String qty = productionMap.get("Qty");
                    int tempNum = Integer.parseInt(productNum);
                    int tempQty = Integer.parseInt(qty);

                    if (tempQty <= tempNum) {
                        lotInfo.setComplete(1);
                        deviceService.insertLotInfo(lotInfo);
                        try {
                            deviceService.lotInfoBak(lotInfo);
                        } catch (Exception e) {
                            logger.error("批次数量记录备份失败", e);
                        }
                        return lotInfo;
                    }
                } else {
                    lotInfo.setLotnum(productNum2);
                    String qty = productionMap2.get("Qty");

                    int tempNum = Integer.parseInt(productNum2);
                    int tempQty = Integer.parseInt(qty);

                    if (tempQty <= tempNum) {
                        lotInfo.setComplete(1);
                        deviceService.insertLotInfo(lotInfo);
                        try {
                            deviceService.lotInfoBak(lotInfo);
                        } catch (Exception e) {
                            logger.error("批次数量记录备份失败", e);
                        }
                        return lotInfo;
                    }
                }
                lotInfo.setComplete(0);
                deviceService.insertLotInfo(lotInfo);
                sqlSession.commit();
                return null;
            } else {
                int num = 0;
                int targetNum = 0;
                int nowNum = Integer.parseInt(lotInfo.getLotnum());
                if (isFirst) {
                    num = Integer.parseInt(productNum);
                    targetNum = Integer.parseInt(productionMap.get("Qty"));
                } else {
                    num = Integer.parseInt(productNum2);
                    targetNum = Integer.parseInt(productionMap2.get("Qty"));
                }
                int temp = nowNum + num;
                lotInfo.setLotnum(String.valueOf(temp));
                if (targetNum <= (nowNum + num)) {
                    if (lotInfo.getComplete() == 1) {
                        logger.error("已经是完成状态，又一次被上传数据:" + lot);
                    }
                    lotInfo.setComplete(1);
                    deviceService.updateLotInfo(lotInfo);
                    try {
                        deviceService.lotInfoBak(lotInfo);
                    } catch (Exception e) {
                        logger.error("批次数量记录备份失败", e);
                    }

                    sqlSession.commit();
                    return lotInfo;
                }
                deviceService.updateLotInfo(lotInfo);
                sqlSession.commit();

            }
        } catch (Exception e) {
            logger.error("上传数据数量校验失败", e);
        } finally {
            sqlSession.close();
        }

        return null;
    }
}