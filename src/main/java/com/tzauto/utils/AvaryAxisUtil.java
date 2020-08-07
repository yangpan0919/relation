package com.tzauto.utils;


import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Schema;
import org.apache.log4j.Logger;


import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AvaryAxisUtil {

    public static Map<String, String[]> parmsNames;
    public static Map<String, String> configMap = new HashMap<>();

    private static Logger logger = Logger.getLogger(AvaryAxisUtil.class);

    public static DateTimeFormatter dtfyyyy_MM_dd_HH_mm_ss = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static DateTimeFormatter dtfyyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static DateTimeFormatter dtfyyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static DateTimeFormatter dtfHHmmss = DateTimeFormatter.ofPattern("HHmmss");
    public static DateTimeFormatter dtfyyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static Map<String, Map> mesInterfaceParaMap = new HashMap<>();

    //    private static final String url = "http://szecpw014.eavarytech.com:8001/WebServiceForSZ/Service1.asmx";   //URL地址
    private static final String url = "http://qhecpw001.eavarytech.com:8001/WebServiceForQHD/Service1.asmx";   //URL地址
    private static final String namespace = "http://tempuri.org/";

    public static String tableNum;

    //    private static final String namespace = GlobalConstants.getProperty("AVARY_MES_WS_NAMESPACE");
    static {
        parmsNames = new HashMap<>();

        mesInterfaceParaMap = new HashMap<>();
        String textPath = "D:/EAP/webServiceInterface";
        File file = new File(textPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File fileTemp : files) {
                Map<String, Object> parmsNamesTemp = new HashMap<>();
                //要求配置文件名称为：devicetype_wbconfig.properties格式
                String deviceType = fileTemp.getName().split("_")[0];


                FileInputStream fis = null;
                Properties properties = new Properties();
                try {
                    fis = new FileInputStream(fileTemp.getPath());
                    properties.load(fis);
                    properties.forEach((x, y) -> {
                        if ((String.valueOf(y)).contains(",")) {
                            String[] arr = ((String) y).split(",");
                            if (arr.length == 2) {
                                parmsNamesTemp.put((String) x, arr);
                                parmsNames.put((String) x, arr);
                            } else {
//                                logger.error("webService 接口配置文件路径加载错误！" + x + "=" + y);
//                                main.stop();
                            }
                        } else {
                            parmsNamesTemp.put((String) x, (String) y);
                            configMap.put((String) x, (String) y);
                        }
                    });
                    mesInterfaceParaMap.put(deviceType, parmsNamesTemp);
                    int i = 1;
                    i++;
                    /**
                     * workLicense=0010,HR001
                     * getProductionCondition=0005,PA001
                     * isInitialPart=0004,G0003
                     * get21Exposure=0004,0018
                     * firstProductionIsOK=0004,0009
                     * tableQuery=0004,G0001
                     * getOrderNum=0004,0002
                     * insertMasterTable=0004,0003
                     * getParmByLotNum=0001,0002
                     * getParmByLotNumAndLayer=0001,0009
                     * insertTable=0004,0006
                     * getBom=FPC02,FPC05
                     */

                } catch (Exception e) {
//                    logger.error("Exception", e);
//                    main.stop();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        tableNum = configMap.get("tablename");
        logger.info("tablename为：" + tableNum);
    }

    //plasma
    public static String insertTable(String paperNo, String macState, String startTime, String endTime, String lotnum, String layer, String mainSerial, String partnum, String workNo, String sfcLayer, String layerName
            , String serial, String IsMain, String orderId, String Item1, String qty, String IsOk, String Item2, String Item4, String Item5, String Item6, String CreateEmpid) throws RemoteException, ServiceException, MalformedURLException {

        Call call = getCallForSendDataToSerGrp();
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(dtfyyyy_MM_dd_HH_mm_ss);
        String[] arr = parmsNames.get("insertTable");
        Object[] params = new Object[]{"test", "test", "#01", arr[0], arr[1],
                "PaperNo|MacState|StartTime|EndTime|Lotnum|Layer|MainSerial|Partnum|WorkNo|SfcLayer|LayerName|Serial|IsMain|OrderId|Item1|Qty|IsOk|Item2|Item4|Item5|Item6|CreateEmpid|CreateTime|ModifyEmpid|ModifyTime"
                , createParm(paperNo, macState, startTime, endTime, lotnum, layer, mainSerial, partnum, workNo, sfcLayer, layerName, serial, IsMain, orderId, Item1, qty, IsOk, Item2, Item4, Item5, Item6, CreateEmpid, time, CreateEmpid, time)
                , time};
        String result = (String) call.invoke(params); //方法执行后的返回值
        return result;
    }


    private static Call getCallForSendDataToSer() throws ServiceException, MalformedURLException {
        String actionUri = "sendDataToSer"; //Action路径
        String op = "sendDataToSer"; //要调用的方法名
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(new URL(url));
        call.setUseSOAPAction(true);
        call.setSOAPActionURI(namespace + actionUri); // action uri
        call.setOperationName(new QName(namespace, op));// 设置要调用哪个方法
// 设置参数名称，具体参照从浏览器中看到的
        call.addParameter(new QName(namespace, "username"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "password"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "equid"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "parameterName"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "parameterValue"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "createDate"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型

        //call.setReturnType(new QName(namespace,"getinfo"),Model.class); //设置返回结果为是某个类
//        call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);//设置结果返回类型
        call.setReturnType(XMLType.XSD_STRING);//设置结果返回类型
        return call;
    }


    protected static Call getCallForGetDataFromSer() throws ServiceException, MalformedURLException {
        String actionUri = "getDataFromSer"; //Action路径
        String op = "getDataFromSer"; //要调用的方法名
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(new URL(url));
        call.setUseSOAPAction(true);
        call.setSOAPActionURI(namespace + actionUri); // action uri
        call.setOperationName(new QName(namespace, op));// 设置要调用哪个方法
// 设置参数名称，具体参照从浏览器中看到的
        call.addParameter(new QName(namespace, "username"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "password"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "equid"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "groupid"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "funid"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "pValue"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "createDate"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型

        //call.setReturnType(new QName(namespace,"getinfo"),Model.class); //设置返回结果为是某个类
//        call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);//设置结果返回类型
        call.setReturnType(XMLType.XSD_SCHEMA);//设置结果返回类型
        return call;
    }

    private static Call getCallForSendDataToSerGrp() throws ServiceException, MalformedURLException {
        String actionUri = "sendDataToSerGrp"; //Action路径
        String op = "sendDataToSerGrp"; //要调用的方法名
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(new URL(url));
        call.setUseSOAPAction(true);
        call.setSOAPActionURI(namespace + actionUri); // action uri
        call.setOperationName(new QName(namespace, op));// 设置要调用哪个方法
// 设置参数名称，具体参照从浏览器中看到的
        call.addParameter(new QName(namespace, "username"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "password"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "equid"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "groupid"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "funid"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "parameterName"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "parameterValue"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型
        call.addParameter(new QName(namespace, "createDate"), XMLType.XSD_STRING, ParameterMode.IN);   //设置请求参数及类型

        //call.setReturnType(new QName(namespace,"getinfo"),Model.class); //设置返回结果为是某个类
//        call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);//设置结果返回类型
        call.setReturnType(XMLType.XSD_STRING);//设置结果返回类型
//        call.setReturnType(QName.valueOf(XMLType.NS_PREFIX_XML));//设置结果返回类型
        return call;
    }


    public static String createParm(String... parms) {
        StringBuilder sb = new StringBuilder();
        int length = parms.length;
        for (int i = 0; i < length - 1; i++) {
            sb.append(parms[i]).append("|");
        }
        sb.append(parms[length - 1]);
        return sb.toString();
    }

    private static String createParm(List parms) {
        StringBuilder sb = new StringBuilder();
        int length = parms.size();
        for (int i = 0; i < length - 1; i++) {
            sb.append(parms.get(i)).append("|");
        }
        sb.append(parms.get(length - 1));
        return sb.toString();
    }

    protected static List<Map<String, String>> parseXml(Schema schema) {
        List<Map<String, String>> list = new ArrayList<>();
        MessageElement[] elements = schema.get_any();
//        List elementHead = elements[0].getChildren();//消息头
        List elementBody = elements[1].getChildren();//消息体信息,DataSet对象
        if (elementBody == null || elementBody.size() == 0) {
            return list;
        }
        String text = elementBody.get(0).toString();//消息体的字符串形式
        createList(list, text);
        return list;

    }

    public static void createList(List list, String text) {
        if (text.contains("<Table")) {
            int i = text.indexOf("<Table");
            int j = text.indexOf("</Table>");
            if (j < 0) {
                return;
            }
            String sub1 = text.substring(i + 6, j);
            i = sub1.indexOf(">");
            sub1 = sub1.substring(i + 1);
            String sub2 = text.substring(j + 8);

            Map<String, String> map = new HashMap<>();
            createMap(map, sub1);
            list.add(map);
            createList(list, sub2);
        }
    }

    public static void createMap(Map<String, String> map, String text) {
        if (text.contains("<")) {
            int i = text.indexOf("<");
            int j = text.indexOf(">");
            String key = text.substring(i + 1, j);
            String sub1 = text.substring(j + 1);
            i = sub1.indexOf("<");
            j = sub1.indexOf(">");
            String value = sub1.substring(0, i);
            map.put(key, value);
            String sub2 = sub1.substring(j + 1);
            createMap(map, sub2);
        }
    }

    public static String unicode2String(String unicode) {
        StringBuffer string = new StringBuffer();
        String pre = "";
        if (unicode.contains("&#x")) {
            int index = unicode.indexOf("&#x");
            if (index > 0) {
                pre = unicode.substring(0, index);
                unicode = unicode.substring(index);
            }
            String[] split = unicode.split(";");
            for (int i = 0; i < split.length; i++) {
                String temp = split[i];
                if (temp.startsWith("&#x")) {
                    String replace = temp.replace("&#x", "");
                    int data = Integer.parseInt(replace, 16);
                    string.append((char) data);
                } else {
                    String[] split1 = temp.split("&#x");
                    if (split1.length == 1) {
                        string.append(split1[0]);
                        continue;
                    }
                    string.append(split1[0]);
                    int data = Integer.parseInt(split1[1], 16);
                    string.append((char) data);
                }
            }
        } else if (unicode.contains("&#")) {
            int index = unicode.indexOf("&#");
            if (index > 0) {
                pre = unicode.substring(0, index);
                unicode = unicode.substring(index);
            }
            String[] split = unicode.split(";");
            for (int i = 0; i < split.length; i++) {
                String temp = split[i];
                if (temp.startsWith("&#")) {
                    String replace = temp.replace("&#", "");
                    int data = Integer.parseInt(replace, 10);
                    string.append((char) data);
                } else {
                    String[] split1 = temp.split("&#");
                    if (split1.length == 1) {
                        string.append(split1[0]);
                        continue;
                    }
                    string.append(split1[0]);
                    int data = Integer.parseInt(split1[1], 10);
                    string.append((char) data);
                }
            }
        } else if (unicode.startsWith("\\u")) {
            String[] strs = unicode.split("\\\\u");
            for (int i = 0; i < strs.length; i++) {
                String temp = strs[i];
                if ("".equals(temp)) {
                    continue;
                }
                int data = Integer.parseInt(strs[i], 16);
                string.append((char) data);
            }
        } else {
            return unicode;
        }
        return pre + string.toString();
    }

    //plasma 秦皇岛A1厂 上传数据
    public static String insertTableOneFactoryQHD(String report, String doDate, String machineNo, String doClass, String item9, String item5, String item8, String item11
            , String item4, String item10, String item2, String item3, String item6, String createEmpid) throws RemoteException, ServiceException, MalformedURLException {

        Call call = getCallForSendDataToSerGrp();
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(dtfyyyy_MM_dd_HH_mm_ss);
        String[] arr = parmsNames.get("insertTable");
        String parm = createParm(report, doDate, machineNo, doClass, item9, item5, item8, item11, item4, item10, item2, item3, item6, createEmpid, createEmpid);
        Object[] params = new Object[]{"test", "test", "#01", arr[0], arr[1],
                "report|DODATE|MACHINENO|DOCLASS|ITEM9|ITEM5|ITEM8|ITEM11|ITEM4|ITEM10|ITEM2|ITEM3|ITEM6|CREATEEMPID|CHECKEMPID"
                , parm
                , time};
        String result = (String) call.invoke(params); //方法执行后的返回值
        logger.info(arr[0] + "|" + arr[1] + " 1厂明細表數據插入:" + parm + "，结果为：" + result);
        return result;
    }

}
