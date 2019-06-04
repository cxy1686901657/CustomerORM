package com.qc.cursoa.core;

import com.qc.cursoa.utils.AnnotationUtil;
import com.qc.cursoa.utils.Dom4jUtil;
import org.dom4j.Document;

import java.io.File;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author qc
 * @date 2019/6/3
 * 全局配置类
 */

public class GlobalConfiguration {

    //    public GlobalConfiguration(String configFile){
//         this.configFile=configFile;
//         init();
//     }
    GlobalConfiguration() throws Exception {
        getMapping();
        createTableWithMark();
    }

    //    private  String configFile;//配置文件名
    private static String classpath; //classpath路径
    private static File cfgFile; // 核心配置文件
    private static Map<String, String> propConfig; // <property>标签中的数据
    private static Set<String> mappingSet; //映射配置文件路径
    private static Set<String> entitySet; //实体类
    public static List<Mapper> mapperList; // 映射信息

//    private void init(){
//        //得到的classpath路径
//        classpath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
//        //针对中文路径进行转码
//        try {
//            classpath = URLDecoder.decode(classpath, "utf-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(classpath);
////        cfgFile = new File(classpath + "cursoa.cfg.xml");
//        cfgFile = new File(classpath + configFile);
//        System.out.println(configFile);
//        if (cfgFile.exists()) {
//            // 解析核心配置文件中的数据
//            Document document = Dom4jUtil.getXMLByFilePath(cfgFile.getPath());
//            propConfig = Dom4jUtil.Elements2Map(document, "property", "name");
//            mappingSet = Dom4jUtil.Elements2Set(document, "mapping", "resource");
//            entitySet = Dom4jUtil.Elements2Set(document, "entity", "package");
//        } else {
//            cfgFile = null;
//            System.out.println("notfound cursoa.cfg.xml");
//        }
//    }

    static {
//        得到的classpath路径
        classpath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        //针对中文路径进行转码
        try {
            classpath = URLDecoder.decode(classpath, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(classpath);
        cfgFile = new File(classpath + "cursoa.cfg.xml");
        System.out.println(cfgFile.getName());
        if (cfgFile.exists()) {
            // 解析核心配置文件中的数据
            Document document = Dom4jUtil.getXMLByFilePath(cfgFile.getPath());
            propConfig = Dom4jUtil.Elements2Map(document, "property", "name");
            entitySet = Dom4jUtil.Elements2Set(document, "entity", "package");
            mappingSet = Dom4jUtil.Elements2Set(document, "mapping", "resource");
        } else {
            cfgFile = null;
            System.out.println("notfound cursoa.cfg.xml");
        }
    }

    private Connection getConnection() throws Exception {
        String url = propConfig.get("connection.url");
        String driverClass = propConfig.get("connection.driverClass");
        String username = propConfig.get("connection.username");
        String password = propConfig.get("connection.password");
        Class.forName(driverClass);
        Connection connection = DriverManager.getConnection(url, username, password);
        connection.setAutoCommit(true);
        return connection;
    }


    private void getMapping() throws Exception {

        mapperList = new ArrayList<>();

        //1. 解析xxx.mapper.xml文件拿到映射数据
        for (String xmlPath : mappingSet) {
            Document document = Dom4jUtil.getXMLByFilePath(classpath + xmlPath);
            String className = Dom4jUtil.getPropValue(document, "class", "name");
            String tableName = Dom4jUtil.getPropValue(document, "class", "table");
            Map<String, String> id_id = Dom4jUtil.ElementsID2Map(document);
            Map<String, String> mapping = Dom4jUtil.Elements2Map(document);

            Mapper mapper = new Mapper();
            mapper.setTableName(tableName);
            mapper.setClassName(className);
            mapper.setIdMapper(id_id);
            mapper.setPropMapper(mapping);
            mapperList.add(mapper);
        }

        //2. 解析实体类中的注解拿到映射数据
        for (String packagePath : entitySet) {
            Set<String> nameSet = AnnotationUtil.getClassNameByPackage(packagePath);
            for (String name : nameSet) {
                Class clz = Class.forName(name);
                String className = AnnotationUtil.getClassName(clz);
                String tableName = AnnotationUtil.getTableName(clz);
                Map<String, String> id_id = AnnotationUtil.getIdMapper(clz);
                Map<String, String> mapping = AnnotationUtil.getPropMapping(clz);

                Mapper mapper = new Mapper();
                mapper.setTableName(tableName);
                mapper.setClassName(className);
                mapper.setIdMapper(id_id);
                mapper.setPropMapper(mapping);
                mapperList.add(mapper);
            }
        }

    }

    //    简单模拟实现对映射的表的创建
    private void createTableWithMark() {
        for (Mapper mapper : mapperList) {
            String createTableSql = "create table" + mapper.getTableName() + "(";

            for (String column : mapper.getPropMapper().values()) {
                createTableSql = createTableSql + column + " " + "varchar(255),";
            }

            String substring = createTableSql.substring(0, createTableSql.length() - 1);
            String end = substring + ");";
            System.out.println(end);
        }
    }

    //创建Session对象
    public Session buildSession() throws Exception {
        //1. 连接数据库
        Connection connection = getConnection();

        //2. 得到映射数据
        getMapping();

        //3. 创建ORMSession对象
        return new Session(connection);

    }


}
